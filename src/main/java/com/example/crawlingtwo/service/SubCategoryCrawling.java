package com.example.crawlingtwo.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import com.example.crawlingtwo.config.WebDriverConfig;
import com.example.crawlingtwo.entity.Category;
import com.example.crawlingtwo.entity.SubCategory;
import com.example.crawlingtwo.repository.CrawlingRepository;
import com.example.crawlingtwo.repository.SubCrawlingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubCategoryCrawling {

	private final WebDriverConfig webDriverConfig;
	private final SubCrawlingRepository subCrawlingRepository;
	private final CrawlingRepository crawlingRepository;
	//click이 아니라 그냥 db에 저장된 url을 기준으로 가져오기 -> 어차피 상위 가져오고 저장하고 하위 카테고리 가져오게 하면 순서는 상관없지 않나?

	public List<SubCategory> getSubCategories(Category mainCategory) {
		WebDriver driver = webDriverConfig.createDriver();
		List<SubCategory> subCategoryList = new ArrayList<>();
		driver.get(mainCategory.getUrl()); //click 말고 url로 반복하자
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(
			ExpectedConditions.presenceOfElementLocated(
				By.cssSelector("a.mantine-Text-root.mantine-Anchor-root.mantine-f6w3s2")
			)
		); //이 코드 없어서 오류남 가져오는데 시간이 걸린 듯

		List<WebElement> subCategories = driver.findElements(
				By.cssSelector("a.mantine-Text-root.mantine-Anchor-root.mantine-f6w3s2"))
			.stream()
			.filter(e -> {
				String text = e.getText().trim();
				return !text.equals("전체") && !text.isBlank();
			})
			.toList();

		for (WebElement subCategory : subCategories) {
			String subHref = subCategory.getAttribute("href");
			String subCategoryName = subCategory.getText().trim();

			if (!subHref.startsWith("http")) {
				subHref = "https://www.inflearn.com" + subHref;
			}

			subCategoryList.add(new SubCategory(subCategoryName, subHref, mainCategory));
		}

		System.out.println("하위 카테고리 갯수" + subCategoryList.size());

		subCrawlingRepository.saveAll(subCategoryList);

		return subCategoryList;

	}

	public void crawAll(){
		List<Category> mainCategories = crawlingRepository.findAll();
		for (Category mainCategory : mainCategories){
			getSubCategories(mainCategory);
		}
	}

}
