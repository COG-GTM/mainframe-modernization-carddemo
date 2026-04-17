package com.carddemo.event;

import java.math.BigDecimal;

/**
 * Event published after a transaction is successfully posted.
 *
 * Replaces the implicit data coupling where CREASTMT.JCL reads the
 * TRANSACT VSAM KSDS file after POSTTRAN.jcl completes.
 *
 * Published to the "transaction.posted" Kafka topic by
 * TransactionPostingService after successful validation and persistence.
 *
 * Consumed by TransactionPostedListener for statement generation,
 * eliminating the need for the SORT + REPRO JCL steps.
 */
public record TransactionPostedEvent(
    /** TRAN-ID — unique transaction identifier */
    String transactionId,
    /** TRAN-TYPE-CD — transaction type code */
    String typeCd,
    /** TRAN-CAT-CD — transaction category code */
    int catCd,
    /** TRAN-SOURCE — originating source */
    String source,
    /** TRAN-DESC — transaction description */
    String description,
    /** TRAN-AMT — transaction amount */
    BigDecimal amount,
    /** TRAN-MERCHANT-ID */
    long merchantId,
    /** TRAN-MERCHANT-NAME */
    String merchantName,
    /** TRAN-MERCHANT-CITY */
    String merchantCity,
    /** TRAN-MERCHANT-ZIP */
    String merchantZip,
    /** TRAN-CARD-NUM — card number for statement grouping */
    String cardNumber,
    /** TRAN-ORIG-TS — origination timestamp */
    String originTimestamp,
    /** TRAN-PROC-TS — processing timestamp set during posting */
    String processTimestamp,
    /** From XREF lookup — account owning this card */
    long accountId,
    /** From XREF lookup — customer owning this card */
    long customerId
) {}
