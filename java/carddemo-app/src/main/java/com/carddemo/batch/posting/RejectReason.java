package com.carddemo.batch.posting;

/**
 * Validation failure reasons produced by {@code CBTRN02C} during daily transaction posting.
 *
 * <p>Each constant mirrors a {@code WS-VALIDATION-FAIL-REASON} code and the exact
 * {@code WS-VALIDATION-FAIL-REASON-DESC} literal moved alongside it in the COBOL program
 * (paragraphs {@code 1500-A-LOOKUP-XREF}, {@code 1500-B-LOOKUP-ACCT} and
 * {@code 2800-UPDATE-ACCOUNT-REC}). The numeric code and description are written to the
 * reject store so downstream reconciliation matches the legacy DALYREJS trailer.</p>
 */
public enum RejectReason {

    /** 100 — XREF read (INVALID KEY) in {@code 1500-A-LOOKUP-XREF}. */
    INVALID_CARD_NUMBER(100, "INVALID CARD NUMBER FOUND"),

    /** 101 — ACCOUNT read (INVALID KEY) in {@code 1500-B-LOOKUP-ACCT}. */
    ACCOUNT_NOT_FOUND(101, "ACCOUNT RECORD NOT FOUND"),

    /** 102 — {@code ACCT-CREDIT-LIMIT < WS-TEMP-BAL} in {@code 1500-B-LOOKUP-ACCT}. */
    OVERLIMIT(102, "OVERLIMIT TRANSACTION"),

    /** 103 — {@code ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10)} in {@code 1500-B-LOOKUP-ACCT}. */
    AFTER_EXPIRATION(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");

    private final int code;
    private final String description;

    RejectReason(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /** Numeric fail reason (WS-VALIDATION-FAIL-REASON PIC 9(04)). */
    public int code() {
        return code;
    }

    /** Fail reason description (WS-VALIDATION-FAIL-REASON-DESC PIC X(76)). */
    public String description() {
        return description;
    }
}
