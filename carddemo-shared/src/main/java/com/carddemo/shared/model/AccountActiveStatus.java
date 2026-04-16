package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing account active status.
 * Derived from ACCT-ACTIVE-STATUS PIC X(01) in CVACT01Y.
 */
public enum AccountActiveStatus {

    ACTIVE('Y'),
    INACTIVE('N');

    private final char code;

    AccountActiveStatus(char code) {
        this.code = code;
    }

    @JsonValue
    public char getCode() {
        return code;
    }

    @JsonCreator
    public static AccountActiveStatus fromCode(char code) {
        for (AccountActiveStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AccountActiveStatus code: " + code);
    }

    public static AccountActiveStatus fromCode(String code) {
        if (code == null || code.length() != 1) {
            throw new IllegalArgumentException(
                    "AccountActiveStatus code must be a single character, got: " + code);
        }
        return fromCode(code.charAt(0));
    }
}
