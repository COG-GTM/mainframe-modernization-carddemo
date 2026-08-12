package com.carddemo.online.user;

import com.carddemo.online.common.ScreenHeader;

/**
 * BMS output of maps COUSR1A, COUSR2A and COUSR3A, produced by SEND-USRADD-SCREEN,
 * SEND-USRUPD-SCREEN and SEND-USRDEL-SCREEN of COBOL programs COUSR01C, COUSR02C and COUSR03C.
 *
 * @param header POPULATE-HEADER-INFO fields
 * @param userId USERIDI / USRIDINI
 * @param firstName FNAMEI
 * @param lastName LNAMEI
 * @param password PASSWDI (COUSR03C never shows it)
 * @param userType USRTYPEI
 * @param errorMessage ERRMSGO, the verbatim COBOL message
 * @param messageColor ERRMSGC, the BMS colour attribute the program moves in
 * @param cursorField the field that received {@code MOVE -1 TO ...L}
 * @param nextProgram program of the CICS XCTL, {@code null} when the screen is redisplayed
 * @param nextTransactionId transaction id of {@code nextProgram}
 */
public record UserDetailResponse(
        ScreenHeader header,
        String userId,
        String firstName,
        String lastName,
        String password,
        String userType,
        String errorMessage,
        String messageColor,
        String cursorField,
        String nextProgram,
        String nextTransactionId) {

    /** DFHGREEN. */
    public static final String COLOR_GREEN = "GREEN";

    /** DFHRED. */
    public static final String COLOR_RED = "RED";

    /** DFHNEUTR. */
    public static final String COLOR_NEUTRAL = "NEUTRAL";
}
