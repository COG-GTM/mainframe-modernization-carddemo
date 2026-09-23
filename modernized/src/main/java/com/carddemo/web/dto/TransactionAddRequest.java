package com.carddemo.web.dto;

/**
 * COTRN02C - transaction add. Amount and timestamps are submitted as strings because the COBOL
 * map validated their exact picture ({@code -99999999.99} and {@code YYYY-MM-DD HH:MM:SS}).
 */
public record TransactionAddRequest(
        Long accountId,
        String cardNumber,
        String typeCode,
        Integer categoryCode,
        String source,
        String description,
        String amount,
        Long merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        String originTimestamp,
        String processingTimestamp) {
}
