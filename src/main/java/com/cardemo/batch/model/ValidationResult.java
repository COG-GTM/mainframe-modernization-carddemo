package com.cardemo.batch.model;

/**
 * Holds the result of transaction validation.
 * Maps to WS-VALIDATION-TRAILER in CBTRN02C.
 */
public class ValidationResult {

    private final boolean valid;
    private final int failReasonCode;
    private final String failReasonDescription;

    private ValidationResult(boolean valid, int failReasonCode, String failReasonDescription) {
        this.valid = valid;
        this.failReasonCode = failReasonCode;
        this.failReasonDescription = failReasonDescription;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, 0, "");
    }

    public static ValidationResult failure(int code, String description) {
        return new ValidationResult(false, code, description);
    }

    public boolean isValid() {
        return valid;
    }

    public int getFailReasonCode() {
        return failReasonCode;
    }

    public String getFailReasonDescription() {
        return failReasonDescription;
    }
}
