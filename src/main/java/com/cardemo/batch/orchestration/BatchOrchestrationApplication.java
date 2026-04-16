package com.cardemo.batch.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for CardDemo Batch Job Orchestration.
 * Replaces JCL job scheduling with Spring Batch orchestration.
 */
@SpringBootApplication
@EnableScheduling
public class BatchOrchestrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchOrchestrationApplication.class, args);
    }
}
