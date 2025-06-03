package com.example.crawling;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
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
			driver.get("https://www.inflearn.com/courses"); // 연결 링크 넣어서 원하는 페이지에 접속

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
			wait.until(
				ExpectedConditions.presenceOfElementLocated(
					By.cssSelector("a[href*='/courses/']")
				) // 태그 분석해서 DOM에 내가 원하는 css 부분을 가져옴 그래서 크롤링을 할 때 이게 있는지 확인하는 코드 (시간은 30초)
			);

			List<WebElement> categories = driver.findElements(
				By.cssSelector("a[href*='/courses/']")
			); // 상위 분류

			Set<String> visited = new HashSet<>(); // 중복 방지를 위해 set 객체 선언

			for (int i = 1; i < categories.size(); i++) {
				String href = categories.get(i).getAttribute("href");
				// 전체 URL로 변환 (상대 경로일 경우)
				if (!href.startsWith("http")) {
					href = "https://www.inflearn.com" + href; // href는 courses부터 시작함
				}

				// 중복 제거
				if (visited.contains(href))
					continue;
				visited.add(href);

				// 여기서 바로 텍스트 추출
				String categoryName = categories.get(i).getText().trim();

				// 빈 문자열이면 건너뜀
				if (categoryName.isBlank())
					continue;

				Lecture lecture = Lecture.builder()
					.topCategory(categoryName)
					.categoryLink(href)
					.build();

				repository.save(lecture);


				System.out.println("카테고리명: " + categoryName);
				System.out.println("링크: " + href);
				System.out.println("-----");

				// 한 번 돌고 이제 하위 분류 돌아야하니 다시 한번 있는지 확인하는 작업 반복해야 함 -> 이걸 생각 못 함

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}
}
