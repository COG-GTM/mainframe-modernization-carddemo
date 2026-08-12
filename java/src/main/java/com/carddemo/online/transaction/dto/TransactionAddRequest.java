package com.carddemo.online.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COTRN02C — input fields of BMS map COTRN2A (mapset COTRN02).
 *
 * <p>All fields are the raw screen strings: COTRN02C validates the characters the operator typed
 * (numeric tests, the {@code -99999999.99} amount edit mask, the {@code YYYY-MM-DD} date masks), so
 * the migration keeps them as text and reproduces the same character level checks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAddRequest {

    /** ACTIDIN PIC X(11) — account id used for the CXACAIX (account) cross reference read. */
    private String accountId;

    /** CARDNIN PIC X(16) — card number used for the CCXREF (card) cross reference read. */
    private String cardNumber;

    /** TTYPCD PIC X(02). */
    private String typeCode;

    /** TCATCD PIC X(04). */
    private String categoryCode;

    /** TRNSRC PIC X(10). */
    private String source;

    /** TDESC PIC X(60). */
    private String description;

    /** TRNAMT PIC X(12), edited as -99999999.99. */
    private String amount;

    /** TORIGDT PIC X(10), YYYY-MM-DD. */
    private String originDate;

    /** TPROCDT PIC X(10), YYYY-MM-DD. */
    private String processDate;

    /** MID PIC X(09). */
    private String merchantId;

    /** MNAME PIC X(30). */
    private String merchantName;

    /** MCITY PIC X(25). */
    private String merchantCity;

    /** MZIP PIC X(10). */
    private String merchantZip;

    /** CONFIRM PIC X(01): 'Y' commits the write, anything else re-prompts. */
    private String confirm;
}
