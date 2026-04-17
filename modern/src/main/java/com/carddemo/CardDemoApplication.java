package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the CardDemo Modern application.
 * <p>
 * Migrated from: COBOL/CICS CardDemo mainframe application
 * This Spring Boot application replaces the legacy CICS transaction server,
 * exposing mainframe business logic as REST APIs.
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
