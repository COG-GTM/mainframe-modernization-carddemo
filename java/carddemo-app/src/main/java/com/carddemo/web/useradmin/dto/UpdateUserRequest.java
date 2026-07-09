package com.carddemo.web.useradmin.dto;

/**
 * Body of {@code PUT /api/admin/users/{userId}} — the {@code COUSR02C} updatable fields
 * ({@code FNAMEI}, {@code LNAMEI}, {@code PASSWDI}, {@code USRTYPEI}). The user id is the VSAM
 * key and comes from the path (it is never modified, matching {@code COUSR02C}).
 *
 * @param firstName SEC-USR-FNAME PIC X(20)
 * @param lastName  SEC-USR-LNAME PIC X(20)
 * @param password  SEC-USR-PWD PIC X(08) — legacy plaintext (stored {@code {noop}}-compatible)
 * @param userType  SEC-USR-TYPE PIC X(01) ('A' / 'U')
 */
public record UpdateUserRequest(
        String firstName,
        String lastName,
        String password,
        String userType) {
}
