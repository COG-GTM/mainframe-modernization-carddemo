package com.carddemo.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User type enumeration derived from COBOL 88-level conditions in COCOM01Y.cpy.
 * CDEMO-USRTYP-ADMIN VALUE 'A' and CDEMO-USRTYP-USER VALUE 'U'.
 */
public enum UserType {

    ADMIN('A'),
    REGULAR('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    @JsonValue
    public char getCode() {
        return code;
    }

    @JsonCreator
    public static UserType fromCode(char code) {
        for (UserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown UserType code: " + code);
    }

    public static UserType fromCode(String code) {
        if (code == null || code.length() != 1) {
            throw new IllegalArgumentException("UserType code must be a single character, got: " + code);
        }
        return fromCode(code.charAt(0));
    }
}
