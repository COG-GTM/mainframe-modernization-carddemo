package com.carddemo.cardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a card.
 * Validation rules ported from COCRDUPC.cbl update logic.
 */
public record CardUpdateRequest(

        @NotBlank(message = "Card name not provided")
        @Size(max = 50, message = "Card name must be at most 50 characters")
        @Pattern(regexp = "^[a-zA-Z ]+$",
                 message = "Card name can only contain alphabets and spaces")
        String embossedName,

        @NotNull(message = "Expiration date not provided")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                 message = "Expiration date must be in YYYY-MM-DD format")
        String expirationDate,

        @NotNull(message = "Card Active Status must be Y or N")
        @Pattern(regexp = "^[YN]$",
                 message = "Card Active Status must be Y or N")
        String activeStatus
) {
}
