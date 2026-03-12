package com.carddemo.posttran.model;

/**
 * Validation fail reason codes matching COBOL WS-VALIDATION-FAIL-REASON values exactly.
 *
 * <pre>
 * 0   = No failure (valid)
 * 100 = Invalid card number (XREF lookup failed)
 * 101 = Account record not found
 * 102 = Overlimit transaction
 * 103 = Transaction received after account expiration
 * </pre>
 */
public enum ValidationFailReason {

    NONE(0, ""),
    INVALID_CARD_NUMBER(100, "INVALID CARD NUMBER FOUND"),
    ACCOUNT_NOT_FOUND(101, "ACCOUNT RECORD NOT FOUND"),
    OVER_LIMIT(102, "OVERLIMIT TRANSACTION"),
    ACCOUNT_EXPIRED(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");

    private final int code;
    private final String description;

    ValidationFailReason(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
