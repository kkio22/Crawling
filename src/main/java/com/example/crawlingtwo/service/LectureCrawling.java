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
		driver.get(subCategory.getUrl());

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(ExpectedConditions.presenceOfElementLocated(
			By.cssSelector("li.css-8atqhb.mantine-1avyp1d")
			));

		List<WebElement> lectures = driver.findElements(
			By.cssSelector("li.css-8atqhb.mantine-1avyp1d"));

		for (WebElement lecture : lectures) {

			String name = lecture.findElement(
					By.cssSelector("p.mantine-Text-root.mantine-b3zn22"))
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

		}

		lectureRepository.saveAll(lectureList);

		return lectureList;

	}

	public void subCrawlingAll (){
		List<SubCategory> subCategories = subCrawlingRepository.findAll();
		for (SubCategory subCategory : subCategories) {
			getLecture(subCategory);
		}
	}
}
