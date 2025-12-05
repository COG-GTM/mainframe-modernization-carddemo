package com.carddemo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Authentication Service Application for CardDemo.
 * 
 * This service replaces the functionality of COBOL program COSGN00C (transaction CC00)
 * which handles user login/logout and validates credentials against the USRSEC VSAM file.
 * 
 * Original COBOL functionality:
 * - Validates user ID and password against USRSEC file
 * - Routes admin users to COADM01C (admin menu)
 * - Routes regular users to COMEN01C (user menu)
 * - Handles session management through CICS COMMAREA
 * 
 * Java implementation:
 * - JWT-based authentication
 * - RESTful API endpoints for login/logout/validate
 * - Integration with User Management Service for credential verification
 */
@SpringBootApplication(scanBasePackages = {"com.carddemo.auth", "com.carddemo.common"})
public class AuthenticationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }
}
