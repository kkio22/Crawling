package com.example.crawling;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
public class InflearnCategoryCrawler {

	private final CrawlingRepository crawlingRepository;

	public InflearnCategoryCrawler(CrawlingRepository crawlingRepository) {
		this.crawlingRepository = crawlingRepository;
	}

	// 매서드로 작성
	public void crawlCategories() {
		// Chrome 옵션 설정
		ChromeOptions options = new ChromeOptions();
		//options.addArguments("--headless=new"); //
		options.addArguments("--lang=ko"); //언어 설정
		options.addArguments(
			"--no-sandbox"); // 샌드박스 보안기능 비활성화  -> 크롬은 기본적으로 sandbox에서 실행됨 근데 리눅스나 CI/CD 환경에서는 이 sandbox와 충돌이 생겨 크롬이 실행이 안 될 수도 있음 그래서 꺼놓는 거임
		options.addArguments(
			"--disable-dev-shm-usage"); // dev/shm 메모리 사용 비활성화 -> 크롬은 dev/shm이라는 공유 메모리 공간을 사용함 근데 도커나 CI 환경에서는 이 공간이 매우 작아서 브라우저가 충돌 날 수 있음 그래서 이걸 끄면 크롬이 일반 디스크 공간을 사용해서 안정적이다.
		options.addArguments(
			"--disable-gpu"); //gpu 가속 비활성화 -> 크롬은 ui 랜더링 성능을 높이기 위해 gpu 가속을 사용함 근데 헤드리스 모드나 GUI 없는 서버 환경에서는 gpu를 사용할 수 없거나 충돌이 일어나서 꺼놓음

		WebDriver driver = new ChromeDriver(options); // 옵션 넣어서 크롬 브라우져 실행 -> 원하는 페이지에 연결 가능 상태 완료

		try {
			driver.get("https://www.inflearn.com/courses/"); // 연결 링크 넣어서 원하는 페이지에 접속

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
			wait.until(
				ExpectedConditions.presenceOfElementLocated(
					By.cssSelector("a[href*='inflearn.com/courses/']")
				) // 태그 분석해서 DOM에 내가 원하는 css 부분을 가져옴 그래서 크롤링을 할 때 이게 있는지 확인하는 코드 (시간은 30초)
			);

			Set<String> visited = new HashSet<>(); // 중복 방지를 위해 set 객체 선언

			List<WebElement> categories = driver.findElements(
				By.cssSelector("a[href*='inflearn.com/courses/']")
			).stream().filter(e -> {
				String text = e.getText().trim();
				return !text.equals("전체") && !text.isBlank();
			}).toList();  // 전체는 크롤링 안 되게 filter 처리

			for (WebElement category : categories) {

				String href = category.getAttribute("href");
				// 전체 URL로 변환 (상대 경로일 경우)
				if (!href.startsWith("http")) {
					href = "https://www.inflearn.com" + href; // href는 courses부터 시작함
				}

				// 상위 카테고리 링크 중복 방지 또 안 돌기 위한 장치
				if (visited.contains(href))
					continue;
				visited.add(href);

				// 여기서 바로 텍스트 추출
				String categoryName = category.getText().trim();


				System.out.println("상위 카테고리명: " + categoryName);
				System.out.println("상위 링크: " + href);
				System.out.println("-----");

				// 한 번 돌고 이제 하위 분류 돌아야하니 다시 한번 있는지 확인하는 작업 반복해야 함

				try {
					((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", category);
					Thread.sleep(300); // 스크롤 안정화
					wait.until(ExpectedConditions.elementToBeClickable(category));
					((JavascriptExecutor)driver).executeScript("arguments[0].click();", category);
					Thread.sleep(1000); // 클릭 후 로딩 대기
				} catch (Exception e) {
					System.out.println("클릭 실패: " + categoryName + " => " + e.getMessage());
					continue;
				}

				List<WebElement> subCategories = driver.findElements(
						By.cssSelector("a.mantine-Text-root.mantine-Anchor-root.mantine-f6w3s2"))
					.stream()
					.filter(e -> {
						String title = e.getText().trim();
						return !title.equals("전체") && !title.isBlank();
					}).toList();

				for (WebElement subCategory : subCategories) {

					String subHref = subCategory.getAttribute("href");

					if (!subHref.startsWith("http")) {
						subHref = "https://www.inflearn.com" + subHref;
					}

					String subCategoryName = subCategory.getText().trim();

					System.out.println("하위 카테고리명: " + subCategoryName);
					System.out.println("하위 링크: " + subHref);
					System.out.println("-----");

					try {
						((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", subCategory); //subCategory가 화면에 보이도록 브라우저 창을 자동으로 스크롤하는 코드이다. 왜냐면 크롤링할때 화면에 보여져야 함
						Thread.sleep(300); // 스크롤 안정화
						wait.until(ExpectedConditions.elementToBeClickable(subCategory));
						((JavascriptExecutor)driver).executeScript("arguments[0].click();", subCategory);
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println("클릭 실패: " + subCategoryName + " => " + e.getMessage());
						continue;
					}

					/*
					문제점 1. 값이 안 나옴
					문제점 2. 태그가 다른 것 같음 -> 설마 바껴야 하나?
					 */
					while (true) {
						List<WebElement> lectures = driver.findElements(
							By.cssSelector("li.css-8atqhb.mantine-1avyp1d"));

						for (WebElement lecture : lectures) {

							String lectureLink = lecture.findElement(By.cssSelector("a[href*='/course/']"))
								.getAttribute("href"); //링크 이상함 -> '^' 대신 '*'(다 포함한다는 의미) 해야 함

							if (lectureLink == null || lectureLink.isBlank())
								continue;

							String lectureImg = lecture.findElement(By.cssSelector("img")).getAttribute("src");

							String instructor = lecture.findElement(
									By.cssSelector("p.mantine-Text-root.css-1r49xhh.mantine-aiouth"))
								.getText()
								.trim();

							String title = lecture.findElement(
									By.cssSelector("p.mantine-Text-root.mantine-b3zn22"))
								.getText()
								.trim();

							String description = lecture.findElement( // 설명은 나오지도 않음
								By.cssSelector("p.mantine-Text-root.css-1uons5e.mantine-121fe6h")).getText().trim(); // 값이 안 나옴

							String price = lecture.findElement(
								By.cssSelector("p.mantine-Text-root.css-uzjboo.mantine-cm9qo8")).getText().trim();

							String reviewCount = lecture.findElement( // 에러 터지는 곳
									By.cssSelector("p.mantine-Text-root.css-bh9d0c.mantine-1s1zpjz"))
								.getText()
								.replaceAll("[()]", "")
								.trim();

							String rating = lecture.findElement(
								By.cssSelector("p.mantine-Text-root.mantine-3qdwx9")).getText().trim();

							String studentCount = lecture.findElement(
								By.cssSelector("span.mantine-Text-root.mantine-jkxzgx")).getText().trim();

							System.out.println("제목: " + title);
							System.out.println("강사: " + instructor);
							System.out.println("썸네일: " + lectureImg);
							System.out.println("강의 설명: " + description);
							System.out.println("별점: " + rating);
							System.out.println("수강생 수: " + studentCount);
							System.out.println("리뷰 수: " + reviewCount);
							System.out.println("가격: " + price);
							System.out.println("강의 링크" + lectureLink);

						}
						System.out.println("오류확인");

						List<WebElement> buttons = driver.findElements(By.cssSelector("button.mantine-qm6umh"));
						WebElement button = buttons.get(buttons.size() - 1);
						if (!button.isEnabled())
							break; // isEnabled는 클릭이 가능하냐의 의미임
						try {
							((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);",
								button); // 클릭하기 위해서는 이미지에서 안 보이는거 보이게 만들기
							Thread.sleep(300);
							((JavascriptExecutor)driver).executeScript("arguments[0].click();", button);
							//button.click(); //데이터로 가져온 버튼 누르는 코드 (이거 에러 남)
							Thread.sleep(1500);
						}catch (Exception e) {
							break;
						}
					}

				}
			}

			driver.get(
				"https://www.inflearn.com/courses/"); //하위로 이동하기 위해 전체에서 상위 카테고리를 클릭해서 페이지 이동이 있기 때문에 처음 페이지로 돌아가는 과정 필요
			Thread.sleep(1000);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}
}

