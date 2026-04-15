package com.carddemo.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for account update requests.
 * Validation rules derived from COACTUPC.cbl:
 * - Active status must be 'Y' or 'N'
 * - Credit limit must be > 0
 * - Cash credit limit must be > 0
 * - Dates must be in YYYY-MM-DD format
 * - Group ID is alphanumeric, max 10 chars
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUpdateRequest {

    @NotNull(message = "Active status must be provided")
    @Pattern(regexp = "^[YN]$", message = "Active status must be 'Y' or 'N'")
    private String activeStatus;

    @NotNull(message = "Credit limit must be provided")
    @DecimalMin(value = "0.01", message = "Credit limit must be greater than 0")
    private BigDecimal creditLimit;

    @NotNull(message = "Cash credit limit must be provided")
    @DecimalMin(value = "0.01", message = "Cash credit limit must be greater than 0")
    private BigDecimal cashCreditLimit;

    @NotNull(message = "Open date must be provided")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Open date must be in YYYY-MM-DD format")
    private String openDate;

    @NotNull(message = "Expiration date must be provided")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Expiration date must be in YYYY-MM-DD format")
    private String expirationDate;

    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2})?$", message = "Reissue date must be in YYYY-MM-DD format")
    private String reissueDate;

    @Size(max = 10, message = "Group ID must not exceed 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Group ID must be alphanumeric")
    private String groupId;
}
