package com.cardemo.gateway.dto;

import java.time.Instant;

/**
 * Standard error response DTO for the API Gateway.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now());
    }
}
