package com.carddemo.card.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating card fields.
 *
 * Business rules from COCRDUPC.cbl:
 * - embossedName: alphabetic characters and spaces only, max 50 chars
 * - activeStatus: must be 'Y' or 'N'
 * - expirationDate: format YYYY-MM-DD, month 1-12, year 1950-2099
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardUpdateRequest {

    @Size(max = 50, message = "Embossed name must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "Card name can only contain alphabets and spaces")
    private String embossedName;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Expiration date must be in YYYY-MM-DD format")
    private String expirationDate;

    @Pattern(regexp = "^[YN]$", message = "Card active status must be Y or N")
    private String activeStatus;
}
