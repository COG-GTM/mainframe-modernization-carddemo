package com.carddemo.transaction.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DateValidator utility.
 * Validates the date validation logic translated from CSUTLDTC.cbl.
 */
class DateValidatorTest {

    @Test
    void isValidDate_validDates() {
        assertTrue(DateValidator.isValidDate("2024-01-15"));
        assertTrue(DateValidator.isValidDate("2024-02-29")); // leap year
        assertTrue(DateValidator.isValidDate("2023-12-31"));
        assertTrue(DateValidator.isValidDate("2000-01-01"));
    }

    @Test
    void isValidDate_invalidDates() {
        assertFalse(DateValidator.isValidDate("2023-02-29")); // not leap year
        assertFalse(DateValidator.isValidDate("2024-13-01")); // invalid month
        assertFalse(DateValidator.isValidDate("2024-00-01")); // zero month
        assertFalse(DateValidator.isValidDate("2024-01-32")); // invalid day
    }

    @Test
    void isValidDate_invalidFormats() {
        assertFalse(DateValidator.isValidDate(null));
        assertFalse(DateValidator.isValidDate(""));
        assertFalse(DateValidator.isValidDate("   "));
        assertFalse(DateValidator.isValidDate("01/15/2024"));
        assertFalse(DateValidator.isValidDate("2024/01/15"));
        assertFalse(DateValidator.isValidDate("20240115"));
        assertFalse(DateValidator.isValidDate("abcd-ef-gh"));
    }

    @Test
    void isValidTimestamp_validTimestamps() {
        assertTrue(DateValidator.isValidTimestamp("2024-01-15-10.30.00.000000"));
        assertTrue(DateValidator.isValidTimestamp("2024-02-29-23.59.59.999999"));
        assertTrue(DateValidator.isValidTimestamp("2024-01-15"));
    }

    @Test
    void isValidTimestamp_invalidTimestamps() {
        assertFalse(DateValidator.isValidTimestamp(null));
        assertFalse(DateValidator.isValidTimestamp(""));
        assertFalse(DateValidator.isValidTimestamp("2024-13"));
        assertFalse(DateValidator.isValidTimestamp("2023-02-29-10.30.00.000000"));
    }
}
