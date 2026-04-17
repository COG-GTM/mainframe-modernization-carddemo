package com.carddemo.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction data.
 *
 * COBOL Traceability: Maps the display fields from COTRN00C (list view)
 * and COTRN01C (detail view) screen maps.
 */
public record TransactionResponse(
        String transactionId,
        String typeCode,
        int categoryCode,
        String source,
        String description,
        BigDecimal amount,
        Long merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        String cardNumber,
        LocalDateTime originTimestamp,
        LocalDateTime processedTimestamp
) {
}
