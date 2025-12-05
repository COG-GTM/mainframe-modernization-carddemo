package com.carddemo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application for CardDemo.
 * 
 * Implements the Strangler Fig Pattern for gradual mainframe migration:
 * 
 * Phase 1 (Current):
 * - Routes /api/auth/** to Authentication Service (replaces COSGN00C)
 * - Routes /api/users/** to User Management Service (replaces COUSR00C/01C/02C/03C)
 * 
 * Future Phases:
 * - Phase 2: Customer and Account services
 * - Phase 3: Card Management services
 * - Phase 4: Transaction services
 * - Phase 5: Batch processing services
 * 
 * The gateway acts as a facade, allowing incremental migration while
 * maintaining a consistent API for clients. Legacy mainframe endpoints
 * can be gradually replaced without disrupting existing integrations.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
