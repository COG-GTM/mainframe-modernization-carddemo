package com.carddemo.transaction.dto;

/**
 * Request DTO for bill payment.
 * Corresponds to COBIL00C input: account ID and confirmation flag.
 */
public record BillPaymentRequest(
        String accountId,
        boolean confirmed
) {
}
