package com.carddemo.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for bill payment.
 *
 * Derived from COBIL00C.cbl input fields:
 * - ACTIDINI (Account ID) is provided as path parameter
 * - TRAN-AMT from CVTRA05Y.cpy (PIC S9(09)V99)
 * - TRAN-CARD-NUM from CVTRA05Y.cpy (PIC X(16))
 */
public record BillPaymentRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Card number is required")
        String cardNum
) {
}
