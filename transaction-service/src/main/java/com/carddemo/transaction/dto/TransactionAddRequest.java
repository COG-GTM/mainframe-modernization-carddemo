package com.carddemo.transaction.dto;

/**
 * Request DTO for adding a new transaction.
 * Corresponds to COTRN02C input fields.
 * All 11 data fields are required per COBOL validation logic.
 */
public record TransactionAddRequest(
        String accountId,
        String cardNumber,
        String tranTypeCd,
        String tranCatCd,
        String tranSource,
        String tranDesc,
        String tranAmt,
        String tranOrigDate,
        String tranProcDate,
        String merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        boolean confirmed
) {
}
