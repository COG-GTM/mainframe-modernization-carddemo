package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application for CardDemo Batch module.
 * Replaces JCL batch job scheduling for COBOL batch programs.
 */
@SpringBootApplication(scanBasePackages = "com.carddemo")
@EntityScan(basePackages = "com.carddemo.entity")
@EnableJpaRepositories(basePackages = "com.carddemo.repository")
public class CardDemoBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardDemoBatchApplication.class, args);
    }
}
