package com.carddemo.online.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Edge cases of COBOL program CSUTLDTC and of the edit paragraphs in copybook CSUTLDPY. */
class DateValidationServiceTest {

    private final DateValidationService service = new DateValidationService();

    @Test
    void acceptsALeapDayOfALeapYear() {
        DateValidationResult result = service.validateCcyymmdd("20240229", "DOB");

        assertThat(result.isValid()).isTrue();
        assertThat(result.returnMessage()).isEmpty();
        assertThat(result.dateFlagsValid()).isTrue();
    }

    @Test
    void acceptsACenturyLeapYearDivisibleBy400() {
        assertThat(service.validateCcyymmdd("20000229", "DOB").isValid()).isTrue();
    }

    @Test
    void rejectsFebruary29OfANonLeapYear() {
        DateValidationResult result = service.validateCcyymmdd("20230229", "DOB");

        assertThat(result.isValid()).isFalse();
        assertThat(result.returnMessage())
                .isEqualTo("DOB:Not a leap year.Cannot have 29 days in this month.");
        assertThat(result.dayFlag()).isEqualTo(DateValidationResult.FLAG_NOT_OK);
    }

    @Test
    void rejectsFebruary29OfACenturyYearNotDivisibleBy400() {
        assertThat(service.validateCcyymmdd("19000229", "DOB").returnMessage())
                .isEqualTo("DOB:Not a leap year.Cannot have 29 days in this month.");
    }

    @Test
    void rejects31DaysInA30DayMonth() {
        assertThat(service.validateCcyymmdd("20240431", "DOB").returnMessage())
                .isEqualTo("DOB:Cannot have 31 days in this month.");
    }

    @Test
    void rejects30DaysInFebruary() {
        assertThat(service.validateCcyymmdd("20240230", "DOB").returnMessage())
                .isEqualTo("DOB:Cannot have 30 days in this month.");
    }

    @Test
    void reportsTheFirstFailingEditOnly() {
        // Blank year and an out of range month: EDIT-YEAR-CCYY writes WS-RETURN-MSG first.
        DateValidationResult result = service.validateCcyymmdd("    1331", "DOB");

        assertThat(result.returnMessage()).isEqualTo("DOB : Year must be supplied.");
        assertThat(result.yearFlag()).isEqualTo(DateValidationResult.FLAG_BLANK);
        assertThat(result.monthFlag()).isEqualTo(DateValidationResult.FLAG_NOT_OK);
    }

    @Test
    void rejectsANonNumericYear() {
        assertThat(service.validateCcyymmdd("20AB1231", "DOB").returnMessage())
                .isEqualTo("DOB must be 4 digit number.");
    }

    @Test
    void rejectsACenturyOtherThan19Or20() {
        assertThat(service.validateCcyymmdd("18991231", "DOB").returnMessage())
                .isEqualTo("DOB : Century is not valid.");
        assertThat(service.validateCcyymmdd("21001231", "DOB").returnMessage())
                .isEqualTo("DOB : Century is not valid.");
        assertThat(service.validateCcyymmdd("19991231", "DOB").isValid()).isTrue();
    }

    @Test
    void rejectsABlankMonth() {
        DateValidationResult result = service.validateCcyymmdd("2024  31", "DOB");

        assertThat(result.returnMessage()).isEqualTo("DOB : Month must be supplied.");
        assertThat(result.monthFlag()).isEqualTo(DateValidationResult.FLAG_BLANK);
    }

    @Test
    void rejectsAMonthOutsideOneToTwelve() {
        assertThat(service.validateCcyymmdd("20241331", "DOB").returnMessage())
                .isEqualTo("DOB: Month must be a number between 1 and 12.");
        assertThat(service.validateCcyymmdd("20240031", "DOB").returnMessage())
                .isEqualTo("DOB: Month must be a number between 1 and 12.");
    }

    @Test
    void rejectsABlankDay() {
        DateValidationResult result = service.validateCcyymmdd("202412  ", "DOB");

        assertThat(result.returnMessage()).isEqualTo("DOB : Day must be supplied.");
        assertThat(result.dayFlag()).isEqualTo(DateValidationResult.FLAG_BLANK);
    }

    @Test
    void rejectsADayOutsideOneToThirtyOne() {
        assertThat(service.validateCcyymmdd("20241232", "DOB").returnMessage())
                .isEqualTo("DOB:day must be a number between 1 and 31.");
        assertThat(service.validateCcyymmdd("20241200", "DOB").returnMessage())
                .isEqualTo("DOB:day must be a number between 1 and 31.");
    }

    @Test
    void rejectsADateOfBirthThatIsNotInThePast() {
        LocalDate today = LocalDate.of(2024, 6, 15);

        assertThat(service.validateDateOfBirth("20240614", "DOB", today).isValid()).isTrue();
        assertThat(service.validateDateOfBirth("20240615", "DOB", today).returnMessage())
                .isEqualTo("DOB:cannot be in the future ");
        assertThat(service.validateDateOfBirth("20240616", "DOB", today).returnMessage())
                .isEqualTo("DOB:cannot be in the future ");
    }

    @Test
    void languageEnvironmentAcceptsAValidDate() {
        LanguageEnvironmentDateResult result =
                service.validateWithLanguageEnvironment("20240229", "YYYYMMDD");

        assertThat(result.isValid()).isTrue();
        assertThat(result.severity()).isZero();
        assertThat(result.messageNumber()).isZero();
        assertThat(result.resultText().trim()).isEqualTo("Date is valid");
        assertThat(result.message()).hasSize(80);
        assertThat(result.message())
                .startsWith("0000Mesg Code: 0000 Date is valid  ")
                .contains("TstDate: 20240229")
                .contains("Mask used:YYYYMMDD");
    }

    @Test
    void languageEnvironmentReportsInsufficientData() {
        LanguageEnvironmentDateResult result =
                service.validateWithLanguageEnvironment("2024", "YYYYMMDD");

        assertThat(result.severity()).isEqualTo(3);
        assertThat(result.messageNumber()).isEqualTo(2507);
        assertThat(result.resultText().trim()).isEqualTo("Insufficient");
    }

    @Test
    void languageEnvironmentReportsABadPictureString() {
        LanguageEnvironmentDateResult result =
                service.validateWithLanguageEnvironment("20240229", "MMDDYYYY");

        assertThat(result.messageNumber()).isEqualTo(2518);
        assertThat(result.resultText().trim()).isEqualTo("Bad Pic String");
    }

    @Test
    void languageEnvironmentReportsNonNumericData() {
        assertThat(service.validateWithLanguageEnvironment("2024AB29", "YYYYMMDD").messageNumber())
                .isEqualTo(2520);
    }

    @Test
    void languageEnvironmentReportsAnInvalidMonth() {
        assertThat(service.validateWithLanguageEnvironment("20241329", "YYYYMMDD").messageNumber())
                .isEqualTo(2517);
    }

    @Test
    void languageEnvironmentReportsAnUnsupportedRange() {
        assertThat(service.validateWithLanguageEnvironment("15811231", "YYYYMMDD").messageNumber())
                .isEqualTo(2513);
    }

    @Test
    void languageEnvironmentReportsAYearInEraOfZero() {
        assertThat(service.validateWithLanguageEnvironment("00000101", "YYYYMMDD").messageNumber())
                .isEqualTo(2521);
    }

    @Test
    void languageEnvironmentReportsADateValueError() {
        assertThat(service.validateWithLanguageEnvironment("20240231", "YYYYMMDD").messageNumber())
                .isEqualTo(2508);
    }
}
