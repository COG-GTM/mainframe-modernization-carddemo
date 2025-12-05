package com.carddemo.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Authentication Filter
 * 
 * Validates that protected routes have valid authentication.
 * 
 * In the mainframe CardDemo, authentication was handled by:
 * 1. COSGN00C validating credentials against USRSEC
 * 2. Setting CDEMO-USER-ID and CDEMO-USER-TYPE in COMMAREA
 * 3. Subsequent programs checking COMMAREA for user context
 * 
 * In the modernized system:
 * 1. Authentication Service issues JWT tokens
 * 2. This filter validates JWT presence for protected routes
 * 3. Backend services validate the full JWT token
 * 
 * Public routes (no authentication required):
 * - /api/auth/login
 * - /api/auth/validate
 * - /actuator/**
 * - /swagger-ui/**
 * - /v3/api-docs/**
 */
@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/validate",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/fallback"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Token validation is delegated to backend services
        // Gateway only checks for presence of token
        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
