package com.carddemo.online.user;

/**
 * One line of BMS map COUSR0A (mapset COUSR00) as filled by POPULATE-USER-DATA of COBOL program
 * COUSR00C: SELnnnnI, USRIDnnI, FNAMEnnI, LNAMEnnI and UTYPEnnI.
 *
 * @param selection SELnnnnI PIC X(01), the action typed by the operator ('U' or 'D')
 * @param userId USRIDnnI, SEC-USR-ID of copybook CSUSR01Y
 * @param firstName FNAMEnnI, SEC-USR-FNAME
 * @param lastName LNAMEnnI, SEC-USR-LNAME
 * @param userType UTYPEnnI, SEC-USR-TYPE
 */
public record UserRow(
        String selection, String userId, String firstName, String lastName, String userType) {
}
