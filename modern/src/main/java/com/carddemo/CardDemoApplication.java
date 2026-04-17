package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the CardDemo modern application.
 *
 * <p>This is the Spring Boot replacement for the COBOL/CICS CardDemo
 * mainframe application, implementing a strangler-fig migration strategy.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
