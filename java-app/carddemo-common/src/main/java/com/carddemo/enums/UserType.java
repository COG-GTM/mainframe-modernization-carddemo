package com.carddemo.enums;

/**
 * User type enum - mirrors COBOL SEC-USR-TYPE field.
 * 'A' = Admin, 'U' = Regular User
 */
public enum UserType {
    ADMIN('A'),
    USER('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }

    public static UserType fromCode(char code) {
        for (UserType type : values()) {
            if (type.code == Character.toUpperCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown user type code: " + code);
    }
}
