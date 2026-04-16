package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing user types from COBOL 88-level conditions in COCOM01Y/CSUSR01Y.
 * <pre>
 *   88 CDEMO-USRTYP-ADMIN VALUE 'A'.
 *   88 CDEMO-USRTYP-USER  VALUE 'U'.
 * </pre>
 */
public enum UserType {

    ADMIN('A'),
    USER('U');

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
