package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application for CardDemo Authorization module.
 * Replaces IMS/DB2/MQ authorization processing.
 */
@SpringBootApplication(scanBasePackages = "com.carddemo")
@EntityScan(basePackages = "com.carddemo.entity")
@EnableJpaRepositories(basePackages = "com.carddemo.repository")
public class CardDemoAuthorizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardDemoAuthorizationApplication.class, args);
    }
}
