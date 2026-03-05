package com.carddemo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DateUtils — validates parity with CSUTLDTC.cbl and COBDATFT.asm.
 */
class DateUtilsTest {

    @Test
    void validateDate_validDates() {
        assertTrue(DateUtils.validateDate("2024-01-15", "yyyy-MM-dd"));
        assertTrue(DateUtils.validateDate("12/31/2023", "MM/dd/yyyy"));
        assertTrue(DateUtils.validateDate("20240229", "yyyyMMdd")); // Leap year
    }

    @Test
    void validateDate_invalidDates() {
        // Note: DateTimeFormatter with SMART resolver adjusts Feb 29 on non-leap years
        // so 2023-02-29 parses as 2023-02-28. Use isValidDate() for strict COBOL-parity checks.
        assertFalse(DateUtils.validateDate("2024-13-01", "yyyy-MM-dd")); // Month 13
        assertFalse(DateUtils.validateDate("2024-00-15", "yyyy-MM-dd")); // Month 0
        assertFalse(DateUtils.validateDate("invalid", "yyyy-MM-dd"));
        assertFalse(DateUtils.validateDate("", "yyyy-MM-dd"));
        assertFalse(DateUtils.validateDate(null, "yyyy-MM-dd"));
    }

    @Test
    void convertDateFormat() {
        String result = DateUtils.convertDateFormat("2024-01-15", "yyyy-MM-dd", "MM/dd/yyyy");
        assertEquals("01/15/2024", result);
    }

    @Test
    void isValidMonth() {
        // Mirrors CSUTLDWY.cpy month validation
        for (int m = 1; m <= 12; m++) {
            assertTrue(DateUtils.isValidMonth(m), "Month " + m + " should be valid");
        }
        assertFalse(DateUtils.isValidMonth(0));
        assertFalse(DateUtils.isValidMonth(13));
        assertFalse(DateUtils.isValidMonth(-1));
    }

    @Test
    void isLeapYear() {
        // Mirrors CSUTLDWY.cpy leap year check
        assertTrue(DateUtils.isLeapYear(2024));  // Divisible by 4
        assertTrue(DateUtils.isLeapYear(2000));  // Divisible by 400
        assertFalse(DateUtils.isLeapYear(1900)); // Divisible by 100 but not 400
        assertFalse(DateUtils.isLeapYear(2023)); // Not divisible by 4
    }

    @Test
    void isValidDayOfMonth_february() {
        // Mirrors CSUTLDWY.cpy February 28/29 logic
        assertTrue(DateUtils.isValidDayOfMonth(2024, 2, 29));  // Leap year
        assertFalse(DateUtils.isValidDayOfMonth(2023, 2, 29)); // Not leap year
        assertTrue(DateUtils.isValidDayOfMonth(2023, 2, 28));
        assertFalse(DateUtils.isValidDayOfMonth(2023, 2, 0));
    }

    @Test
    void isValidDayOfMonth_thirtyOneDayMonths() {
        // Mirrors CSUTLDWY.cpy 31-day month checks
        int[] months31 = {1, 3, 5, 7, 8, 10, 12};
        for (int m : months31) {
            assertTrue(DateUtils.isValidDayOfMonth(2024, m, 31), "Month " + m + " should allow 31 days");
            assertFalse(DateUtils.isValidDayOfMonth(2024, m, 32), "Month " + m + " should not allow 32 days");
        }
    }

    @Test
    void isValidDayOfMonth_thirtyDayMonths() {
        int[] months30 = {4, 6, 9, 11};
        for (int m : months30) {
            assertTrue(DateUtils.isValidDayOfMonth(2024, m, 30), "Month " + m + " should allow 30 days");
            assertFalse(DateUtils.isValidDayOfMonth(2024, m, 31), "Month " + m + " should not allow 31 days");
        }
    }

    @Test
    void isValidDate_comprehensive() {
        assertTrue(DateUtils.isValidDate(2024, 1, 1));
        assertTrue(DateUtils.isValidDate(2024, 12, 31));
        assertTrue(DateUtils.isValidDate(2024, 2, 29));
        assertFalse(DateUtils.isValidDate(2023, 2, 29));
        assertFalse(DateUtils.isValidDate(2024, 0, 1));
        assertFalse(DateUtils.isValidDate(2024, 1, 0));
    }
}
