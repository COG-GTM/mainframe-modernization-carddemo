package com.carddemo.online.user;

/**
 * BMS input fields of maps COUSR1A, COUSR2A and COUSR3A (mapsets COUSR01, COUSR02, COUSR03) used
 * by COBOL programs COUSR01C, COUSR02C and COUSR03C. The values map onto copybook CSUSR01Y.
 *
 * @param userId USERIDI / USRIDINI PIC X(08), SEC-USR-ID
 * @param firstName FNAMEI PIC X(20), SEC-USR-FNAME
 * @param lastName LNAMEI PIC X(20), SEC-USR-LNAME
 * @param password PASSWDI PIC X(08), SEC-USR-PWD
 * @param userType USRTYPEI PIC X(01), SEC-USR-TYPE
 */
public record UserFormRequest(
        String userId, String firstName, String lastName, String password, String userType) {
}
