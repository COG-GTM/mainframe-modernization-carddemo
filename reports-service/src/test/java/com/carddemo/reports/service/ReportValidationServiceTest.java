package com.carddemo.reports.service;

import com.carddemo.reports.dto.ReportRequest;
import com.carddemo.reports.exception.ReportValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportValidationServiceTest {

    private ReportValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ReportValidationService();
    }

    // --- Report type validation ---

    @Test
    void emptyReportType_throwsValidationError() {
        ReportRequest request = new ReportRequest("", null, null, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertEquals("Please enter Report Type ...", ex.getMessage());
    }

    @Test
    void nullReportType_throwsValidationError() {
        ReportRequest request = new ReportRequest(null, null, null, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertEquals("Please enter Report Type ...", ex.getMessage());
    }

    @Test
    void invalidReportType_throwsValidationError() {
        ReportRequest request = new ReportRequest("04", null, null, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertEquals("Report Type must be 01, 02, or 03", ex.getMessage());
    }

    // --- Monthly report validation ---

    @Test
    void monthly_validRequest_computesCorrectDateRange() {
        ReportRequest request = new ReportRequest("01", 6, 2025, null, null);
        ReportValidationService.DateRange range =
                validationService.validateAndComputeDateRange(request);
        assertEquals(LocalDate.of(2025, 6, 1), range.startDate());
        assertEquals(LocalDate.of(2025, 6, 30), range.endDate());
    }

    @Test
    void monthly_february_computesCorrectLastDay() {
        ReportRequest request = new ReportRequest("01", 2, 2024, null, null);
        ReportValidationService.DateRange range =
                validationService.validateAndComputeDateRange(request);
        assertEquals(LocalDate.of(2024, 2, 1), range.startDate());
        assertEquals(LocalDate.of(2024, 2, 29), range.endDate()); // 2024 is leap year
    }

    @Test
    void monthly_december_computesCorrectLastDay() {
        ReportRequest request = new ReportRequest("01", 12, 2025, null, null);
        ReportValidationService.DateRange range =
                validationService.validateAndComputeDateRange(request);
        assertEquals(LocalDate.of(2025, 12, 1), range.startDate());
        assertEquals(LocalDate.of(2025, 12, 31), range.endDate());
    }

    @Test
    void monthly_missingMonth_throwsValidationError() {
        ReportRequest request = new ReportRequest("01", null, 2025, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("valid start month")));
    }

    @Test
    void monthly_invalidMonth_throwsValidationError() {
        ReportRequest request = new ReportRequest("01", 13, 2025, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("valid start month")));
    }

    @Test
    void monthly_monthZero_throwsValidationError() {
        ReportRequest request = new ReportRequest("01", 0, 2025, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("valid start month")));
    }

    @Test
    void monthly_missingYear_throwsValidationError() {
        ReportRequest request = new ReportRequest("01", 6, null, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("valid start year")));
    }

    // --- Yearly report validation ---

    @Test
    void yearly_validRequest_computesJanToDecRange() {
        ReportRequest request = new ReportRequest("02", null, 2025, null, null);
        ReportValidationService.DateRange range =
                validationService.validateAndComputeDateRange(request);
        assertEquals(LocalDate.of(2025, 1, 1), range.startDate());
        assertEquals(LocalDate.of(2025, 12, 31), range.endDate());
    }

    @Test
    void yearly_missingYear_throwsValidationError() {
        ReportRequest request = new ReportRequest("02", null, null, null, null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getMessage().contains("valid start year"));
    }

    // --- Custom report validation ---

    @Test
    void custom_validDates_returnsDateRange() {
        ReportRequest request = new ReportRequest("03", null, null,
                "2025-01-01", "2025-06-30");
        ReportValidationService.DateRange range =
                validationService.validateAndComputeDateRange(request);
        assertEquals(LocalDate.of(2025, 1, 1), range.startDate());
        assertEquals(LocalDate.of(2025, 6, 30), range.endDate());
    }

    @Test
    void custom_missingStartDate_throwsValidationError() {
        ReportRequest request = new ReportRequest("03", null, null,
                null, "2025-06-30");
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("Start Date")));
    }

    @Test
    void custom_missingEndDate_throwsValidationError() {
        ReportRequest request = new ReportRequest("03", null, null,
                "2025-01-01", null);
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("End Date")));
    }

    @Test
    void custom_invalidMonthInStartDate_throwsValidationError() {
        ReportRequest request = new ReportRequest("03", null, null,
                "2025-13-01", "2025-06-30");
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("Start Date - Not a valid Month")));
    }

    @Test
    void custom_invalidDayInEndDate_throwsValidationError() {
        ReportRequest request = new ReportRequest("03", null, null,
                "2025-01-01", "2025-06-32");
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("End Date - Not a valid Day")));
    }

    @Test
    void custom_nonNumericYear_throwsValidationError() {
        ReportRequest request = new ReportRequest("03", null, null,
                "ABCD-01-01", "2025-06-30");
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("Start Date - Not a valid Year")));
    }

    @Test
    void custom_malformedDateFormat_throwsValidationError() {
        ReportRequest request = new ReportRequest("03", null, null,
                "2025/01/01", "2025-06-30");
        ReportValidationException ex = assertThrows(ReportValidationException.class,
                () -> validationService.validateAndComputeDateRange(request));
        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> e.contains("Start Date - Not a valid date")));
    }
}
