package com.carddemo.event;

import java.math.BigDecimal;

/**
 * Event representing a pending daily transaction awaiting posting.
 *
 * Replaces: reading records from the DALYTRAN sequential flat file
 * (AWS.M2.CARDDEMO.DALYTRAN.PS) as defined by CVTRA06Y.cpy.
 *
 * Published by an external system or file-ingestion adapter to the
 * "transaction.pending" Kafka topic (or SQS queue).
 *
 * Consumed by TransactionPendingListener, which delegates to
 * TransactionPostingService for validation and posting.
 */
public record TransactionPendingEvent(
    /** DALYTRAN-ID PIC X(16) — unique transaction identifier */
    String transactionId,
    /** DALYTRAN-TYPE-CD PIC X(02) — transaction type code */
    String typeCd,
    /** DALYTRAN-CAT-CD PIC 9(04) — transaction category code */
    int catCd,
    /** DALYTRAN-SOURCE PIC X(10) — originating source system */
    String source,
    /** DALYTRAN-DESC PIC X(100) — transaction description */
    String description,
    /** DALYTRAN-AMT PIC S9(09)V99 — transaction amount (BigDecimal for fixed-point fidelity) */
    BigDecimal amount,
    /** DALYTRAN-MERCHANT-ID PIC 9(09) */
    long merchantId,
    /** DALYTRAN-MERCHANT-NAME PIC X(50) */
    String merchantName,
    /** DALYTRAN-MERCHANT-CITY PIC X(50) */
    String merchantCity,
    /** DALYTRAN-MERCHANT-ZIP PIC X(10) */
    String merchantZip,
    /** DALYTRAN-CARD-NUM PIC X(16) — card number for XREF lookup */
    String cardNumber,
    /** DALYTRAN-ORIG-TS PIC X(26) — origination timestamp */
    String originTimestamp,
    /** DALYTRAN-PROC-TS PIC X(26) — processing timestamp */
    String processTimestamp
) {}
