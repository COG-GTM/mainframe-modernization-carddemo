package com.carddemo.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for bill payment.
 *
 * Reflects the result of the bill payment saga, which modernizes
 * the atomic COBIL00C.cbl operation of writing to TRANSACT and
 * updating ACCTDAT in a single CICS unit of work.
 */
public record BillPaymentResponse(
        String transactionId,
        String accountId,
        BigDecimal amount,
        BigDecimal newBalance,
        String status,
        Instant processedAt
) {
}
