package com.carddemo.transaction.dto;

import java.math.BigDecimal;

/**
 * Response DTO for bill payment result.
 */
public record BillPaymentResponse(
        String transactionId,
        BigDecimal amountPaid,
        BigDecimal newBalance,
        String message
) {
}
