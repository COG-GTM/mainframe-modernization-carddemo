package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application for CardDemo Messaging module.
 * Replaces MQ-based account inquiry service (COACCT01/CODATE01).
 */
@SpringBootApplication(scanBasePackages = "com.carddemo")
@EntityScan(basePackages = "com.carddemo.entity")
@EnableJpaRepositories(basePackages = "com.carddemo.repository")
public class CardDemoMessagingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardDemoMessagingApplication.class, args);
    }
}
