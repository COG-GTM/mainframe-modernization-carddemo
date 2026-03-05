package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application for CardDemo Online module.
 * Replaces CICS transaction server for online operations.
 */
@SpringBootApplication(scanBasePackages = "com.carddemo")
@EntityScan(basePackages = "com.carddemo.entity")
@EnableJpaRepositories(basePackages = "com.carddemo.repository")
public class CardDemoOnlineApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardDemoOnlineApplication.class, args);
    }
}
