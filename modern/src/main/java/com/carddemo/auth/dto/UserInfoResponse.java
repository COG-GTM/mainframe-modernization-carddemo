package com.carddemo.auth.dto;

/**
 * Response DTO for the /auth/me endpoint returning current user info from the JWT token.
 *
 * Migrated from: COSGN00C.cbl COMMAREA fields
 * Original COMMAREA fields: CDEMO-USER-ID, CDEMO-USER-TYPE
 * VSAM file: USRSEC (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS)
 */
public record UserInfoResponse(
        String userId,
        String firstName,
        String lastName,
        String userType
) {
}
