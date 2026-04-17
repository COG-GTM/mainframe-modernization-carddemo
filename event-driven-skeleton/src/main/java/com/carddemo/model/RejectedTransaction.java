package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Rejected transaction record.
 *
 * Replaces: DALYREJS GDG output written by CBTRN02C (2500-WRITE-REJECT-REC).
 * The COBOL program appends the original DALYTRAN-RECORD (350 bytes) plus
 * a validation failure trailer (reason code + description) to the reject file.
 *
 * In the event-driven architecture, rejected transactions are published
 * to the "transaction.rejected" topic instead of writing to a GDG file.
 */
public record RejectedTransaction(
    String transactionId,
    String cardNumber,
    BigDecimal amount,
    int failureReasonCode,
    String failureDescription
) {}
