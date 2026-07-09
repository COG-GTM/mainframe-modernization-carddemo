package com.carddemo.security;

/**
 * Successful sign-on response for {@code POST /api/auth/signon}.
 *
 * <p>Carries the authenticated user's identity plus the routing decision {@code COSGN00C} made
 * on success: {@code role} / {@code destination} / {@code program} correspond to the
 * {@code EXEC CICS XCTL} to {@code COADM01C} (admin) or {@code COMEN01C} (main menu).</p>
 *
 * @param userId      SEC-USR-ID PIC X(08)
 * @param firstName   SEC-USR-FNAME PIC X(20)
 * @param lastName    SEC-USR-LNAME PIC X(20)
 * @param userType    SEC-USR-TYPE PIC X(01) ('A' / 'U')
 * @param role        Spring Security authority (ROLE_ADMIN / ROLE_USER)
 * @param destination logical target menu (ADMIN_MENU / MAIN_MENU)
 * @param program     COBOL program the CICS XCTL transferred to (COADM01C / COMEN01C)
 */
public record SignonResponse(
        String userId,
        String firstName,
        String lastName,
        String userType,
        String role,
        String destination,
        String program) {

    static SignonResponse from(CardDemoUserDetails user) {
        CardDemoRole role = user.getRole();
        return new SignonResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                role.getTypeCode(),
                role.getAuthority(),
                role.getDestination(),
                role.getProgram());
    }
}
