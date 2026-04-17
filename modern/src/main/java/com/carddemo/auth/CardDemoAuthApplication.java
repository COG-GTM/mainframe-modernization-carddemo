package com.carddemo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Auth Service - Phase 1 Migration.
 *
 * Migrated from: COSGN00C.cbl (Signon Screen for the CardDemo Application)
 * Original CICS Transaction: CC00
 * This service replaces the COBOL sign-on program with a REST-based
 * JWT authentication service.
 */
@SpringBootApplication
public class CardDemoAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoAuthApplication.class, args);
    }
}
