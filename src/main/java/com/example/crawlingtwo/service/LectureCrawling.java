package com.example.crawlingtwo.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import com.example.crawlingtwo.config.WebDriverConfig;
import com.example.crawlingtwo.entity.Lecture;
import com.example.crawlingtwo.entity.SubCategory;
import com.example.crawlingtwo.repository.LectureRepository;
import com.example.crawlingtwo.repository.SubCrawlingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LectureCrawling {

	private final SubCrawlingRepository subCrawlingRepository;
	private final LectureRepository lectureRepository;
	private final WebDriverConfig webDriverConfig;

	public List<Lecture> getLecture(SubCategory subCategory) {
		List<Lecture> lectureList = new ArrayList<>();
		WebDriver driver = webDriverConfig.createDriver();

		int page = 1;

		while (true) {
			String pagedUrl = subCategory.getUrl() + "?page_number=" + page;
			driver.get(pagedUrl);

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
			wait.until(ExpectedConditions.presenceOfElementLocated(
				By.cssSelector("ul.mantine-1avyp1d")
			));

			List<WebElement> lectures = driver.findElements(By.cssSelector("ul.mantine-1avyp1d > li"));

			int lectureCountOnPage = 0;

			for (WebElement lecture : lectures) {

				try {

					String name = lecture.findElement(
							By.cssSelector("p.mantine-Text-root.css-10bh5qj.mantine-b3zn22"))
						.getText()
						.trim();

					String instructor = lecture.findElement(
							By.cssSelector("p.mantine-Text-root.css-1r49xhh.mantine-aiouth"))
						.getText()
						.trim();

					String lectureLink = lecture.findElement(By.cssSelector("a[href*='/course/']"))
						.getAttribute("href");

					String lectureImage = lecture.findElement(By.cssSelector("img")).getAttribute("src");

					lectureList.add(new Lecture(name, instructor, lectureImage, lectureLink, subCategory));

					lectureCountOnPage++;

				} catch (NoSuchElementException e) {
					continue;
				}
			}

			if (lectureCountOnPage == 0)
				break;
			page++;
		}

		driver.quit();
		lectureRepository.saveAll(lectureList);

		return lectureList;

	}

	public void subCrawlingAll() {
		List<SubCategory> subCategories = subCrawlingRepository.findAll();
		for (SubCategory subCategory : subCategories) {
			getLecture(subCategory);
		}
	}
}