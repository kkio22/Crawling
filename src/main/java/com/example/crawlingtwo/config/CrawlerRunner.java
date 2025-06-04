package com.example.crawlingtwo.config;



import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.crawlingtwo.service.CategoryCrawling;
import com.example.crawlingtwo.service.LectureCrawling;
import com.example.crawlingtwo.service.SubCategoryCrawling;

@Component
public class CrawlerRunner
	implements CommandLineRunner { // spring boot가 실행될 때 동시에 크롤링도 시작되어야 하기 때문에 그걸 해주는 클래스가 따로 있어야 함

	private final CategoryCrawling categoryCrawling;
	private final SubCategoryCrawling subCategoryCrawling;
	private final LectureCrawling lectureCrawling;

	public CrawlerRunner(CategoryCrawling categoryCrawling, SubCategoryCrawling subCategoryCrawling,
		LectureCrawling lectureCrawling) {
		this.categoryCrawling = categoryCrawling;
		this.subCategoryCrawling = subCategoryCrawling;
		this.lectureCrawling = lectureCrawling;
	}

	@Override // 이 매서드가 있어야 함
	public void run(String... args) throws Exception {
		System.out.println("크롤링 시작");
		categoryCrawling.getMainCategories();
		subCategoryCrawling.crawlingAll();
		lectureCrawling.subCrawlingAll();
		System.out.println("크롤링 완료!");
	} //spring boot 실행하고, 크롤링 로직 자동 실행
}
