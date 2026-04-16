package com.cardemo.utility.service;

import com.cardemo.utility.model.DateValidationResult;
import com.cardemo.utility.model.DateValidationResult.FlagStatus;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Set;

/**
 * Port of CSUTLDTC date validation utility and associated copybooks
 * (CSUTLDWY, CSUTLDPY) to a Java service class.
 *
 * <p>Maps COBOL paragraphs to Java methods:
 * <ul>
 *   <li>EDIT-DATE-CCYYMMDD  -> {@link #editDateCcyymmdd(String, String)}</li>
 *   <li>EDIT-YEAR-CCYY      -> {@link #editYearCcyy(String, String, DateValidationResult)}</li>
 *   <li>EDIT-MONTH           -> {@link #editMonth(String, String, DateValidationResult)}</li>
 *   <li>EDIT-DAY             -> {@link #editDay(String, String, DateValidationResult)}</li>
 *   <li>EDIT-DAY-MONTH-YEAR  -> {@link #editDayMonthYear(String, int, int, int, DateValidationResult)}</li>
 *   <li>EDIT-DATE-LE         -> {@link #editDateLe(String, String, DateValidationResult)}</li>
 *   <li>EDIT-DATE-OF-BIRTH   -> {@link #editDateOfBirth(String, String, DateValidationResult)}</li>
 * </ul>
 */
@Service
public class DateValidationService {

    private static final Set<Integer> THIRTY_ONE_DAY_MONTHS = Set.of(1, 3, 5, 7, 8, 10, 12);
    private static final int FEBRUARY = 2;

    private static final DateTimeFormatter STRICT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Validates a date string in CCYYMMDD format.
     * Maps to EDIT-DATE-CCYYMMDD paragraph in CSUTLDPY.cpy.
     *
     * @param ccyymmdd  the date string (8 characters, CCYYMMDD)
     * @param fieldName the field name used in error messages (WS-EDIT-VARIABLE-NAME)
     * @return the validation result
     */
    public DateValidationResult editDateCcyymmdd(String ccyymmdd, String fieldName) {
        DateValidationResult result = new DateValidationResult();

        String dateStr = normalizeInput(ccyymmdd, 8);
        String yearStr = dateStr.substring(0, 4);
        String monthStr = dateStr.substring(4, 6);
        String dayStr = dateStr.substring(6, 8);

        // PERFORM EDIT-YEAR-CCYY
        if (!editYearCcyy(fieldName, yearStr, result)) {
            return result;
        }

        // PERFORM EDIT-MONTH
        if (!editMonth(fieldName, monthStr, result)) {
            return result;
        }

        // PERFORM EDIT-DAY
        if (!editDay(fieldName, dayStr, result)) {
            return result;
        }

        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);
        int day = Integer.parseInt(dayStr);

        // PERFORM EDIT-DAY-MONTH-YEAR
        if (!editDayMonthYear(fieldName, year, month, day, result)) {
            return result;
        }

        // PERFORM EDIT-DATE-LE (final validation via CEEDAYS equivalent)
        editDateLe(fieldName, dateStr, result);

        return result;
    }

    /**
     * Validates a date of birth: performs full date validation, then checks
     * that the date is not in the future.
     * Maps to EDIT-DATE-OF-BIRTH paragraph in CSUTLDPY.cpy.
     *
     * @param ccyymmdd  the date string (8 characters, CCYYMMDD)
     * @param fieldName the field name used in error messages
     * @return the validation result
     */
    public DateValidationResult editDateOfBirth(String ccyymmdd, String fieldName) {
        DateValidationResult result = editDateCcyymmdd(ccyymmdd, fieldName);

        if (!result.isValid()) {
            return result;
        }

        return editDateOfBirth(fieldName, ccyymmdd, result);
    }

    /**
     * Validates the year portion (CCYY).
     * Maps to EDIT-YEAR-CCYY paragraph in CSUTLDPY.cpy.
     *
     * @return true if year is valid and processing should continue
     */
    boolean editYearCcyy(String fieldName, String yearStr, DateValidationResult result) {
        result.setYearFlag(FlagStatus.NOT_OK);

        // Not supplied: blank or null
        if (yearStr == null || yearStr.isBlank()) {
            result.setYearFlag(FlagStatus.BLANK);
            result.setErrorMessage(fieldName + " : Year must be supplied.");
            return false;
        }

        // Not numeric
        if (!yearStr.matches("\\d{4}")) {
            result.setYearFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + " must be 4 digit number.");
            return false;
        }

        // Century not reasonable: must be 19 or 20
        int century = Integer.parseInt(yearStr.substring(0, 2));
        if (century != 19 && century != 20) {
            result.setYearFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + " : Century is not valid.");
            return false;
        }

        result.setYearFlag(FlagStatus.VALID);
        return true;
    }

    /**
     * Validates the month portion.
     * Maps to EDIT-MONTH paragraph in CSUTLDPY.cpy.
     *
     * @return true if month is valid and processing should continue
     */
    boolean editMonth(String fieldName, String monthStr, DateValidationResult result) {
        result.setMonthFlag(FlagStatus.NOT_OK);

        // Not supplied
        if (monthStr == null || monthStr.isBlank()) {
            result.setMonthFlag(FlagStatus.BLANK);
            result.setErrorMessage(fieldName + " : Month must be supplied.");
            return false;
        }

        // Not numeric — check first, matching COBOL TEST-NUMVAL logic
        if (!monthStr.matches("\\d+")) {
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + ": Month must be a number between 1 and 12.");
            return false;
        }

        int month = Integer.parseInt(monthStr);
        // WS-VALID-MONTH: values 1 through 12
        if (month < 1 || month > 12) {
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + ": Month must be a number between 1 and 12.");
            return false;
        }

        result.setMonthFlag(FlagStatus.VALID);
        return true;
    }

    /**
     * Validates the day portion.
     * Maps to EDIT-DAY paragraph in CSUTLDPY.cpy.
     *
     * @return true if day is valid and processing should continue
     */
    boolean editDay(String fieldName, String dayStr, DateValidationResult result) {
        result.setDayFlag(FlagStatus.VALID);

        // Not supplied
        if (dayStr == null || dayStr.isBlank()) {
            result.setDayFlag(FlagStatus.BLANK);
            result.setErrorMessage(fieldName + " : Day must be supplied.");
            return false;
        }

        // Not numeric
        if (!dayStr.matches("\\d+")) {
            result.setDayFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + ":day must be a number between 1 and 31.");
            return false;
        }

        int day = Integer.parseInt(dayStr);
        // WS-VALID-DAY: values 1 through 31
        if (day < 1 || day > 31) {
            result.setDayFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + ":day must be a number between 1 and 31.");
            return false;
        }

        result.setDayFlag(FlagStatus.VALID);
        return true;
    }

    /**
     * Cross-validates day/month/year combinations.
     * Maps to EDIT-DAY-MONTH-YEAR paragraph in CSUTLDPY.cpy.
     *
     * @return true if the combination is valid and processing should continue
     */
    boolean editDayMonthYear(String fieldName, int year, int month, int day,
                             DateValidationResult result) {
        // 31-day check: if not a 31-day month and day is 31
        if (!THIRTY_ONE_DAY_MONTHS.contains(month) && day == 31) {
            result.setDayFlag(FlagStatus.NOT_OK);
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + ":Cannot have 31 days in this month.");
            return false;
        }

        // February 30 check
        if (month == FEBRUARY && day == 30) {
            result.setDayFlag(FlagStatus.NOT_OK);
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + ":Cannot have 30 days in this month.");
            return false;
        }

        // Leap year check for Feb 29
        // COBOL logic from CSUTLDPY.cpy lines 243-271:
        //   IF WS-EDIT-DATE-YY-N = 0  -> MOVE 400 TO WS-DIV-BY
        //   ELSE                       -> MOVE 4   TO WS-DIV-BY
        //   DIVIDE WS-EDIT-DATE-CCYY-N BY WS-DIV-BY GIVING ... REMAINDER ...
        //   IF REMAINDER = 0 -> leap year
        if (month == FEBRUARY && day == 29) {
            int yy = year % 100;
            int divBy;
            if (yy == 0) {
                divBy = 400;
            } else {
                divBy = 4;
            }
            int remainder = year % divBy;
            if (remainder != 0) {
                result.setDayFlag(FlagStatus.NOT_OK);
                result.setMonthFlag(FlagStatus.NOT_OK);
                result.setYearFlag(FlagStatus.NOT_OK);
                result.setErrorMessage(
                        fieldName + ":Not a leap year.Cannot have 29 days in this month.");
                return false;
            }
        }

        // If already invalid from earlier steps, exit
        if (!result.isValid()) {
            return false;
        }

        return true;
    }

    /**
     * Final date validation using Java's date parsing (equivalent to CEEDAYS API call).
     * Maps to EDIT-DATE-LE paragraph in CSUTLDPY.cpy which calls CSUTLDTC.cbl.
     *
     * <p>The CSUTLDTC.cbl EVALUATE block maps feedback codes to result messages:
     * <ul>
     *   <li>FC-INVALID-DATE      -> "Date is valid"</li>
     *   <li>FC-INSUFFICIENT-DATA -> "Insufficient"</li>
     *   <li>FC-BAD-DATE-VALUE    -> "Datevalue error"</li>
     *   <li>FC-INVALID-ERA       -> "Invalid Era    "</li>
     *   <li>FC-UNSUPP-RANGE      -> "Unsupp. Range  "</li>
     *   <li>FC-INVALID-MONTH     -> "Invalid month  "</li>
     *   <li>FC-BAD-PIC-STRING    -> "Bad Pic String "</li>
     *   <li>FC-NON-NUMERIC-DATA  -> "Nonnumeric data"</li>
     *   <li>FC-YEAR-IN-ERA-ZERO  -> "YearInEra is 0 "</li>
     *   <li>OTHER                -> "Date is invalid"</li>
     * </ul>
     */
    void editDateLe(String fieldName, String dateStr, DateValidationResult result) {
        try {
            LocalDate.parse(dateStr, STRICT_DATE_FORMATTER);
            // Date is valid — equivalent to FC-INVALID-DATE (severity 0)
            result.setDayFlag(FlagStatus.VALID);
        } catch (DateTimeException e) {
            result.setDayFlag(FlagStatus.NOT_OK);
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setYearFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + " validation error: Date is invalid");
        }
    }

    /**
     * Date of birth future check.
     * Maps to EDIT-DATE-OF-BIRTH paragraph in CSUTLDPY.cpy.
     */
    private DateValidationResult editDateOfBirth(String fieldName, String dateStr,
                                                  DateValidationResult result) {
        String normalized = normalizeInput(dateStr, 8);
        LocalDate editDate = LocalDate.parse(normalized, STRICT_DATE_FORMATTER);
        LocalDate currentDate = LocalDate.now();

        // COBOL: IF WS-CURRENT-DATE-BINARY > WS-EDIT-DATE-BINARY -> CONTINUE
        //        ELSE -> error
        if (!currentDate.isAfter(editDate) && !currentDate.isEqual(editDate)) {
            result.setDayFlag(FlagStatus.NOT_OK);
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setYearFlag(FlagStatus.NOT_OK);
            result.setErrorMessage(fieldName + ":cannot be in the future");
            return result;
        }

        return result;
    }

    /**
     * Normalizes input to a fixed-length string, padding with spaces if needed.
     */
    private String normalizeInput(String input, int length) {
        if (input == null) {
            return " ".repeat(length);
        }
        if (input.length() >= length) {
            return input.substring(0, length);
        }
        return input + " ".repeat(length - input.length());
    }
}
