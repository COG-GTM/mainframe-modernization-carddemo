package com.carddemo.batch.posting;

/**
 * COBOL program: CBTRN02C — daily transaction posting (JCL POSTTRAN).
 *
 * <p>Reject reason codes and messages moved into {@code WS-VALIDATION-FAIL-REASON} /
 * {@code WS-VALIDATION-FAIL-REASON-DESC} by paragraphs {@code 1500-A-LOOKUP-XREF},
 * {@code 1500-B-LOOKUP-ACCT} and {@code 2800-UPDATE-ACCOUNT-REC}. The code is written to the
 * 80-byte validation trailer of every DALYREJS record.
 */
public enum PostingRejectReason {

    /** 1500-A-LOOKUP-XREF: no CARDXREF record for DALYTRAN-CARD-NUM. */
    INVALID_CARD_NUMBER(100, "INVALID CARD NUMBER FOUND"),

    /** 1500-B-LOOKUP-ACCT: no ACCTDATA record for XREF-ACCT-ID. */
    ACCOUNT_NOT_FOUND(101, "ACCOUNT RECORD NOT FOUND"),

    /** 1500-B-LOOKUP-ACCT: ACCT-CREDIT-LIMIT &lt; cycle credit - cycle debit + DALYTRAN-AMT. */
    OVERLIMIT_TRANSACTION(102, "OVERLIMIT TRANSACTION"),

    /** 1500-B-LOOKUP-ACCT: ACCT-EXPIRAION-DATE &lt; DALYTRAN-ORIG-TS (1:10). */
    TRANSACTION_AFTER_EXPIRATION(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"),

    /** 2800-UPDATE-ACCOUNT-REC: REWRITE of the account record hit INVALID KEY. */
    ACCOUNT_REWRITE_FAILED(109, "ACCOUNT RECORD NOT FOUND");

    private final int code;
    private final String description;

    PostingRejectReason(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /** WS-VALIDATION-FAIL-REASON PIC 9(04). */
    public int getCode() {
        return code;
    }

    /** WS-VALIDATION-FAIL-REASON-DESC PIC X(76). */
    public String getDescription() {
        return description;
    }
}
