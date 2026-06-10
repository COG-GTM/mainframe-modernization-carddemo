package com.carddemo.interestcalc.domain;

import java.math.BigDecimal;

/**
 * Transaction record written by the interest job.
 *
 * <p>Copybook: {@code app/cpy/CVTRA05Y.cpy} ({@code TRAN-RECORD}, RECLN = 350)
 *
 * <ul>
 *   <li>{@code transactionId}   &larr; {@code TRAN-ID            PIC X(16)}</li>
 *   <li>{@code typeCode}        &larr; {@code TRAN-TYPE-CD       PIC X(02)}</li>
 *   <li>{@code categoryCode}    &larr; {@code TRAN-CAT-CD        PIC 9(04)}</li>
 *   <li>{@code source}          &larr; {@code TRAN-SOURCE        PIC X(10)}</li>
 *   <li>{@code description}     &larr; {@code TRAN-DESC          PIC X(100)}</li>
 *   <li>{@code amount}          &larr; {@code TRAN-AMT           PIC S9(09)V99}</li>
 *   <li>{@code merchantId}      &larr; {@code TRAN-MERCHANT-ID   PIC 9(09)}</li>
 *   <li>{@code merchantName}    &larr; {@code TRAN-MERCHANT-NAME PIC X(50)}</li>
 *   <li>{@code merchantCity}    &larr; {@code TRAN-MERCHANT-CITY PIC X(50)}</li>
 *   <li>{@code merchantZip}     &larr; {@code TRAN-MERCHANT-ZIP  PIC X(10)}</li>
 *   <li>{@code cardNumber}      &larr; {@code TRAN-CARD-NUM      PIC X(16)}</li>
 *   <li>{@code originTimestamp} &larr; {@code TRAN-ORIG-TS       PIC X(26)}</li>
 *   <li>{@code processTimestamp}&larr; {@code TRAN-PROC-TS       PIC X(26)}</li>
 *   <li>(FILLER PIC X(20) not mapped)</li>
 * </ul>
 */
public record TransactionRecord(
        String transactionId,
        String typeCode,
        String categoryCode,
        String source,
        String description,
        BigDecimal amount,
        long merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        String cardNumber,
        String originTimestamp,
        String processTimestamp) {

    public TransactionRecord {
        amount = amount.setScale(2);
    }
}
