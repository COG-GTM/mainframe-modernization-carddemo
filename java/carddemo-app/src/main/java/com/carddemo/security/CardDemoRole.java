package com.carddemo.security;

/**
 * Maps the {@code SEC-USR-TYPE} field of copybook {@code CSUSR01Y} to a Spring Security role
 * and the sign-on routing decision made by {@code COSGN00C}.
 *
 * <p>In {@code COSGN00C}, on a successful sign-on the {@code SEC-USR-TYPE} value determines
 * which program the transaction transfers control to via {@code EXEC CICS XCTL}: an admin
 * ({@code CDEMO-USRTYP-ADMIN}, type {@code 'A'}) is routed to {@code COADM01C} (admin menu),
 * everyone else to {@code COMEN01C} (main menu).</p>
 */
public enum CardDemoRole {

    /** SEC-USR-TYPE 'A' — administrator; routed to the admin menu ({@code COADM01C}). */
    ADMIN("A", "ROLE_ADMIN", "COADM01C", "ADMIN_MENU"),

    /** SEC-USR-TYPE 'U' (or any non-admin value) — regular user; routed to the main menu. */
    USER("U", "ROLE_USER", "COMEN01C", "MAIN_MENU");

    private final String typeCode;
    private final String authority;
    private final String program;
    private final String destination;

    CardDemoRole(String typeCode, String authority, String program, String destination) {
        this.typeCode = typeCode;
        this.authority = authority;
        this.program = program;
        this.destination = destination;
    }

    /** SEC-USR-TYPE code ({@code 'A'} / {@code 'U'}). */
    public String getTypeCode() {
        return typeCode;
    }

    /** Spring Security authority name ({@code ROLE_ADMIN} / {@code ROLE_USER}). */
    public String getAuthority() {
        return authority;
    }

    /** COBOL program the CICS {@code XCTL} transfers to on success. */
    public String getProgram() {
        return program;
    }

    /** Logical destination menu exposed to callers ({@code ADMIN_MENU} / {@code MAIN_MENU}). */
    public String getDestination() {
        return destination;
    }

    /**
     * Resolves the role from a {@code SEC-USR-TYPE} value. Mirrors the {@code COSGN00C}
     * routing: only {@code 'A'} (case-insensitive) is treated as admin; every other value —
     * including {@code 'U'} and any unexpected code — is treated as a regular user, matching
     * the COBOL {@code IF CDEMO-USRTYP-ADMIN … ELSE …} fall-through.
     */
    public static CardDemoRole fromTypeCode(String type) {
        if (type != null && ADMIN.typeCode.equalsIgnoreCase(type.trim())) {
            return ADMIN;
        }
        return USER;
    }
}
