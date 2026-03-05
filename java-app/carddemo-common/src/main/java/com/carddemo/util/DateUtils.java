package com.carddemo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date utility class — replaces CSUTLDTC.cbl (CEEDAYS calls) and COBDATFT.asm.
 * Provides date validation and format conversion using java.time.
 * Explicit validation methods match COBOL behavior from CSUTLDWY.cpy exactly.
 */
public final class DateUtils {

    private DateUtils() {}

    /**
     * Validate a date string against a given format.
     * Replaces CEEDAYS date validation from CSUTLDTC.cbl.
     */
    public static boolean validateDate(String date, String format) {
        if (date == null || date.isBlank()) return false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate.parse(date.trim(), formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Convert a date from one format to another.
     * Replaces COBDATFT.asm date format conversion.
     */
    public static String convertDateFormat(String date, String fromFormat, String toFormat) {
        if (date == null || date.isBlank()) return null;
        try {
            DateTimeFormatter from = DateTimeFormatter.ofPattern(fromFormat);
            DateTimeFormatter to = DateTimeFormatter.ofPattern(toFormat);
            LocalDate parsed = LocalDate.parse(date.trim(), from);
            return parsed.format(to);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Validate month (1-12) — mirrors CSUTLDWY.cpy validation.
     */
    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    /**
     * Validate day of month — mirrors CSUTLDWY.cpy validation.
     * Handles 31-day months, 30-day months, and February (28/29).
     */
    public static boolean isValidDayOfMonth(int year, int month, int day) {
        if (day < 1) return false;

        int maxDay;
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                maxDay = 31;
                break;
            case 4: case 6: case 9: case 11:
                maxDay = 30;
                break;
            case 2:
                maxDay = isLeapYear(year) ? 29 : 28;
                break;
            default:
                return false;
        }

        return day <= maxDay;
    }

    /**
     * Check leap year — mirrors CSUTLDWY.cpy February 28/29 logic.
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Validate a full date (year, month, day) — combines all CSUTLDWY.cpy checks.
     */
    public static boolean isValidDate(int year, int month, int day) {
        if (!isValidMonth(month)) return false;
        return isValidDayOfMonth(year, month, day);
    }
}
