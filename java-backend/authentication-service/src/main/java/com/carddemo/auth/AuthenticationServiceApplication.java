package com.carddemo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Authentication Service Application
 * 
 * This service replaces the COSGN00C COBOL program functionality from the CardDemo
 * mainframe application. It provides JWT-based authentication for the modernized
 * CardDemo system as part of Phase 1 of the Strangler Fig Pattern migration.
 * 
 * Original COBOL Program: COSGN00C (Transaction CC00)
 * - Handles user login/logout
 * - Validates credentials against USRSEC VSAM file
 * - Routes users to appropriate menu based on user type (Admin/Regular)
 */
@SpringBootApplication
public class AuthenticationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }
}
