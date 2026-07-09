package com.carddemo.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link DateValidator}, covering the CSUTLDTC / CEEDAYS behaviour: valid dates,
 * invalid formats, impossible dates (including leap-year handling), boundary dates and the
 * COBOL feedback-code / severity / message-number mapping.
 */
class DateValidatorTest {

    private final DateValidator validator = new DateValidator();

    @Nested
    @DisplayName("valid dates")
    class ValidDates {

        @ParameterizedTest
        @ValueSource(strings = {"20240229", "20230228", "20000229", "19000228",
                "20240101", "20241231", "20240430", "20240115"})
        void acceptsValidCcyymmddDates(String date) {
            DateValidationResult result = validator.validate(date);

            assertThat(result.isValid()).isTrue();
            assertThat(result.status()).isEqualTo(DateValidationStatus.VALID);
            assertThat(result.severity()).isZero();
            assertThat(result.messageNumber()).isZero();
            assertThat(result.resultText()).isEqualTo("Date is valid");
        }

        @Test
        void acceptsTrailingBlanksInTenCharField() {
            // CardDemo passes 10-byte fields: "20240229  " with a "YYYYMMDD  " mask.
            DateValidationResult result = validator.validate("20240229  ", "YYYYMMDD  ");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        void computesLilianDayNumber() {
            // Lilian day 1 is 1582-10-15 by definition.
            assertThat(validator.validate("15821015").lilianDay()).isEqualTo(1L);
            assertThat(validator.validate("15821016").lilianDay()).isEqualTo(2L);
        }

        @Test
        void lilianDayIsNullWhenInvalid() {
            assertThat(validator.validate("20241301").lilianDay()).isNull();
        }
    }

    @Nested
    @DisplayName("leap year handling")
    class LeapYears {

        @Test
        void feb29ValidInLeapYear() {
            assertThat(validator.isValid("20240229")).isTrue();
        }

        @Test
        void feb29InvalidInNonLeapYear() {
            DateValidationResult result = validator.validate("20230229");

            assertThat(result.isValid()).isFalse();
            assertThat(result.status()).isEqualTo(DateValidationStatus.BAD_DATE_VALUE);
            assertThat(result.messageNumber()).isEqualTo(2508);
        }

        @Test
        void feb29ValidInYear2000DivisibleBy400() {
            assertThat(validator.isValid("20000229")).isTrue();
        }

        @Test
        void feb29InvalidInYear1900CenturyNotDivisibleBy400() {
            assertThat(validator.isValid("19000229")).isFalse();
        }
    }

    @Nested
    @DisplayName("impossible / out-of-range field values")
    class ImpossibleDates {

        @Test
        void month13IsInvalidMonth() {
            DateValidationResult result = validator.validate("20241301");

            assertThat(result.status()).isEqualTo(DateValidationStatus.INVALID_MONTH);
            assertThat(result.messageNumber()).isEqualTo(2517);
            assertThat(result.resultText()).isEqualTo("Invalid month");
        }

        @Test
        void month00IsInvalidMonth() {
            assertThat(validator.validate("20240001").status())
                    .isEqualTo(DateValidationStatus.INVALID_MONTH);
        }

        @Test
        void day00IsBadDateValue() {
            DateValidationResult result = validator.validate("20240100");

            assertThat(result.status()).isEqualTo(DateValidationStatus.BAD_DATE_VALUE);
            assertThat(result.messageNumber()).isEqualTo(2508);
        }

        @Test
        void day32IsBadDateValue() {
            assertThat(validator.validate("20240132").status())
                    .isEqualTo(DateValidationStatus.BAD_DATE_VALUE);
        }

        @Test
        void day31InThirtyDayMonthIsBadDateValue() {
            // April has 30 days.
            assertThat(validator.validate("20240431").status())
                    .isEqualTo(DateValidationStatus.BAD_DATE_VALUE);
        }

        @Test
        void feb30IsBadDateValue() {
            assertThat(validator.validate("20240230").status())
                    .isEqualTo(DateValidationStatus.BAD_DATE_VALUE);
        }

        @Test
        void yearZeroIsYearInEraZero() {
            DateValidationResult result = validator.validate("00000101");

            assertThat(result.status()).isEqualTo(DateValidationStatus.YEAR_IN_ERA_ZERO);
            assertThat(result.messageNumber()).isEqualTo(2521);
        }
    }

    @Nested
    @DisplayName("nonnumeric / insufficient data")
    class BadInput {

        @Test
        void lettersInDateAreNonnumeric() {
            DateValidationResult result = validator.validate("2024AB29");

            assertThat(result.status()).isEqualTo(DateValidationStatus.NONNUMERIC_DATA);
            assertThat(result.messageNumber()).isEqualTo(2520);
            assertThat(result.resultText()).isEqualTo("Nonnumeric data");
        }

        @Test
        void embeddedSpaceIsNonnumeric() {
            assertThat(validator.validate("2024 229").status())
                    .isEqualTo(DateValidationStatus.NONNUMERIC_DATA);
        }

        @Test
        void tooShortIsInsufficientData() {
            DateValidationResult result = validator.validate("202402");

            assertThat(result.status()).isEqualTo(DateValidationStatus.INSUFFICIENT_DATA);
            assertThat(result.messageNumber()).isEqualTo(2507);
            assertThat(result.resultText()).isEqualTo("Insufficient");
        }

        @Test
        void emptyDateIsInsufficientData() {
            assertThat(validator.validate("").status())
                    .isEqualTo(DateValidationStatus.INSUFFICIENT_DATA);
        }

        @Test
        void nullDateIsInsufficientData() {
            assertThat(validator.validate(null).status())
                    .isEqualTo(DateValidationStatus.INSUFFICIENT_DATA);
        }
    }

    @Nested
    @DisplayName("bad picture strings")
    class BadPictures {

        @ParameterizedTest
        @ValueSource(strings = {"YYYMMDD", "YYYYMMD", "YYYYWWDD", "ABCDEFGH"})
        void invalidPictureIsBadPicString(String format) {
            DateValidationResult result = validator.validate("20240229", format);

            assertThat(result.status()).isEqualTo(DateValidationStatus.BAD_PICTURE_STRING);
            assertThat(result.messageNumber()).isEqualTo(2518);
            assertThat(result.resultText()).isEqualTo("Bad Pic String");
        }

        @Test
        void nullFormatIsBadPicString() {
            assertThat(validator.validate("20240229", null).status())
                    .isEqualTo(DateValidationStatus.BAD_PICTURE_STRING);
        }
    }

    @Nested
    @DisplayName("alternate pictures with separators")
    class AlternatePictures {

        @Test
        void isoDashedFormat() {
            assertThat(validator.isValid("2024-02-29", "YYYY-MM-DD")).isTrue();
        }

        @Test
        void usSlashFormat() {
            assertThat(validator.isValid("02/29/2024", "MM/DD/YYYY")).isTrue();
        }

        @Test
        void twoDigitYearWindowsTo1900s() {
            // YY defaults to the 1900s CEEDAYS window: "99" -> 1999.
            assertThat(validator.validate("991231", "YYMMDD").lilianDay())
                    .isEqualTo(validator.validate("19991231").lilianDay());
        }

        @Test
        void separatorMismatchIsBadDateValue() {
            assertThat(validator.validate("2024/02/29", "YYYY-MM-DD").status())
                    .isEqualTo(DateValidationStatus.BAD_DATE_VALUE);
        }
    }

    @Nested
    @DisplayName("supported range boundaries")
    class RangeBoundaries {

        @Test
        void dateBeforeLilianEpochIsUnsupportedRange() {
            // 1582-10-14 is a real calendar day but before the Lilian epoch (1582-10-15).
            DateValidationResult result = validator.validate("15821014");

            assertThat(result.status()).isEqualTo(DateValidationStatus.UNSUPPORTED_RANGE);
            assertThat(result.messageNumber()).isEqualTo(2513);
            assertThat(result.resultText()).isEqualTo("Unsupp. Range");
        }

        @Test
        void lilianEpochItselfIsValid() {
            assertThat(validator.isValid("15821015")).isTrue();
        }

        @Test
        void maxSupportedDateIsValid() {
            assertThat(validator.isValid("99991231")).isTrue();
        }
    }

    @Nested
    @DisplayName("feedback-code metadata mapping")
    class FeedbackMetadata {

        @ParameterizedTest
        @CsvSource({
                "VALID,0,0,Date is valid",
                "INSUFFICIENT_DATA,3,2507,Insufficient",
                "BAD_DATE_VALUE,3,2508,Datevalue error",
                "INVALID_ERA,3,2509,Invalid Era",
                "UNSUPPORTED_RANGE,3,2513,Unsupp. Range",
                "INVALID_MONTH,3,2517,Invalid month",
                "BAD_PICTURE_STRING,3,2518,Bad Pic String",
                "NONNUMERIC_DATA,3,2520,Nonnumeric data",
                "YEAR_IN_ERA_ZERO,3,2521,YearInEra is 0",
        })
        void statusMetadataMatchesCobol(DateValidationStatus status, int severity,
                                        int messageNumber, String resultText) {
            assertThat(status.severity()).isEqualTo(severity);
            assertThat(status.messageNumber()).isEqualTo(messageNumber);
            assertThat(status.resultText()).isEqualTo(resultText);
        }

        @Test
        void onlyValidIsValid() {
            for (DateValidationStatus status : DateValidationStatus.values()) {
                assertThat(status.isValid()).isEqualTo(status == DateValidationStatus.VALID);
            }
        }
    }

    @Nested
    @DisplayName("formatted 80-character LS-RESULT message")
    class FormattedMessage {

        @Test
        void validMessageLayoutMatchesWsMessage() {
            String message = validator.validate("20240229", "YYYYMMDD").formattedMessage();

            assertThat(message).hasSize(80);
            assertThat(message).startsWith("0000Mesg Code: 0000 Date is valid  ");
            assertThat(message).contains("TstDate: 20240229");
            assertThat(message).contains("Mask used:YYYYMMDD");
        }

        @Test
        void errorMessageCarriesSeverityAndMessageNumber() {
            String message = validator.validate("20241301", "YYYYMMDD").formattedMessage();

            assertThat(message).hasSize(80);
            assertThat(message).startsWith("0003Mesg Code: 2517 Invalid month");
        }
    }
}
