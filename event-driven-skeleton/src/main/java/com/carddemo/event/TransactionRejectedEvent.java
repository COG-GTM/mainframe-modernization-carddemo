package com.carddemo.event;

/**
 * Event published when a transaction fails validation.
 *
 * Replaces: writing to DALYREJS GDG file by CBTRN02C (2500-WRITE-REJECT-REC).
 *
 * Published to the "transaction.rejected" Kafka topic.
 *
 * Validation failure codes (from CBTRN02C):
 *   100 — "INVALID CARD NUMBER FOUND" (1500-A-LOOKUP-XREF)
 *   101 — "ACCOUNT RECORD NOT FOUND" (1500-B-LOOKUP-ACCT)
 *   102 — "OVERLIMIT TRANSACTION" (credit limit exceeded)
 *   103 — "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"
 */
public record TransactionRejectedEvent(
    /** The original pending transaction that failed validation */
    TransactionPendingEvent originalTransaction,
    /** WS-VALIDATION-FAIL-REASON — numeric code (100-103) */
    int failureReasonCode,
    /** WS-VALIDATION-FAIL-REASON-DESC — human-readable description */
    String failureDescription
) {}
