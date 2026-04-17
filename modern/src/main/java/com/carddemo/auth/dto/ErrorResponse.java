package com.carddemo.auth.dto;

import java.time.Instant;

/**
 * Uniform error response format for the auth service.
 *
 * Maps COBOL error messages (e.g., "Wrong Password. Try again ...",
 * "User not found. Try again ...") to structured JSON error responses
 * with HTTP status codes.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, Instant.now());
    }
}
