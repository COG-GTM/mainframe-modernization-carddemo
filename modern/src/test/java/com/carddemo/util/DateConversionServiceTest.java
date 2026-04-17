package com.carddemo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DateConversionService}, verifying equivalence with the
 * COBOL {@code CSUTLDTC.cbl} date conversion utility.
 */
class DateConversionServiceTest {

    private DateConversionService service;

    @BeforeEach
    void setUp() {
        service = new DateConversionService();
    }

    @Nested
    @DisplayName("convertDate — format-to-format conversions")
    class ConvertDate {

        @ParameterizedTest(name = "{0} ({1}) -> {2} = {3}")
        @CsvSource({
                "20240115, YYYYMMDD, YYYY-MM-DD, 2024-01-15",
                "20240115, YYYYMMDD, MM/DD/YYYY, 01/15/2024",
                "20240115, YYYYMMDD, DD/MM/YYYY, 15/01/2024",
                "2024-01-15, YYYY-MM-DD, YYYYMMDD, 20240115",
                "01/15/2024, MM/DD/YYYY, YYYYMMDD, 20240115",
                "15/01/2024, DD/MM/YYYY, YYYY-MM-DD, 2024-01-15",
                "12/31/2023, MM/DD/YYYY, DD/MM/YYYY, 31/12/2023",
        })
        void shouldConvertBetweenFormats(String input, String inputFmt, String outputFmt, String expected) {
            assertEquals(expected, service.convertDate(input, inputFmt, outputFmt));
        }

        @Test
        @DisplayName("should convert YYYYMMDD to Julian and back")
        void shouldConvertToAndFromJulian() {
            String julian = service.convertDate("20240115", "YYYYMMDD", "JULIAN");
            assertNotNull(julian);
            String roundTrip = service.convertDate(julian, "JULIAN", "YYYYMMDD");
            assertEquals("20240115", roundTrip);
        }

        @Test
        @DisplayName("should handle leap year date Feb 29")
        void shouldHandleLeapYear() {
            assertEquals("02/29/2024", service.convertDate("20240229", "YYYYMMDD", "MM/DD/YYYY"));
        }

        @Test
        @DisplayName("should handle epoch boundary dates")
        void shouldHandleEpochBoundary() {
            assertEquals("1970-01-01", service.convertDate("19700101", "YYYYMMDD", "YYYY-MM-DD"));
        }
    }

    @Nested
    @DisplayName("convertDate — error handling")
    class ConvertDateErrors {

        @Test
        @DisplayName("should throw on null input")
        void shouldThrowOnNullInput() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.convertDate(null, "YYYYMMDD", "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("should throw on blank input")
        void shouldThrowOnBlankInput() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.convertDate("   ", "YYYYMMDD", "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("should throw on unsupported format")
        void shouldThrowOnUnsupportedFormat() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.convertDate("20240115", "BADFORMAT", "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("should throw on invalid date value")
        void shouldThrowOnInvalidDate() {
            assertThrows(DateTimeParseException.class,
                    () -> service.convertDate("20241301", "YYYYMMDD", "YYYY-MM-DD"));
        }

        @Test
        @DisplayName("should throw on non-leap-year Feb 29")
        void shouldThrowOnNonLeapYear() {
            assertThrows(DateTimeParseException.class,
                    () -> service.convertDate("20230229", "YYYYMMDD", "YYYY-MM-DD"));
        }
    }

    @Nested
    @DisplayName("isValidDate")
    class IsValidDate {

        @Test
        @DisplayName("should return true for valid YYYYMMDD")
        void shouldValidateCorrectDate() {
            assertTrue(service.isValidDate("20240115", "YYYYMMDD"));
        }

        @Test
        @DisplayName("should return false for invalid month")
        void shouldRejectInvalidMonth() {
            assertFalse(service.isValidDate("20241301", "YYYYMMDD"));
        }

        @Test
        @DisplayName("should return false for invalid day")
        void shouldRejectInvalidDay() {
            assertFalse(service.isValidDate("20240132", "YYYYMMDD"));
        }

        @Test
        @DisplayName("should return false for non-numeric input")
        void shouldRejectNonNumeric() {
            assertFalse(service.isValidDate("ABCDEFGH", "YYYYMMDD"));
        }
    }

    @Nested
    @DisplayName("Julian day conversions")
    class JulianDay {

        @Test
        @DisplayName("should compute Julian day number")
        void shouldComputeJulianDay() {
            long julian = service.toJulianDay("2024-01-15", "YYYY-MM-DD");
            assertTrue(julian > 0);
        }

        @Test
        @DisplayName("should round-trip through Julian day")
        void shouldRoundTripJulian() {
            long julian = service.toJulianDay("2024-01-15", "YYYY-MM-DD");
            String result = service.fromJulianDay(julian, "YYYY-MM-DD");
            assertEquals("2024-01-15", result);
        }

        @Test
        @DisplayName("known Julian day: 2000-01-01 = JD 2451545")
        void shouldMatchKnownJulianDay() {
            long julian = service.toJulianDay("20000101", "YYYYMMDD");
            assertEquals(2451545L, julian);
        }
    }
}
