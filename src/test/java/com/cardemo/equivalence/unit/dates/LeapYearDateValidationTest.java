package com.cardemo.equivalence.unit.dates;

import com.cardemo.batch.service.DateValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for leap year date validation per COBOL program CSUTLDTC.
 * Business rule 5: Date utility must correctly handle Feb 29 on leap years.
 * Uses COBOL's WS-DIV-BY approach for leap year calculation.
 */
@DisplayName("Leap Year Date Validation Tests")
class LeapYearDateValidationTest {

    private DateValidationService dateService;

    @BeforeEach
    void setUp() {
        dateService = new DateValidationService();
    }

    @Nested
    @DisplayName("Feb 29 Leap Year Cases")
    class Feb29LeapYear {

        @Test
        @DisplayName("2024-02-29 is valid (divisible by 4, not by 100)")
        void feb29_2024_valid() {
            assertTrue(dateService.isValidDate(2024, 2, 29));
        }

        @Test
        @DisplayName("2023-02-29 is invalid (not a leap year)")
        void feb29_2023_invalid() {
            assertFalse(dateService.isValidDate(2023, 2, 29));
        }

        @Test
        @DisplayName("2000-02-29 is valid (divisible by 400)")
        void feb29_2000_valid() {
            assertTrue(dateService.isValidDate(2000, 2, 29));
        }

        @Test
        @DisplayName("1900-02-29 is invalid (divisible by 100 but not 400)")
        void feb29_1900_invalid() {
            assertFalse(dateService.isValidDate(1900, 2, 29));
        }

        @Test
        @DisplayName("2020-02-29 is valid")
        void feb29_2020_valid() {
            assertTrue(dateService.isValidDate(2020, 2, 29));
        }

        @Test
        @DisplayName("2100-02-29 is invalid (century year, not divisible by 400)")
        void feb29_2100_invalid() {
            assertFalse(dateService.isValidDate(2100, 2, 29));
        }

        @Test
        @DisplayName("2400-02-29 is valid (divisible by 400)")
        void feb29_2400_valid() {
            assertTrue(dateService.isValidDate(2400, 2, 29));
        }
    }

    @Nested
    @DisplayName("Leap Year Detection")
    class LeapYearDetection {

        @ParameterizedTest
        @ValueSource(ints = {2000, 2004, 2008, 2012, 2016, 2020, 2024, 2400})
        @DisplayName("Should identify leap years")
        void isLeapYear_leapYears(int year) {
            assertTrue(dateService.isLeapYear(year));
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 2001, 2002, 2003, 2100, 2200, 2300})
        @DisplayName("Should identify non-leap years")
        void isLeapYear_nonLeapYears(int year) {
            assertFalse(dateService.isLeapYear(year));
        }
    }

    @Nested
    @DisplayName("February Boundaries")
    class FebBoundaries {

        @Test
        @DisplayName("Feb 28 is always valid")
        void feb28_alwaysValid() {
            assertTrue(dateService.isValidDate(2023, 2, 28));
            assertTrue(dateService.isValidDate(2024, 2, 28));
        }

        @Test
        @DisplayName("Feb 30 is never valid")
        void feb30_neverValid() {
            assertFalse(dateService.isValidDate(2024, 2, 30));
            assertFalse(dateService.isValidDate(2000, 2, 30));
        }

        @Test
        @DisplayName("Feb 31 is never valid")
        void feb31_neverValid() {
            assertFalse(dateService.isValidDate(2024, 2, 31));
        }
    }

    @Nested
    @DisplayName("Month Boundary Validation")
    class MonthBoundaries {

        @Test
        @DisplayName("Jan 31 is valid")
        void jan31_valid() {
            assertTrue(dateService.isValidDate(2024, 1, 31));
        }

        @Test
        @DisplayName("Apr 30 is valid, Apr 31 is invalid")
        void april_boundary() {
            assertTrue(dateService.isValidDate(2024, 4, 30));
            assertFalse(dateService.isValidDate(2024, 4, 31));
        }

        @Test
        @DisplayName("Month 0 is invalid")
        void month0_invalid() {
            assertFalse(dateService.isValidDate(2024, 0, 15));
        }

        @Test
        @DisplayName("Month 13 is invalid")
        void month13_invalid() {
            assertFalse(dateService.isValidDate(2024, 13, 15));
        }

        @Test
        @DisplayName("Day 0 is invalid")
        void day0_invalid() {
            assertFalse(dateService.isValidDate(2024, 1, 0));
        }

        @Test
        @DisplayName("Negative day is invalid")
        void negativeDay_invalid() {
            assertFalse(dateService.isValidDate(2024, 1, -1));
        }
    }

    @Nested
    @DisplayName("Date String Parsing")
    class DateStringParsing {

        @Test
        @DisplayName("Valid date string YYYY-MM-DD parsed correctly")
        void validDateString() {
            assertTrue(dateService.isValidDateString("2024-02-29"));
        }

        @Test
        @DisplayName("Invalid leap year date string rejected")
        void invalidLeapYearString() {
            assertFalse(dateService.isValidDateString("2023-02-29"));
        }

        @Test
        @DisplayName("Null date string rejected")
        void nullDateString() {
            assertFalse(dateService.isValidDateString(null));
        }

        @Test
        @DisplayName("Empty date string rejected")
        void emptyDateString() {
            assertFalse(dateService.isValidDateString(""));
        }

        @Test
        @DisplayName("Wrong format date string rejected")
        void wrongFormat() {
            assertFalse(dateService.isValidDateString("02/29/2024"));
        }
    }

    @Nested
    @DisplayName("Date of Birth Validation")
    class DateOfBirth {

        @Test
        @DisplayName("DOB in the past is valid")
        void dob_inPast_valid() {
            assertTrue(dateService.isValidDateOfBirth(1990, 5, 15, 2024, 1, 15));
        }

        @Test
        @DisplayName("DOB equal to current date is invalid (must be strictly past)")
        void dob_today_invalid() {
            assertFalse(dateService.isValidDateOfBirth(2024, 1, 15, 2024, 1, 15));
        }

        @Test
        @DisplayName("DOB in the future is invalid")
        void dob_future_invalid() {
            assertFalse(dateService.isValidDateOfBirth(2025, 6, 1, 2024, 1, 15));
        }

        @Test
        @DisplayName("Invalid DOB date (Feb 30) is invalid")
        void dob_invalidDate_invalid() {
            assertFalse(dateService.isValidDateOfBirth(1990, 2, 30, 2024, 1, 15));
        }

        @Test
        @DisplayName("Leap year DOB on Feb 29 is valid")
        void dob_leapYearFeb29_valid() {
            assertTrue(dateService.isValidDateOfBirth(2000, 2, 29, 2024, 1, 15));
        }
    }
}
