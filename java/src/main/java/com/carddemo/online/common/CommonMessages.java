package com.carddemo.online.common;

/**
 * COBOL copybooks: CSMSG01Y (CCDA-COMMON-MESSAGES) and COTTL01Y (CCDA-TITLE01/CCDA-TITLE02).
 *
 * <p>The literals are reproduced exactly as coded; the trailing blanks of the COBOL PIC X(50)
 * and PIC X(40) fields are dropped because the JSON payloads are not fixed width.
 */
public final class CommonMessages {

    /** CCDA-MSG-THANK-YOU. */
    public static final String THANK_YOU = "Thank you for using CardDemo application...";

    /** CCDA-MSG-INVALID-KEY. */
    public static final String INVALID_KEY = "Invalid key pressed. Please see below...";

    /** CCDA-TITLE01. */
    public static final String TITLE01 = "        Mainframe Modernization        ";

    /** CCDA-TITLE02. */
    public static final String TITLE02 = "              CardDemo                  ";

    private CommonMessages() {
    }
}
