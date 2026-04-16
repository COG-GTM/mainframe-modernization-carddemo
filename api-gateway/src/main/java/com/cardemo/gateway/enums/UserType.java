package com.cardemo.gateway.enums;

/**
 * Maps CDEMO-USER-TYPE from COCOM01Y.cpy.
 * Level-88 conditions: CDEMO-USRTYP-ADMIN VALUE 'A', CDEMO-USRTYP-USER VALUE 'U'.
 */
public enum UserType {
    ADMIN("A"),
    USER("U");

    private final String code;

    UserType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static UserType fromCode(String code) {
        for (UserType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown user type code: " + code);
    }
}
