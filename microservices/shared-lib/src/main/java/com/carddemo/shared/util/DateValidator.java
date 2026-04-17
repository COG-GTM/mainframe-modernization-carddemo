package com.carddemo.shared.util;

import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date validation utility translated from CSUTLDTC.cbl.
 *
 * The original COBOL program uses the CEEDAYS API to validate dates.
 * This Java implementation replicates the validation logic and error reporting
 * using Java's date APIs.
 *
 * Supported formats:
 * - YYYY-MM-DD
 * - MM/DD/YYYY
 * - DD/MM/YYYY
 * - YYYYMMDD
 */
public final class DateValidator {

    private static final int SEVERITY_SUCCESS = 0;
    private static final int SEVERITY_ERROR = 1;

    private static final String MSG_VALID = "Date is valid";
    private static final String MSG_INSUFFICIENT = "Insufficient";
    private static final String MSG_DATEVALUE_ERROR = "Datevalue error";
    private static final String MSG_INVALID_MONTH = "Invalid month";
    private static final String MSG_BAD_PIC_STRING = "Bad Pic String";
    private static final String MSG_NONNUMERIC_DATA = "Nonnumeric data";
    private static final String MSG_DATE_INVALID = "Date is invalid";
    private static final String MSG_UNSUPP_RANGE = "Unsupp. Range";

    private static final Map<String, Pattern> FORMAT_PATTERNS = Map.of(
            "YYYY-MM-DD", Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$"),
            "MM/DD/YYYY", Pattern.compile("^(\\d{2})/(\\d{2})/(\\d{4})$"),
            "DD/MM/YYYY", Pattern.compile("^(\\d{2})/(\\d{2})/(\\d{4})$"),
            "YYYYMMDD", Pattern.compile("^(\\d{4})(\\d{2})(\\d{2})$")
    );

    private static final int MIN_YEAR = 1600;
    private static final int MAX_YEAR = 9999;

    private DateValidator() {
    }

    /**
     * Validates a date string against the specified format.
     *
     * @param date   the date string to validate
     * @param format the expected format (YYYY-MM-DD, MM/DD/YYYY, DD/MM/YYYY, YYYYMMDD)
     * @return a {@link DateValidationResult} containing the validation outcome
     */
    public static DateValidationResult validate(String date, String format) {
        if (date == null || date.trim().isEmpty()) {
            return error(MSG_INSUFFICIENT, date, format);
        }

        if (format == null || format.trim().isEmpty()) {
            return error(MSG_BAD_PIC_STRING, date, format);
        }

        String normalizedFormat = format.trim().toUpperCase();
        Pattern pattern = FORMAT_PATTERNS.get(normalizedFormat);

        if (pattern == null) {
            return error(MSG_BAD_PIC_STRING, date, format);
        }

        String trimmedDate = date.trim();
        Matcher matcher = pattern.matcher(trimmedDate);

        if (!matcher.matches()) {
            if (containsNonNumericData(trimmedDate, normalizedFormat)) {
                return error(MSG_NONNUMERIC_DATA, date, format);
            }
            return error(MSG_DATE_INVALID, date, format);
        }

        int year;
        int month;
        int day;

        try {
            switch (normalizedFormat) {
                case "YYYY-MM-DD":
                    year = Integer.parseInt(matcher.group(1));
                    month = Integer.parseInt(matcher.group(2));
                    day = Integer.parseInt(matcher.group(3));
                    break;
                case "MM/DD/YYYY":
                    month = Integer.parseInt(matcher.group(1));
                    day = Integer.parseInt(matcher.group(2));
                    year = Integer.parseInt(matcher.group(3));
                    break;
                case "DD/MM/YYYY":
                    day = Integer.parseInt(matcher.group(1));
                    month = Integer.parseInt(matcher.group(2));
                    year = Integer.parseInt(matcher.group(3));
                    break;
                case "YYYYMMDD":
                    year = Integer.parseInt(matcher.group(1));
                    month = Integer.parseInt(matcher.group(2));
                    day = Integer.parseInt(matcher.group(3));
                    break;
                default:
                    return error(MSG_BAD_PIC_STRING, date, format);
            }
        } catch (NumberFormatException e) {
            return error(MSG_NONNUMERIC_DATA, date, format);
        }

        if (year < MIN_YEAR || year > MAX_YEAR) {
            return error(MSG_UNSUPP_RANGE, date, format);
        }

        if (month < 1 || month > 12) {
            return error(MSG_INVALID_MONTH, date, format);
        }

        int maxDay = daysInMonth(year, month);
        if (day < 1 || day > maxDay) {
            return error(MSG_DATEVALUE_ERROR, date, format);
        }

        // Final sanity check using Java's date API
        try {
            LocalDate.of(year, month, day);
        } catch (Exception e) {
            return error(MSG_DATE_INVALID, date, format);
        }

        return new DateValidationResult(true, MSG_VALID, SEVERITY_SUCCESS, date, format);
    }

    private static boolean containsNonNumericData(String date, String format) {
        String digitsOnly;
        switch (format) {
            case "YYYY-MM-DD":
                digitsOnly = date.replace("-", "");
                break;
            case "MM/DD/YYYY":
            case "DD/MM/YYYY":
                digitsOnly = date.replace("/", "");
                break;
            case "YYYYMMDD":
                digitsOnly = date;
                break;
            default:
                return false;
        }
        return !digitsOnly.chars().allMatch(Character::isDigit);
    }

    private static int daysInMonth(int year, int month) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> isLeapYear(year) ? 29 : 28;
            default -> 0;
        };
    }

    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private static DateValidationResult error(String message, String date, String format) {
        return new DateValidationResult(false, message, SEVERITY_ERROR, date, format);
    }
}
