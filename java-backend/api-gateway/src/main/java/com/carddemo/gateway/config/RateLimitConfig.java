package com.carddemo.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Configuration
 * 
 * Provides rate limiting capabilities for the API Gateway.
 * This is a cross-cutting concern that wasn't present in the mainframe
 * but is essential for modern API management.
 * 
 * Rate limiting helps:
 * - Protect backend services from overload
 * - Ensure fair usage across clients
 * - Prevent denial of service attacks
 */
@Configuration
public class RateLimitConfig {

    /**
     * Key resolver based on client IP address
     * Used for rate limiting per client
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown"
        );
    }

    /**
     * Key resolver based on user ID from JWT token
     * Used for rate limiting per authenticated user
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // In production, extract user ID from JWT
                // For now, use the token hash as key
                return Mono.just(String.valueOf(authHeader.hashCode()));
            }
            return Mono.just("anonymous");
        };
    }
}
