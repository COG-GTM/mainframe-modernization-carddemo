package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing card active status.
 * Derived from CARD-ACTIVE-STATUS PIC X(01) in CVACT02Y.
 */
public enum CardActiveStatus {

    ACTIVE('Y'),
    INACTIVE('N');

    private final char code;

    CardActiveStatus(char code) {
        this.code = code;
    }

    @JsonValue
    public char getCode() {
        return code;
    }

    @JsonCreator
    public static CardActiveStatus fromCode(char code) {
        for (CardActiveStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CardActiveStatus code: " + code);
    }

    public static CardActiveStatus fromCode(String code) {
        if (code == null || code.length() != 1) {
            throw new IllegalArgumentException(
                    "CardActiveStatus code must be a single character, got: " + code);
        }
        return fromCode(code.charAt(0));
    }
}
