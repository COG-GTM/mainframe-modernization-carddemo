package com.carddemo.gateway.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Fallback Controller
 * 
 * Provides fallback responses when backend services are unavailable.
 * This implements the Circuit Breaker pattern for resilience.
 * 
 * In the mainframe, service unavailability would result in:
 * - CICS ABEND codes
 * - Transaction timeout messages
 * - 'Unable to verify the User ...' type messages
 * 
 * The modernized system provides graceful degradation with
 * meaningful error messages and appropriate HTTP status codes.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback for Authentication Service
     */
    @GetMapping("/auth")
    public ResponseEntity<FallbackResponse> authFallback() {
        FallbackResponse response = new FallbackResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Authentication Service Unavailable",
                "The authentication service is temporarily unavailable. Please try again later.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Fallback for User Management Service
     */
    @GetMapping("/users")
    public ResponseEntity<FallbackResponse> usersFallback() {
        FallbackResponse response = new FallbackResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "User Management Service Unavailable",
                "The user management service is temporarily unavailable. Please try again later.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Generic fallback for any service
     */
    @GetMapping("/generic")
    public ResponseEntity<FallbackResponse> genericFallback() {
        FallbackResponse response = new FallbackResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "The requested service is temporarily unavailable. Please try again later.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @Data
    @AllArgsConstructor
    public static class FallbackResponse {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
    }
}
