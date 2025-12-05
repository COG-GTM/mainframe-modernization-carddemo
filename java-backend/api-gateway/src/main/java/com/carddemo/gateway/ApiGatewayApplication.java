package com.carddemo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application
 * 
 * This gateway implements the Strangler Fig Pattern for the CardDemo modernization.
 * It routes requests between the legacy mainframe system and the new Java microservices.
 * 
 * Phase 1 Routing:
 * - Authentication requests (/api/auth/**) -> Authentication Service (new Java)
 * - User management requests (/api/users/**) -> User Management Service (new Java)
 * - All other requests -> Legacy mainframe system (to be implemented in later phases)
 * 
 * The gateway provides:
 * - Request routing based on path patterns
 * - Cross-cutting concerns (logging, rate limiting, circuit breaker)
 * - Gradual migration support (route by route)
 * - Centralized entry point for all CardDemo services
 * 
 * As more services are modernized in later phases, additional routes will be added
 * to redirect traffic from the mainframe to the new Java services.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
