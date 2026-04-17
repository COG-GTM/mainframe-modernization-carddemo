package com.carddemo.auth.dto;

/**
 * Login response DTO containing the JWT token and user information.
 *
 * Migrated from: COSGN00C.cbl, READ-USER-SEC-FILE paragraph (lines 209-257)
 * The COMMAREA fields CDEMO-USER-ID and CDEMO-USER-TYPE are now
 * encoded as JWT claims, while user details are returned in the response body.
 * Original XCTL targets: COADM01C (admin menu), COMEN01C (regular user menu)
 */
public record LoginResponse(
        String token,
        String userId,
        String firstName,
        String lastName,
        String userType
) {
}
