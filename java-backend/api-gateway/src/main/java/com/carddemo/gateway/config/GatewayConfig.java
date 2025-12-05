package com.carddemo.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway route configuration implementing the Strangler Fig Pattern.
 * 
 * The Strangler Fig Pattern allows gradual migration from mainframe to microservices:
 * 1. New requests are routed to new Java services
 * 2. Legacy requests continue to go to mainframe (via proxy)
 * 3. As more services are migrated, more routes are added
 * 4. Eventually, all traffic goes to new services
 * 
 * Phase 1 Routes:
 * - /api/auth/** -> Authentication Service (port 8081)
 *   Replaces: COSGN00C (CC00 transaction)
 * 
 * - /api/users/** -> User Management Service (port 8082)
 *   Replaces: COUSR00C (CU00), COUSR01C (CU01), COUSR02C (CU02), COUSR03C (CU03)
 * 
 * Future Phase Routes (placeholders):
 * - /api/customers/** -> Customer Service
 * - /api/accounts/** -> Account Service
 * - /api/cards/** -> Card Service
 * - /api/transactions/** -> Transaction Service
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service Routes
                // Replaces COSGN00C (CC00 transaction)
                .route("authentication-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Gateway-Source", "carddemo-gateway")
                                .addResponseHeader("X-Response-Source", "authentication-service"))
                        .uri("lb://authentication-service"))

                // User Management Service Routes
                // Replaces COUSR00C, COUSR01C, COUSR02C, COUSR03C
                .route("user-management-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Gateway-Source", "carddemo-gateway")
                                .addResponseHeader("X-Response-Source", "user-management-service"))
                        .uri("lb://user-management-service"))

                // Health check route for gateway
                .route("gateway-health", r -> r
                        .path("/gateway/health")
                        .filters(f -> f.setPath("/actuator/health"))
                        .uri("http://localhost:8080"))

                .build();
    }
}
