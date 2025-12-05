package com.carddemo.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Management Service Application
 * 
 * This service replaces the user management COBOL programs from the CardDemo
 * mainframe application as part of Phase 1 of the Strangler Fig Pattern migration.
 * 
 * Original COBOL Programs:
 * - COUSR00C (Transaction CU00) - List all users from USRSEC file
 * - COUSR01C (Transaction CU01) - Add a new Regular/Admin user to USRSEC file
 * - COUSR02C (Transaction CU02) - Update a user in USRSEC file
 * - COUSR03C (Transaction CU03) - Delete a user from USRSEC file
 * 
 * All programs operate on the USRSEC VSAM file (Key Sequenced Data Set).
 */
@SpringBootApplication
public class UserManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplication.class, args);
    }
}
