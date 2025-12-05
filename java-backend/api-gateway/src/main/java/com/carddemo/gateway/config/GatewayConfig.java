package com.carddemo.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Route Configuration
 * 
 * Implements the Strangler Fig Pattern routing for CardDemo modernization.
 * 
 * Phase 1 Routes:
 * - /api/auth/** -> Authentication Service (replaces COSGN00C)
 * - /api/users/** -> User Management Service (replaces COUSR00C-COUSR03C)
 * 
 * Future Phases (to be added):
 * - /api/customers/** -> Customer Service (replaces customer programs)
 * - /api/accounts/** -> Account Service (replaces account programs)
 * - /api/cards/** -> Card Service (replaces card programs)
 * - /api/transactions/** -> Transaction Service (replaces transaction programs)
 * 
 * Legacy Fallback:
 * - All unmatched routes can be configured to forward to mainframe proxy
 *   (when mainframe connectivity is established)
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service Routes (Phase 1)
                // Replaces COSGN00C (Transaction CC00)
                .route("authentication-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Gateway-Source", "carddemo-gateway")
                                .circuitBreaker(config -> config
                                        .setName("authCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://authentication-service"))

                // User Management Service Routes (Phase 1)
                // Replaces COUSR00C, COUSR01C, COUSR02C, COUSR03C
                .route("user-management-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Gateway-Source", "carddemo-gateway")
                                .circuitBreaker(config -> config
                                        .setName("userCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://user-management-service"))

                // Health check route for gateway itself
                .route("gateway-health", r -> r
                        .path("/gateway/health")
                        .filters(f -> f.setPath("/actuator/health"))
                        .uri("http://localhost:${server.port:8080}"))

                .build();
    }
}
