package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CardDemo data-layer migration application.
 *
 * <p>This Spring Boot project is the modernization target for the mainframe
 * CardDemo COBOL/VSAM entities. Each entity (Account, Card, Customer,
 * Transaction, ...) is migrated as a JPA entity + Spring Data repository with
 * Flyway-managed schema and seed data.</p>
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
