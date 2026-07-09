package com.carddemo.session;

/**
 * User type — mirrors {@code CDEMO-USER-TYPE PIC X(01)} in copybook {@code COCOM01Y}
 * (CARDDEMO-COMMAREA) and its 88-levels.
 *
 * <ul>
 *   <li>{@code CDEMO-USRTYP-ADMIN VALUE 'A'}</li>
 *   <li>{@code CDEMO-USRTYP-USER  VALUE 'U'}</li>
 * </ul>
 */
public enum UserType {

    ADMIN('A'),
    USER('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    /** The single-character COBOL code ('A' or 'U'). */
    public char code() {
        return code;
    }

    /**
     * Resolve from the raw COBOL code. {@code null}/blank yields {@code null}
     * (mirrors an uninitialised {@code CDEMO-USER-TYPE}).
     */
    public static UserType fromCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        char c = Character.toUpperCase(value.trim().charAt(0));
        for (UserType t : values()) {
            if (t.code == c) {
                return t;
            }
        }
        return null;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
