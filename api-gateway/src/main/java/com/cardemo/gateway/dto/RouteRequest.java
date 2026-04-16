package com.cardemo.gateway.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for program-based routing.
 * Maps the COBOL pattern of navigating via XCTL to a target program.
 */
public record RouteRequest(
        @NotBlank(message = "Target program name is required")
        String targetProgram,

        Integer programContext,

        String accountId,

        String cardNumber,

        String customerId
) {
}
