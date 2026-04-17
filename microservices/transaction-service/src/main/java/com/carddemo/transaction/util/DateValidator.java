package com.carddemo.transaction.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Date validation utility translating CSUTLDTC.cbl logic.
 * Validates date formats (YYYY-MM-DD) with calendar correctness checks.
 */
public final class DateValidator {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);

    private DateValidator() {
    }

    /**
     * Validates that the given string is a valid date in YYYY-MM-DD format.
     *
     * @param dateStr the date string to validate
     * @return true if the date is valid, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return false;
        }
        if (dateStr.length() != 10) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates a timestamp string. Accepts YYYY-MM-DD prefix for timestamp values
     * (the full 26-char COBOL timestamp format: YYYY-MM-DD-HH.MM.SS.FFFFFF).
     *
     * @param timestamp the timestamp string to validate
     * @return true if the date portion is valid
     */
    public static boolean isValidTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return false;
        }
        if (timestamp.length() < 10) {
            return false;
        }
        String datePart = timestamp.substring(0, 10);
        return isValidDate(datePart);
    }
}
