package com.example.crawling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CrawlerRunner implements CommandLineRunner { // spring boot가 실행될 때 동시에 크롤링도 시작되어야 하기 때문에 그걸 해주는 클래스가 따로 있어야 함

	private final InflearnCategoryCrawler inflearnCategoryCrawler;


	public CrawlerRunner(InflearnCategoryCrawler inflearnCategoryCrawler) {
		this.inflearnCategoryCrawler = inflearnCategoryCrawler;
	}

	@Override // 이 매서드가 있어야 함
	public void run(String... args) throws Exception {
		System.out.println("크롤링 시작");
		inflearnCategoryCrawler.crawlCategories(); // inflearnCategoryCrawler에 있는 매서드 실행
		System.out.println("크롤링 완료!");


	} //spring boot 실행하고, 크롤링 로직 자동 실행
}
