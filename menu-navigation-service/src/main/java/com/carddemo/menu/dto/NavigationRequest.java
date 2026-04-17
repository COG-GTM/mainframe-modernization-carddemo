package com.carddemo.menu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/v1/menu/navigate.
 * Corresponds to PROCESS-ENTER-KEY paragraph in COMEN01C.cbl / COADM01C.cbl,
 * where the user selects an option number.
 */
public record NavigationRequest(
        @NotBlank(message = "Session ID is required")
        String sessionId,

        @Min(value = 1, message = "Option number must be at least 1")
        @Max(value = 99, message = "Option number must be at most 99")
        int optionNumber,

        @NotBlank(message = "User type is required")
        String userType
) {
}
