package com.carddemo.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Management Service Application for CardDemo.
 * 
 * This service replaces the functionality of COBOL programs:
 * - COUSR00C (CU00) - List all users from USRSEC file
 * - COUSR01C (CU01) - Add a new Regular/Admin user to USRSEC file
 * - COUSR02C (CU02) - Update a user in USRSEC file
 * - COUSR03C (CU03) - Delete a user from USRSEC file
 * 
 * Original COBOL functionality:
 * - CRUD operations on USRSEC VSAM file
 * - Paginated user listing (10 records per page)
 * - User selection for update (U) or delete (D)
 * - Validation of required fields
 * 
 * Java implementation:
 * - RESTful API endpoints for user management
 * - JPA/PostgreSQL for data persistence
 * - Spring Security for authorization
 */
@SpringBootApplication(scanBasePackages = {"com.carddemo.user", "com.carddemo.common"})
public class UserManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplication.class, args);
    }
}
