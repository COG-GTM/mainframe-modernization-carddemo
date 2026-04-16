package com.cardemo.utility.service;

import com.cardemo.utility.model.DateValidationResult;
import com.cardemo.utility.model.DateValidationResult.FlagStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DateValidationService.
 * Each test maps to a specific COBOL validation rule from CSUTLDPY.cpy.
 */
class DateValidationServiceTest {

    private DateValidationService service;
    private static final String FIELD = "TestDate";

    @BeforeEach
    void setUp() {
        service = new DateValidationService();
    }

    // =========================================================================
    // EDIT-YEAR-CCYY tests
    // =========================================================================
    @Nested
    @DisplayName("EDIT-YEAR-CCYY: Year validation")
    class EditYearCcyyTests {

        @Test
        @DisplayName("Year must be supplied - blank year")
        void yearBlank() {
            DateValidationResult result = service.editDateCcyymmdd("    0115", FIELD);
            assertEquals(FlagStatus.BLANK, result.getYearFlag());
            assertEquals(FIELD + " : Year must be supplied.", result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Year must be 4 digit number - non-numeric")
        void yearNonNumeric() {
            DateValidationResult result = service.editDateCcyymmdd("AB120115", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getYearFlag());
            assertEquals(FIELD + " must be 4 digit number.", result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Century not valid - century 18")
        void centuryInvalid18() {
            DateValidationResult result = service.editDateCcyymmdd("18990115", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getYearFlag());
            assertEquals(FIELD + " : Century is not valid.", result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Century not valid - century 21")
        void centuryInvalid21() {
            DateValidationResult result = service.editDateCcyymmdd("21000115", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getYearFlag());
            assertEquals(FIELD + " : Century is not valid.", result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Valid century 19")
        void centuryValid19() {
            DateValidationResult result = service.editDateCcyymmdd("19900115", FIELD);
            assertEquals(FlagStatus.VALID, result.getYearFlag());
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid century 20")
        void centuryValid20() {
            DateValidationResult result = service.editDateCcyymmdd("20240115", FIELD);
            assertEquals(FlagStatus.VALID, result.getYearFlag());
            assertTrue(result.isValid());
        }
    }

    // =========================================================================
    // EDIT-MONTH tests
    // =========================================================================
    @Nested
    @DisplayName("EDIT-MONTH: Month validation")
    class EditMonthTests {

        @Test
        @DisplayName("Month must be supplied - blank month")
        void monthBlank() {
            DateValidationResult result = service.editDateCcyymmdd("2024  15", FIELD);
            assertEquals(FlagStatus.BLANK, result.getMonthFlag());
            assertEquals(FIELD + " : Month must be supplied.", result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Month must be a number - non-numeric")
        void monthNonNumeric() {
            DateValidationResult result = service.editDateCcyymmdd("2024AB15", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ": Month must be a number between 1 and 12.",
                    result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Month out of range - 0")
        void monthZero() {
            DateValidationResult result = service.editDateCcyymmdd("20240015", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ": Month must be a number between 1 and 12.",
                    result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Month out of range - 13")
        void monthThirteen() {
            DateValidationResult result = service.editDateCcyymmdd("20241315", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ": Month must be a number between 1 and 12.",
                    result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Valid month - January")
        void monthJanuary() {
            DateValidationResult result = service.editDateCcyymmdd("20240115", FIELD);
            assertEquals(FlagStatus.VALID, result.getMonthFlag());
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid month - December")
        void monthDecember() {
            DateValidationResult result = service.editDateCcyymmdd("20241215", FIELD);
            assertEquals(FlagStatus.VALID, result.getMonthFlag());
            assertTrue(result.isValid());
        }
    }

    // =========================================================================
    // EDIT-DAY tests
    // =========================================================================
    @Nested
    @DisplayName("EDIT-DAY: Day validation")
    class EditDayTests {

        @Test
        @DisplayName("Day must be supplied - blank day")
        void dayBlank() {
            DateValidationResult result = service.editDateCcyymmdd("202401  ", FIELD);
            assertEquals(FlagStatus.BLANK, result.getDayFlag());
            assertEquals(FIELD + " : Day must be supplied.", result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Day must be a number - non-numeric")
        void dayNonNumeric() {
            DateValidationResult result = service.editDateCcyymmdd("202401AB", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FIELD + ":day must be a number between 1 and 31.",
                    result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Day out of range - 0")
        void dayZero() {
            DateValidationResult result = service.editDateCcyymmdd("20240100", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FIELD + ":day must be a number between 1 and 31.",
                    result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Day out of range - 32")
        void dayThirtyTwo() {
            DateValidationResult result = service.editDateCcyymmdd("20240132", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FIELD + ":day must be a number between 1 and 31.",
                    result.getErrorMessage());
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Valid day - 1")
        void dayOne() {
            DateValidationResult result = service.editDateCcyymmdd("20240101", FIELD);
            assertEquals(FlagStatus.VALID, result.getDayFlag());
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid day - 31 in January")
        void dayThirtyOneJanuary() {
            DateValidationResult result = service.editDateCcyymmdd("20240131", FIELD);
            assertEquals(FlagStatus.VALID, result.getDayFlag());
            assertTrue(result.isValid());
        }
    }

    // =========================================================================
    // EDIT-DAY-MONTH-YEAR tests (cross-validation)
    // =========================================================================
    @Nested
    @DisplayName("EDIT-DAY-MONTH-YEAR: Cross-validation")
    class EditDayMonthYearTests {

        @Test
        @DisplayName("31 days valid in January (31-day month)")
        void thirtyOneDaysJanuary() {
            DateValidationResult result = service.editDateCcyymmdd("20240131", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("31 days valid in March (31-day month)")
        void thirtyOneDaysMarch() {
            DateValidationResult result = service.editDateCcyymmdd("20240331", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("31 days valid in May (31-day month)")
        void thirtyOneDaysMay() {
            DateValidationResult result = service.editDateCcyymmdd("20240531", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("31 days valid in July (31-day month)")
        void thirtyOneDaysJuly() {
            DateValidationResult result = service.editDateCcyymmdd("20240731", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("31 days valid in August (31-day month)")
        void thirtyOneDaysAugust() {
            DateValidationResult result = service.editDateCcyymmdd("20240831", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("31 days valid in October (31-day month)")
        void thirtyOneDaysOctober() {
            DateValidationResult result = service.editDateCcyymmdd("20241031", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("31 days valid in December (31-day month)")
        void thirtyOneDaysDecember() {
            DateValidationResult result = service.editDateCcyymmdd("20241231", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Cannot have 31 days in April")
        void thirtyOneDaysApril() {
            DateValidationResult result = service.editDateCcyymmdd("20240431", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ":Cannot have 31 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot have 31 days in June")
        void thirtyOneDaysJune() {
            DateValidationResult result = service.editDateCcyymmdd("20240631", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ":Cannot have 31 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot have 31 days in September")
        void thirtyOneDaysSeptember() {
            DateValidationResult result = service.editDateCcyymmdd("20240931", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ":Cannot have 31 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot have 31 days in November")
        void thirtyOneDaysNovember() {
            DateValidationResult result = service.editDateCcyymmdd("20241131", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ":Cannot have 31 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot have 31 days in February")
        void thirtyOneDaysFebruary() {
            DateValidationResult result = service.editDateCcyymmdd("20240231", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ":Cannot have 31 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot have 30 days in February")
        void thirtyDaysFebruary() {
            DateValidationResult result = service.editDateCcyymmdd("20240230", FIELD);
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FIELD + ":Cannot have 30 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("30 days valid in April")
        void thirtyDaysApril() {
            DateValidationResult result = service.editDateCcyymmdd("20240430", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid Feb 28 in non-leap year")
        void feb28NonLeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("20230228", FIELD);
            assertTrue(result.isValid());
        }
    }

    // =========================================================================
    // Leap year boundary tests
    // =========================================================================
    @Nested
    @DisplayName("Leap year validation (COBOL logic from CSUTLDPY lines 243-271)")
    class LeapYearTests {

        @Test
        @DisplayName("2000 is a leap year (divisible by 400) - Feb 29 valid")
        void year2000LeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("20000229", FIELD);
            assertTrue(result.isValid(),
                    "2000 should be leap year: divisible by 400");
        }

        @Test
        @DisplayName("1900 is NOT a leap year (century year, not divisible by 400)")
        void year1900NotLeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("19000229", FIELD);
            assertFalse(result.isValid());
            assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
            assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
            assertEquals(FlagStatus.NOT_OK, result.getYearFlag());
            assertEquals(
                    FIELD + ":Not a leap year.Cannot have 29 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("2024 is a leap year (divisible by 4, not century year) - Feb 29 valid")
        void year2024LeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("20240229", FIELD);
            assertTrue(result.isValid(),
                    "2024 should be leap year: divisible by 4");
        }

        @Test
        @DisplayName("2023 is NOT a leap year (not divisible by 4)")
        void year2023NotLeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("20230229", FIELD);
            assertFalse(result.isValid());
            assertEquals(
                    FIELD + ":Not a leap year.Cannot have 29 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("1996 is a leap year (divisible by 4, not century year)")
        void year1996LeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("19960229", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("1997 is NOT a leap year")
        void year1997NotLeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("19970229", FIELD);
            assertFalse(result.isValid());
            assertEquals(
                    FIELD + ":Not a leap year.Cannot have 29 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("2100 century not valid (century 21)")
        void year2100CenturyInvalid() {
            // Century 21 is not valid per COBOL rules (only 19 and 20)
            DateValidationResult result = service.editDateCcyymmdd("21000229", FIELD);
            assertFalse(result.isValid());
            assertEquals(FIELD + " : Century is not valid.", result.getErrorMessage());
        }
    }

    // =========================================================================
    // EDIT-DATE-OF-BIRTH tests
    // =========================================================================
    @Nested
    @DisplayName("EDIT-DATE-OF-BIRTH: Date of birth cannot be in the future")
    class EditDateOfBirthTests {

        @Test
        @DisplayName("Past date is valid for DOB")
        void pastDateValid() {
            DateValidationResult result = service.editDateOfBirth("19900115", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Today's date is valid for DOB")
        void todayDateValid() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            DateValidationResult result = service.editDateOfBirth(today, FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Future date is invalid for DOB")
        void futureDateInvalid() {
            String futureDate = LocalDate.now().plusDays(1)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // Ensure the future date is within century 19/20
            int year = Integer.parseInt(futureDate.substring(0, 4));
            if (year >= 1900 && year <= 2099) {
                DateValidationResult result = service.editDateOfBirth(futureDate, FIELD);
                assertFalse(result.isValid());
                assertEquals(FlagStatus.NOT_OK, result.getDayFlag());
                assertEquals(FlagStatus.NOT_OK, result.getMonthFlag());
                assertEquals(FlagStatus.NOT_OK, result.getYearFlag());
                assertEquals(FIELD + ":cannot be in the future",
                        result.getErrorMessage());
            }
        }

        @Test
        @DisplayName("DOB with invalid date format still fails on date validation")
        void invalidDateFormatForDob() {
            DateValidationResult result = service.editDateOfBirth("ABCD0115", FIELD);
            assertFalse(result.isValid());
            assertEquals(FIELD + " must be 4 digit number.", result.getErrorMessage());
        }
    }

    // =========================================================================
    // Fully valid date tests
    // =========================================================================
    @Nested
    @DisplayName("Fully valid dates")
    class ValidDateTests {

        @Test
        @DisplayName("Valid date: 2024-01-15")
        void validDate20240115() {
            DateValidationResult result = service.editDateCcyymmdd("20240115", FIELD);
            assertTrue(result.isValid());
            assertNull(result.getErrorMessage());
            assertEquals(FlagStatus.VALID, result.getYearFlag());
            assertEquals(FlagStatus.VALID, result.getMonthFlag());
            assertEquals(FlagStatus.VALID, result.getDayFlag());
        }

        @Test
        @DisplayName("Valid date: 1999-12-31")
        void validDate19991231() {
            DateValidationResult result = service.editDateCcyymmdd("19991231", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid date: 2000-02-29 (leap year)")
        void validDate20000229() {
            DateValidationResult result = service.editDateCcyymmdd("20000229", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid date: 1900-01-01")
        void validDate19000101() {
            DateValidationResult result = service.editDateCcyymmdd("19000101", FIELD);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Valid date: 2099-12-31")
        void validDate20991231() {
            DateValidationResult result = service.editDateCcyymmdd("20991231", FIELD);
            assertTrue(result.isValid());
        }
    }

    // =========================================================================
    // DateValidationResult structure tests
    // =========================================================================
    @Nested
    @DisplayName("DateValidationResult semantics (WS-EDIT-DATE-FLGS)")
    class DateValidationResultTests {

        @Test
        @DisplayName("isValid when all flags are VALID (LOW-VALUES)")
        void isValidAllLowValues() {
            DateValidationResult result = new DateValidationResult();
            result.setYearFlag(FlagStatus.VALID);
            result.setMonthFlag(FlagStatus.VALID);
            result.setDayFlag(FlagStatus.VALID);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("isInvalid when all flags are NOT_OK ('000')")
        void isInvalidAll000() {
            DateValidationResult result = new DateValidationResult();
            // Default is NOT_OK for all
            assertTrue(result.isInvalid());
        }

        @Test
        @DisplayName("Neither valid nor invalid when mixed flags")
        void mixedFlags() {
            DateValidationResult result = new DateValidationResult();
            result.setYearFlag(FlagStatus.VALID);
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setDayFlag(FlagStatus.BLANK);
            assertFalse(result.isValid());
            assertFalse(result.isInvalid());
        }

        @Test
        @DisplayName("COBOL flag conversion - VALID -> LOW-VALUES")
        void cobolFlagValid() {
            assertEquals('\0', DateValidationResult.toCobolFlag(FlagStatus.VALID));
        }

        @Test
        @DisplayName("COBOL flag conversion - NOT_OK -> '0'")
        void cobolFlagNotOk() {
            assertEquals('0', DateValidationResult.toCobolFlag(FlagStatus.NOT_OK));
        }

        @Test
        @DisplayName("COBOL flag conversion - BLANK -> 'B'")
        void cobolFlagBlank() {
            assertEquals('B', DateValidationResult.toCobolFlag(FlagStatus.BLANK));
        }

        @Test
        @DisplayName("getFlags returns 3-char string")
        void getFlagsString() {
            DateValidationResult result = new DateValidationResult();
            result.setYearFlag(FlagStatus.NOT_OK);
            result.setMonthFlag(FlagStatus.NOT_OK);
            result.setDayFlag(FlagStatus.NOT_OK);
            assertEquals("000", result.getFlags());
        }

        @Test
        @DisplayName("getFlags returns LOW-VALUES for valid result")
        void getFlagsValid() {
            DateValidationResult result = new DateValidationResult();
            result.setYearFlag(FlagStatus.VALID);
            result.setMonthFlag(FlagStatus.VALID);
            result.setDayFlag(FlagStatus.VALID);
            assertEquals("\0\0\0", result.getFlags());
        }
    }

    // =========================================================================
    // Error message exact match tests
    // =========================================================================
    @Nested
    @DisplayName("Error messages character-for-character identical to COBOL source")
    class ErrorMessageExactMatchTests {

        @Test
        @DisplayName("Year must be supplied message")
        void yearMustBeSupplied() {
            DateValidationResult result = service.editDateCcyymmdd("    0115", "DOB");
            assertEquals("DOB : Year must be supplied.", result.getErrorMessage());
        }

        @Test
        @DisplayName("Must be 4 digit number message")
        void mustBe4DigitNumber() {
            DateValidationResult result = service.editDateCcyymmdd("AB120115", "DOB");
            assertEquals("DOB must be 4 digit number.", result.getErrorMessage());
        }

        @Test
        @DisplayName("Century is not valid message")
        void centuryNotValid() {
            DateValidationResult result = service.editDateCcyymmdd("18990115", "DOB");
            assertEquals("DOB : Century is not valid.", result.getErrorMessage());
        }

        @Test
        @DisplayName("Month must be supplied message")
        void monthMustBeSupplied() {
            DateValidationResult result = service.editDateCcyymmdd("2024  15", "DOB");
            assertEquals("DOB : Month must be supplied.", result.getErrorMessage());
        }

        @Test
        @DisplayName("Month must be a number between 1 and 12 message")
        void monthMustBeNumber() {
            DateValidationResult result = service.editDateCcyymmdd("20241315", "DOB");
            assertEquals("DOB: Month must be a number between 1 and 12.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Day must be supplied message")
        void dayMustBeSupplied() {
            DateValidationResult result = service.editDateCcyymmdd("202401  ", "DOB");
            assertEquals("DOB : Day must be supplied.", result.getErrorMessage());
        }

        @Test
        @DisplayName("Day must be a number between 1 and 31 message")
        void dayMustBeNumber() {
            DateValidationResult result = service.editDateCcyymmdd("202401AB", "DOB");
            assertEquals("DOB:day must be a number between 1 and 31.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot have 31 days in this month message")
        void cannotHave31Days() {
            DateValidationResult result = service.editDateCcyymmdd("20240431", "DOB");
            assertEquals("DOB:Cannot have 31 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot have 30 days in this month message (February)")
        void cannotHave30Days() {
            DateValidationResult result = service.editDateCcyymmdd("20240230", "DOB");
            assertEquals("DOB:Cannot have 30 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Not a leap year message")
        void notALeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("20230229", "DOB");
            assertEquals("DOB:Not a leap year.Cannot have 29 days in this month.",
                    result.getErrorMessage());
        }

        @Test
        @DisplayName("Cannot be in the future message")
        void cannotBeInFuture() {
            String futureDate = LocalDate.now().plusYears(1)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            DateValidationResult result = service.editDateOfBirth(futureDate, "DOB");
            assertEquals("DOB:cannot be in the future", result.getErrorMessage());
        }
    }

    // =========================================================================
    // Edge case tests
    // =========================================================================
    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Null input treated as blank")
        void nullInput() {
            DateValidationResult result = service.editDateCcyymmdd(null, FIELD);
            assertEquals(FlagStatus.BLANK, result.getYearFlag());
            assertEquals(FIELD + " : Year must be supplied.", result.getErrorMessage());
        }

        @Test
        @DisplayName("Empty string treated as blank")
        void emptyInput() {
            DateValidationResult result = service.editDateCcyymmdd("", FIELD);
            assertEquals(FlagStatus.BLANK, result.getYearFlag());
            assertEquals(FIELD + " : Year must be supplied.", result.getErrorMessage());
        }

        @Test
        @DisplayName("Short string padded to 8 characters")
        void shortInput() {
            DateValidationResult result = service.editDateCcyymmdd("2024", FIELD);
            // "2024" padded to "2024    " -> month is "  " -> blank
            assertEquals(FlagStatus.VALID, result.getYearFlag());
            assertEquals(FlagStatus.BLANK, result.getMonthFlag());
        }

        @Test
        @DisplayName("All 31-day months accept day 31")
        void allThirtyOneDayMonths() {
            int[] months = {1, 3, 5, 7, 8, 10, 12};
            for (int month : months) {
                String dateStr = String.format("2024%02d31", month);
                DateValidationResult result = service.editDateCcyymmdd(dateStr, FIELD);
                assertTrue(result.isValid(),
                        "Month " + month + " should accept 31 days");
            }
        }

        @Test
        @DisplayName("All 30-day months reject day 31")
        void allThirtyDayMonthsReject31() {
            int[] months = {4, 6, 9, 11};
            for (int month : months) {
                String dateStr = String.format("2024%02d31", month);
                DateValidationResult result = service.editDateCcyymmdd(dateStr, FIELD);
                assertFalse(result.isValid(),
                        "Month " + month + " should NOT accept 31 days");
                assertEquals(FIELD + ":Cannot have 31 days in this month.",
                        result.getErrorMessage());
            }
        }

        @Test
        @DisplayName("February valid days: 1 through 28 in non-leap year")
        void febValidDaysNonLeap() {
            for (int day = 1; day <= 28; day++) {
                String dateStr = String.format("202302%02d", day);
                DateValidationResult result = service.editDateCcyymmdd(dateStr, FIELD);
                assertTrue(result.isValid(),
                        "Feb " + day + " in non-leap year 2023 should be valid");
            }
        }

        @Test
        @DisplayName("February 29 valid in leap year 2024")
        void feb29InLeapYear() {
            DateValidationResult result = service.editDateCcyymmdd("20240229", FIELD);
            assertTrue(result.isValid());
        }
    }
}
