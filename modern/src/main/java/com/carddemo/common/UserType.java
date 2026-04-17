package com.carddemo.common;

/**
 * User type enumeration corresponding to COBOL level-88 conditions
 * in {@code COCOM01Y.cpy}:
 * <pre>
 *   88 CDEMO-USRTYP-ADMIN  VALUE 'A'.
 *   88 CDEMO-USRTYP-USER   VALUE 'U'.
 * </pre>
 */
public enum UserType {

    /** Administrative user with access to user management functions. */
    ADMIN('A'),

    /** Regular user with access to account and transaction functions. */
    USER('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    /**
     * Returns the single-character COBOL code for this user type.
     *
     * @return {@code 'A'} for ADMIN, {@code 'U'} for USER
     */
    public char getCode() {
        return code;
    }

    /**
     * Resolves a {@link UserType} from the legacy single-character code.
     *
     * @param code the COBOL user-type character ({@code 'A'} or {@code 'U'})
     * @return the matching {@link UserType}
     * @throws IllegalArgumentException if the code is not recognized
     */
    public static UserType fromCode(char code) {
        for (UserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown UserType code: " + code);
    }
}
