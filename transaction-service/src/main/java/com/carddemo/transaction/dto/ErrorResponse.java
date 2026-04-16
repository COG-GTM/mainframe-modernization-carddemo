package com.carddemo.transaction.dto;

import java.time.LocalDateTime;

/**
 * Standard error response preserving COBOL error messages character-for-character.
 */
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now());
    }
}
