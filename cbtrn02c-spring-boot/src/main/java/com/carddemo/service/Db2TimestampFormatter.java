package com.carddemo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formats a timestamp the way the COBOL Z-GET-DB2-FORMAT-TIMESTAMP paragraph does:
 * {@code YYYY-MM-DD-HH.MM.SS.mm0000} (26 chars), where {@code mm} is hundredths of a
 * second and the trailing {@code 0000} matches the COBOL MOVE '0000' TO DB2-REST.
 */
public final class Db2TimestampFormatter {

    private static final DateTimeFormatter BASE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS");

    private Db2TimestampFormatter() {
    }

    public static String format(LocalDateTime ts) {
        return BASE.format(ts) + "0000";
    }

    public static String now() {
        return format(LocalDateTime.now());
    }
}
