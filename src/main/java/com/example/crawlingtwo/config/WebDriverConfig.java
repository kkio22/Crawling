package com.example.crawlingtwo.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class WebDriverConfig {
	@Bean
	public WebDriver createDriver() {
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

		return new ChromeDriver(options);

	}
}
