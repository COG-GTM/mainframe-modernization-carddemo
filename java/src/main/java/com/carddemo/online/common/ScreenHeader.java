package com.carddemo.online.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * COBOL paragraph: POPULATE-HEADER-INFO, shared by COSGN00C, COMEN01C, COADM01C, COUSR00C,
 * COUSR01C, COUSR02C and COUSR03C. Copybooks: COTTL01Y (titles), CSDAT01Y (WS-CURDATE-MM-DD-YY,
 * WS-CURTIME-HH-MM-SS).
 *
 * @param title01 CCDA-TITLE01
 * @param title02 CCDA-TITLE02
 * @param transactionName WS-TRANID moved to TRNNAMEO
 * @param programName WS-PGMNAME moved to PGMNAMEO
 * @param currentDate WS-CURDATE-MM-DD-YY (MM/DD/YY)
 * @param currentTime WS-CURTIME-HH-MM-SS (HH:MM:SS)
 */
public record ScreenHeader(
        String title01,
        String title02,
        String transactionName,
        String programName,
        String currentDate,
        String currentTime) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static ScreenHeader of(String transactionId, String programName) {
        return of(transactionId, programName, LocalDateTime.now());
    }

    public static ScreenHeader of(String transactionId, String programName, LocalDateTime now) {
        return new ScreenHeader(
                CommonMessages.TITLE01,
                CommonMessages.TITLE02,
                transactionId,
                programName,
                DATE_FORMAT.format(now),
                TIME_FORMAT.format(now));
    }
}
