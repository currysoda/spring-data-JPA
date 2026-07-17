package com.study.springdatajpa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
// 리포지토리 스캔 + 프록시 생성 범위 지정. Spring Boot는 @SpringBootApplication 패키지
// 기준으로 이걸 자동으로 해주므로 원래 생략 가능(학습용으로 명시).
@EnableJpaRepositories(basePackages = "com.study.springdatajpa")
public class AppConfig {

}
