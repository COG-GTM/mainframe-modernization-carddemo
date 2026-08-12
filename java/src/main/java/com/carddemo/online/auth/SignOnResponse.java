package com.carddemo.online.auth;

import com.carddemo.online.common.ScreenHeader;

/**
 * BMS map COSGN0A (mapset COSGN00) output, produced by SEND-SIGNON-SCREEN / SEND-PLAIN-TEXT of
 * COBOL program COSGN00C.
 *
 * @param header POPULATE-HEADER-INFO fields
 * @param userId USERIDO PIC X(08)
 * @param errorMessage ERRMSGO PIC X(78), the verbatim COBOL message
 * @param cursorField the field that received {@code MOVE -1 TO ...L}, i.e. where the cursor is put
 * @param nextProgram the program the CICS XCTL would transfer to, {@code null} when the signon
 *     screen is redisplayed
 * @param nextTransactionId the transaction id of {@code nextProgram}
 */
public record SignOnResponse(
        ScreenHeader header,
        String userId,
        String errorMessage,
        String cursorField,
        String nextProgram,
        String nextTransactionId) {
}
