package com.carddemo.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DateValidatorTest {

    // --- Valid dates ---

    @Test
    void validDate_isoFormat() {
        DateValidationResult result = DateValidator.validate("2024-01-15", "YYYY-MM-DD");
        assertTrue(result.isValid());
        assertEquals("Date is valid", result.getMessage());
        assertEquals(0, result.getSeverityCode());
        assertEquals("2024-01-15", result.getTestedDate());
        assertEquals("YYYY-MM-DD", result.getFormatUsed());
    }

    @Test
    void validDate_usFormat() {
        DateValidationResult result = DateValidator.validate("01/15/2024", "MM/DD/YYYY");
        assertTrue(result.isValid());
        assertEquals("Date is valid", result.getMessage());
    }

    @Test
    void validDate_europeanFormat() {
        DateValidationResult result = DateValidator.validate("15/01/2024", "DD/MM/YYYY");
        assertTrue(result.isValid());
        assertEquals("Date is valid", result.getMessage());
    }

    @Test
    void validDate_compactFormat() {
        DateValidationResult result = DateValidator.validate("20240115", "YYYYMMDD");
        assertTrue(result.isValid());
        assertEquals("Date is valid", result.getMessage());
    }

    // --- Leap year tests ---

    @Test
    void leapYear_feb29Valid() {
        DateValidationResult result = DateValidator.validate("2024-02-29", "YYYY-MM-DD");
        assertTrue(result.isValid());
        assertEquals("Date is valid", result.getMessage());
    }

    @Test
    void leapYear_feb29Invalid_nonLeapYear() {
        DateValidationResult result = DateValidator.validate("2023-02-29", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Datevalue error", result.getMessage());
        assertEquals(1, result.getSeverityCode());
    }

    @Test
    void leapYear_centuryNotLeap() {
        DateValidationResult result = DateValidator.validate("1900-02-29", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Datevalue error", result.getMessage());
    }

    @Test
    void leapYear_400YearLeap() {
        DateValidationResult result = DateValidator.validate("2000-02-29", "YYYY-MM-DD");
        assertTrue(result.isValid());
    }

    @Test
    void leapYear_feb28AlwaysValid() {
        DateValidationResult result = DateValidator.validate("2023-02-28", "YYYY-MM-DD");
        assertTrue(result.isValid());
    }

    // --- Invalid months ---

    @Test
    void invalidMonth_zero() {
        DateValidationResult result = DateValidator.validate("2024-00-15", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Invalid month", result.getMessage());
    }

    @Test
    void invalidMonth_thirteen() {
        DateValidationResult result = DateValidator.validate("2024-13-15", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Invalid month", result.getMessage());
    }

    // --- Invalid days ---

    @Test
    void invalidDay_zero() {
        DateValidationResult result = DateValidator.validate("2024-01-00", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Datevalue error", result.getMessage());
    }

    @Test
    void invalidDay_31InApril() {
        DateValidationResult result = DateValidator.validate("2024-04-31", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Datevalue error", result.getMessage());
    }

    @Test
    void invalidDay_32InJanuary() {
        DateValidationResult result = DateValidator.validate("2024-01-32", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Datevalue error", result.getMessage());
    }

    // --- Boundary days for each month ---

    @ParameterizedTest
    @CsvSource({
            "2024-01-31, true",
            "2024-03-31, true",
            "2024-04-30, true",
            "2024-06-30, true",
            "2024-09-30, true",
            "2024-11-30, true",
            "2024-05-31, true",
            "2024-07-31, true",
            "2024-08-31, true",
            "2024-10-31, true",
            "2024-12-31, true"
    })
    void validDate_lastDayOfMonth(String date, boolean expected) {
        DateValidationResult result = DateValidator.validate(date, "YYYY-MM-DD");
        assertEquals(expected, result.isValid());
    }

    // --- Null and empty inputs ---

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void nullOrEmptyDate_returnsInsufficient(String date) {
        DateValidationResult result = DateValidator.validate(date, "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Insufficient", result.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void nullOrEmptyFormat_returnsBadPicString(String format) {
        DateValidationResult result = DateValidator.validate("2024-01-15", format);
        assertFalse(result.isValid());
        assertEquals("Bad Pic String", result.getMessage());
    }

    // --- Unsupported format ---

    @Test
    void unsupportedFormat_returnsBadPicString() {
        DateValidationResult result = DateValidator.validate("2024-01-15", "DD-MMM-YYYY");
        assertFalse(result.isValid());
        assertEquals("Bad Pic String", result.getMessage());
    }

    // --- Non-numeric data ---

    @Test
    void nonNumericData_inIsoFormat() {
        DateValidationResult result = DateValidator.validate("20AB-01-15", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Nonnumeric data", result.getMessage());
    }

    @Test
    void nonNumericData_inCompactFormat() {
        DateValidationResult result = DateValidator.validate("2024O115", "YYYYMMDD");
        assertFalse(result.isValid());
        assertEquals("Nonnumeric data", result.getMessage());
    }

    // --- Year range ---

    @Test
    void yearTooLow_returnsUnsuppRange() {
        DateValidationResult result = DateValidator.validate("1599-01-01", "YYYY-MM-DD");
        assertFalse(result.isValid());
        assertEquals("Unsupp. Range", result.getMessage());
    }

    @Test
    void yearAtMinBoundary_valid() {
        DateValidationResult result = DateValidator.validate("1600-01-01", "YYYY-MM-DD");
        assertTrue(result.isValid());
    }

    @Test
    void yearAtMaxBoundary_valid() {
        DateValidationResult result = DateValidator.validate("9999-12-31", "YYYY-MM-DD");
        assertTrue(result.isValid());
    }

    // --- Format case insensitivity ---

    @Test
    void formatCaseInsensitive() {
        DateValidationResult result = DateValidator.validate("2024-01-15", "yyyy-mm-dd");
        assertTrue(result.isValid());
    }

    // --- Wrong separator for format ---

    @Test
    void wrongSeparator_returnsInvalid() {
        DateValidationResult result = DateValidator.validate("2024/01/15", "YYYY-MM-DD");
        assertFalse(result.isValid());
    }

    // --- Cross-format validation ---

    @Test
    void usFormat_monthDaySwapped() {
        // 13/01/2024 is invalid for MM/DD/YYYY since month=13
        DateValidationResult result = DateValidator.validate("13/01/2024", "MM/DD/YYYY");
        assertFalse(result.isValid());
        assertEquals("Invalid month", result.getMessage());
    }

    @Test
    void europeanFormat_dayMonthSwapped() {
        // 01/13/2024 is invalid for DD/MM/YYYY since month=13
        DateValidationResult result = DateValidator.validate("01/13/2024", "DD/MM/YYYY");
        assertFalse(result.isValid());
        assertEquals("Invalid month", result.getMessage());
    }

    // --- toString ---

    @Test
    void resultToString_containsAllFields() {
        DateValidationResult result = DateValidator.validate("2024-01-15", "YYYY-MM-DD");
        String str = result.toString();
        assertTrue(str.contains("valid=true"));
        assertTrue(str.contains("Date is valid"));
        assertTrue(str.contains("2024-01-15"));
        assertTrue(str.contains("YYYY-MM-DD"));
    }
}
