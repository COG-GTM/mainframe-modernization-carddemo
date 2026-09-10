package com.carddemo.context;

/** {@code CDEMO-USER-TYPE} of copybook {@code COCOM01Y}. */
public enum UserType {
    ADMIN('A'),
    USER('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    public char code() {
        return code;
    }

    public static UserType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        char first = Character.toUpperCase(code.charAt(0));
        for (UserType type : values()) {
            if (type.code == first) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown user type: " + code);
    }
}
