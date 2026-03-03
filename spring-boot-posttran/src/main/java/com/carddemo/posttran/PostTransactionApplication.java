package com.carddemo.posttran;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot application entry point for the CardDemo Post Transaction batch job.
 * Migrated from mainframe COBOL program CBTRN02C.
 */
@SpringBootApplication
@EnableScheduling
public class PostTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostTransactionApplication.class, args);
    }
}
