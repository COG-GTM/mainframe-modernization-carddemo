package com.carddemo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and time edit masks of copybook {@code CSDAT01Y} ({@code WS-DATE-TIME}), used by the screens
 * and the batch programs to build display and timestamp strings.
 */
public final class CardDemoDateTime {

    /** {@code WS-CURDATE} — {@code 9(04)9(02)9(02)} */
    public static final DateTimeFormatter CURDATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    /** {@code WS-CURDATE-MM-DD-YY} */
    public static final DateTimeFormatter CURDATE_MM_DD_YY = DateTimeFormatter.ofPattern("MM/dd/yy");
    /** {@code WS-CURTIME-HH-MM-SS} */
    public static final DateTimeFormatter CURTIME_HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    /** {@code WS-TIMESTAMP} — {@code yyyy-MM-dd HH:mm:ss.SSSSSS} as written to {@code TRAN-PROC-TS}. */
    public static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    /** {@code CCYY-MM-DD} dates as stored in the account, card and customer masters. */
    public static final DateTimeFormatter DATE_CCYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private CardDemoDateTime() {
    }

    public static String timestamp(LocalDateTime value) {
        return TIMESTAMP.format(value);
    }

    public static String date(LocalDate value) {
        return DATE_CCYY_MM_DD.format(value);
    }
}
