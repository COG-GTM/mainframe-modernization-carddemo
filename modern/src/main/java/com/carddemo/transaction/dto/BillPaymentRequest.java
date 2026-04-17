package com.carddemo.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for bill payment.
 *
 * COBOL Traceability: Maps the input fields from COBIL00C (Txn CB00).
 * The COBOL program reads the account, looks up the card via CXACAIX,
 * creates a payment transaction, and updates the account balance atomically.
 */
public record BillPaymentRequest(
        @NotBlank(message = "Account ID is required")
        String accountId,
        String cardNumber,
        @NotNull(message = "Amount is required")
        BigDecimal amount
) {
}
