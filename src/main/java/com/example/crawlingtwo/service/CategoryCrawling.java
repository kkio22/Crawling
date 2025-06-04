package com.example.crawlingtwo.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import com.example.crawlingtwo.config.WebDriverConfig;
import com.example.crawlingtwo.entity.Category;
import com.example.crawlingtwo.repository.CrawlingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryCrawling {

	public final WebDriverConfig webDriverConfig;
	public final CrawlingRepository crawlingRepository;

	public List<Category> getMainCategories() {
		WebDriver driver = webDriverConfig.createDriver();
		List<Category> mainCategoryList = new ArrayList<>();
		Set<String> visited = new HashSet<>();

		driver.get("https://www.inflearn.com/courses/"); // 메인 페이지

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(
			ExpectedConditions.presenceOfElementLocated(
				By.cssSelector("a[href*='inflearn.com/courses/']")
			)
		);

		//상위 카테고리

		List<WebElement> mainCategories = driver.findElements(By.cssSelector("a[href*='inflearn.com/courses/']"))
			.stream()
			.filter(e -> {
				String text = e.getText().trim();
				return !text.equals("전체") && !text.isBlank();
			})
			.toList(); // 상위 카테고리 데이터 추출

		for (WebElement mainCategory : mainCategories) {
			String mainHref = mainCategory.getAttribute("href");
			String mainCategoryName = mainCategory.getText().trim();

			if (!mainHref.startsWith("http")) {
				mainHref = "https://www.inflearn.com" + mainHref;
			}

			if (visited.contains(mainHref))
				continue;
			visited.add(mainHref);

			mainCategoryList.add(new Category(mainCategoryName, mainHref)); //엔티티에 저장 -> 나중에 list로 레파지토리에 저장 가능

		}

		System.out.println("상위 카테고리 갯수" + mainCategoryList.size());

		crawlingRepository.saveAll(mainCategoryList);

		return mainCategoryList;

	}

}
