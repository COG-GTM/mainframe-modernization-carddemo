package com.carddemo.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Account status enumeration derived from ACCT-ACTIVE-STATUS in CVACT01Y.cpy.
 */
public enum AccountStatus {

    ACTIVE('Y'),
    INACTIVE('N');

    private final char code;

    AccountStatus(char code) {
        this.code = code;
    }

    @JsonValue
    public char getCode() {
        return code;
    }

    @JsonCreator
    public static AccountStatus fromCode(char code) {
        for (AccountStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AccountStatus code: " + code);
    }

    public static AccountStatus fromCode(String code) {
        if (code == null || code.length() != 1) {
            throw new IllegalArgumentException("AccountStatus code must be a single character, got: " + code);
        }
        return fromCode(code.charAt(0));
    }
}
