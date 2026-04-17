package com.carddemo.card.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for card update operations.
 *
 * Migrated from: COCRDUPC.cbl (Credit Card Update — CCUP)
 * Only updatable fields are exposed; cardNumber is immutable (PK).
 * accountId and cvvCode are not updatable per original COBOL program behavior.
 */
public record CardUpdateRequest(
        @Size(max = 50, message = "Embossed name must not exceed 50 characters")
        String embossedName,

        @Pattern(regexp = "\\d{2}-\\d{2}-\\d{4}", message = "Expiration date must be in MM-DD-YYYY format")
        String expirationDate,

        @Pattern(regexp = "[YN]", message = "Active status must be 'Y' or 'N'")
        String activeStatus
) {
}
