package com.carddemo.service;

/**
 * Outcome of validating (and possibly posting) a single daily transaction.
 * Mirrors the COBOL WS-VALIDATION-FAIL-REASON / WS-VALIDATION-FAIL-REASON-DESC pair:
 * a reason code of 0 means the transaction passed all validations and was posted.
 */
public class ValidationResult {

    private final int failReasonCode;
    private final String failReasonDesc;

    private ValidationResult(int failReasonCode, String failReasonDesc) {
        this.failReasonCode = failReasonCode;
        this.failReasonDesc = failReasonDesc;
    }

    public static ValidationResult ok() {
        return new ValidationResult(0, "");
    }

    public static ValidationResult rejected(int failReasonCode, String failReasonDesc) {
        return new ValidationResult(failReasonCode, failReasonDesc);
    }

    public boolean isValid() {
        return failReasonCode == 0;
    }

    public boolean isRejected() {
        return failReasonCode != 0;
    }

    public int getFailReasonCode() {
        return failReasonCode;
    }

    public String getFailReasonDesc() {
        return failReasonDesc;
    }
}
