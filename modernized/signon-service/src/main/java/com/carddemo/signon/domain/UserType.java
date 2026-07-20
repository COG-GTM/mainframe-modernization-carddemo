package com.carddemo.signon.domain;

/**
 * User classification carried in {@code SEC-USR-TYPE} (copybook
 * {@code CSUSR01Y}, PIC X(01)).
 *
 * <p>Mirrors the CICS 88-levels in {@code COCOM01Y}:
 * <pre>
 *   88 CDEMO-USRTYP-ADMIN VALUE 'A'.
 *   88 CDEMO-USRTYP-USER  VALUE 'U'.
 * </pre>
 * The single-character code is the faithful persisted value; this enum only
 * adds type-safe interpretation for the business logic.
 */
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

    /**
     * Resolve a stored {@code SEC-USR-TYPE} code. Any value that is not the
     * admin marker {@code 'A'} is treated as a regular user, matching the
     * {@code IF CDEMO-USRTYP-ADMIN ... ELSE ...} fallthrough in
     * {@code COSGN00C}.
     */
    public static UserType fromCode(String rawCode) {
        if (rawCode != null && !rawCode.isEmpty() && rawCode.charAt(0) == ADMIN.code) {
            return ADMIN;
        }
        return USER;
    }
}
