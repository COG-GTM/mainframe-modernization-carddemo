package com.carddemo.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for parsing COBOL date formats used in the CardDemo application.
 *
 * <p>COBOL dates follow these patterns:
 * <ul>
 *   <li>PIC X(10) dates: YYYY-MM-DD format</li>
 *   <li>PIC X(26) timestamps: yyyy-MM-dd-HH.mm.ss.SSSSSS or ISO format</li>
 * </ul>
 */
public final class DateUtil {

    private static final DateTimeFormatter COBOL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter COBOL_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private DateUtil() {
        // utility class
    }

    /**
     * Parses a COBOL PIC X(10) date string in YYYY-MM-DD format to a LocalDate.
     *
     * @param dateStr the date string in YYYY-MM-DD format
     * @return the parsed LocalDate, or null if the input is null or blank
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), COBOL_DATE_FORMAT);
    }

    /**
     * Parses a COBOL PIC X(26) timestamp string to a LocalDateTime.
     * Supports both COBOL format (yyyy-MM-dd-HH.mm.ss.SSSSSS) and ISO format.
     *
     * @param timestampStr the timestamp string
     * @return the parsed LocalDateTime, or null if the input is null or blank
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDateTime parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isBlank()) {
            return null;
        }
        String trimmed = timestampStr.trim();
        try {
            return LocalDateTime.parse(trimmed, COBOL_TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(trimmed);
        }
    }

    /**
     * Formats a LocalDate to the COBOL PIC X(10) YYYY-MM-DD string format.
     *
     * @param date the date to format
     * @return the formatted date string, or null if the input is null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(COBOL_DATE_FORMAT);
    }

    /**
     * Formats a LocalDateTime to the COBOL PIC X(26) timestamp string format.
     *
     * @param dateTime the datetime to format
     * @return the formatted timestamp string, or null if the input is null
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(COBOL_TIMESTAMP_FORMAT);
    }
}
