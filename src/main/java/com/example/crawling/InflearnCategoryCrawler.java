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

	private final Repository repository;

	public InflearnCategoryCrawler(Repository repository) {
		this.repository = repository;
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

			int i =0;
			while(true){
				List<WebElement> categories = driver.findElements(
					By.cssSelector("a[href*='inflearn.com/courses/']")
				); // 상위 분류

				if(i >= categories.size()) break;

				WebElement category= categories.get(i);

				i++;

				String href = category.getAttribute("href");
				// 전체 URL로 변환 (상대 경로일 경우)
				if (!href.startsWith("http")) {
					href = "https://www.inflearn.com" + href; // href는 courses부터 시작함
				}

				// 중복 제거
				if (visited.contains(href))
					continue;
				visited.add(href);

				// 여기서 바로 텍스트 추출
				String categoryName = category.getText().trim();

				if(categoryName.equals("전체")) continue;// 해당 반복문 그냥 통과임

				// 빈 문자열이면 건너뜀
				if (categoryName.isBlank())
					continue;


				System.out.println("상위 카테고리명: " + categoryName);
				System.out.println("상위 링크: " + href);
				System.out.println("-----");

				// 한 번 돌고 이제 하위 분류 돌아야하니 다시 한번 있는지 확인하는 작업 반복해야 함


				try {
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", category);
					Thread.sleep(300); // 스크롤 안정화
					wait.until(ExpectedConditions.elementToBeClickable(category));
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", category);
					Thread.sleep(1000); // 클릭 후 로딩 대기
				} catch (Exception e) {
					System.out.println("클릭 실패: " + categoryName + " => " + e.getMessage());
					continue;
				}



				List<WebElement> subCategories = driver.findElements(By.cssSelector("a.mantine-Text-root.mantine-Anchor-root.mantine-f6w3s2"));


				for (WebElement subCategory : subCategories) {

					try {
						String subHref = subCategory.getAttribute("href");

						if (!subHref.startsWith("http")) {
							subHref = "https://www.inflearn.com" + subHref;
						}

						String subCategoryName = subCategory.getText().trim();

						if (subCategoryName.equals("전체"))
							continue;

						if (subCategoryName.isBlank())
							continue;

						System.out.println("하위 카테고리명: " + subCategoryName);
						System.out.println("하위 링크: " + subHref);
						System.out.println("-----");
					}catch (Exception e){
						continue;
					}

					while (true) {
						List<WebElement> lectures = driver.findElements(
							By.cssSelector("li.css-8atqhb.mantine-1avyp1d"));

						for (WebElement lecture : lectures) {

							try {
								// String lectureLink = lecture.findElement(By.cssSelector("a[href^='/course/']"))
								// 	.getAttribute("href"); //링크 이상함 이거 뭔지 모르겠음
								//
								// if (lectureLink == null || lectureLink.isBlank())
								// 	continue;

								String lectureImg = lecture.findElement(By.cssSelector("img")).getAttribute("src");

								String instructor = lecture.findElement(By.cssSelector("p.mantine-Text-root.css-1r49xhh.mantine-aiouth"))
									.getText()
									.trim();

								String title = lecture.findElement(By.cssSelector("p.mantine-Text-root.mantine-b3zn22"))
									.getText()
									.trim();

								String description = lecture.findElement(By.cssSelector("p.mantine-Text-root.mantine-121f6eh")).getText().trim();

								String price = lecture.findElement(By.cssSelector("mantine-Text-root css-uzjboo mantine-cm9qo8")).getText().trim();

								String reviewCount = lecture.findElement(By.cssSelector("p.mantine-Text-root.mantine-1slzpiz")).getText().replaceAll("[()]", "").trim();

								String rating = lecture.findElement(By.cssSelector("p.mantine-Text-root.mantine-3qdwx9")).getText().trim();

								String studentCount = lecture.findElement(By.cssSelector("span.mantine-Text-root.mantine-jxkzgx")).getText().trim();



								System.out.println("제목: " + title);
								System.out.println("강사: " + instructor);
								System.out.println("썸네일: " + lectureImg);
								System.out.println("강의 설명: " + description);
								System.out.println("별점: " + rating);
								System.out.println("수강생 수: " + studentCount);
								System.out.println("리뷰 수: " + reviewCount);
								System.out.println("가격: " + price);
								System.out.println("썸네일: " + lectureImg);
								//System.out.println("강의 링크" + lectureLink);
							}catch(Exception e){
								continue;
							}

						}

						List<WebElement> buttons = driver.findElements(By.cssSelector("button.mantine-qm6umh"));
						WebElement button = buttons.get(buttons.size() -1);
						if(!button.isEnabled()) break; // isEnabled는 클릭이 가능하냐의 의미임
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button); // 클릭하기 위해서는 이미지에서 안 보이는거 보이게 만들기
						Thread.sleep(300);
						((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
						//button.click(); //데이터로 가져온 버튼 누르는 코드 (이거 에러 남)
						Thread.sleep(1500);

					}
				}

				driver.get("https://www.inflearn.com/courses/"); //하위로 이동하기 위해 전체에서 상위 카테고리를 클릭해서 페이지 이동이 있기 때문에 처음 페이지로 돌아가는 과정 필요
				Thread.sleep(1000);


 			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}
}
