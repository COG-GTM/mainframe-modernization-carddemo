package com.carddemo.card.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating card details.
 * Translates the update logic from COCRDUPC.cbl which allows updating
 * embossed name, expiration date, and active status.
 */
public record CardUpdateRequest(
        @Size(max = 50, message = "Embossed name must not exceed 50 characters")
        String cardEmbossedName,

        @Size(max = 10, message = "Expiration date must not exceed 10 characters")
        String cardExpirationDate,

        @Pattern(regexp = "[YN]", message = "Active status must be Y or N")
        String cardActiveStatus
) {
}
