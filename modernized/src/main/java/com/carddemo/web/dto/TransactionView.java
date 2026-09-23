package com.carddemo.web.dto;

import java.math.BigDecimal;

/** COTRN00C / COTRN01C - transaction list row and transaction detail. */
public record TransactionView(
        String transactionId,
        String typeCode,
        Integer categoryCode,
        String source,
        String description,
        BigDecimal amount,
        Long merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        String cardNumber,
        String originTimestamp,
        String processingTimestamp) {
}
