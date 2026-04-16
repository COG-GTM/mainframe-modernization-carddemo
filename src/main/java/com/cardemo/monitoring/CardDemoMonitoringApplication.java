package com.cardemo.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Operational Monitoring Application.
 * Provides Micrometer/Prometheus metrics for COBOL-to-Java modernized batch processing.
 */
@SpringBootApplication
public class CardDemoMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoMonitoringApplication.class, args);
    }
}
