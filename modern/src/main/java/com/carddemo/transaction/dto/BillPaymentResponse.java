package com.carddemo.transaction.dto;

import java.math.BigDecimal;

/**
 * Response DTO for bill payment result.
 *
 * COBOL Traceability: Maps the output displayed by COBIL00C after
 * successful payment processing.
 */
public record BillPaymentResponse(
        String transactionId,
        String accountId,
        BigDecimal amountPaid,
        BigDecimal previousBalance,
        BigDecimal newBalance,
        String message
) {
}
