package com.carddemo.signon.web;

import com.carddemo.signon.service.SignonResult;

/**
 * Signon response payload.
 *
 * @param success            whether the credentials were accepted
 * @param outcome            the {@link SignonResult.Outcome} name (parity branch)
 * @param message            error message (empty on success), verbatim from COSGN00C
 * @param destinationProgram XCTL target on success ({@code COADM01C}/{@code COMEN01C})
 * @param userId             normalized user id that was evaluated
 * @param userType           resolved user type ({@code ADMIN}/{@code USER}) or null
 */
public record SignonResponse(
        boolean success,
        String outcome,
        String message,
        String destinationProgram,
        String userId,
        String userType) {

    public static SignonResponse from(SignonResult result) {
        return new SignonResponse(
                result.isSuccess(),
                result.outcome().name(),
                result.message(),
                result.destinationProgram(),
                result.userId(),
                result.userType() == null ? null : result.userType().name());
    }
}
