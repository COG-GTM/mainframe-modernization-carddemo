package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.ReportRequest;
import com.carddemo.transaction.dto.ReportResponse;
import com.carddemo.transaction.entity.ReportRequestEntity;
import com.carddemo.transaction.exception.InvalidRequestException;
import com.carddemo.transaction.repository.ReportRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportService.
 *
 * COBOL Traceability: Tests the report request logic from CORPT00C.
 */
class ReportServiceTest {

    private ReportRequestRepository reportRequestRepository;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportRequestRepository = mock(ReportRequestRepository.class);
        reportService = new ReportService(reportRequestRepository);
    }

    @Test
    void submitReportRequest_shouldCreatePendingRequest() {
        when(reportRequestRepository.save(any(ReportRequestEntity.class)))
                .thenAnswer(inv -> {
                    ReportRequestEntity entity = inv.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        ReportRequest request = new ReportRequest(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), "MONTHLY");

        ReportResponse response = reportService.submitReportRequest(request);

        assertNotNull(response);
        assertEquals(1L, response.reportId());
        assertEquals("PENDING", response.status());
        assertEquals("MONTHLY", response.reportType());
        verify(reportRequestRepository).save(any(ReportRequestEntity.class));
    }

    @Test
    void submitReportRequest_invalidDateRange_shouldThrow() {
        ReportRequest request = new ReportRequest(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1), "MONTHLY");

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () ->
                reportService.submitReportRequest(request));
        assertEquals("End date must be on or after start date", ex.getMessage());
    }

    @Test
    void submitReportRequest_invalidReportType_shouldThrow() {
        ReportRequest request = new ReportRequest(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), "INVALID");

        assertThrows(InvalidRequestException.class, () ->
                reportService.submitReportRequest(request));
    }

    @Test
    void submitReportRequest_yearlyType_shouldSucceed() {
        when(reportRequestRepository.save(any(ReportRequestEntity.class)))
                .thenAnswer(inv -> {
                    ReportRequestEntity entity = inv.getArgument(0);
                    entity.setId(2L);
                    return entity;
                });

        ReportRequest request = new ReportRequest(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "YEARLY");

        ReportResponse response = reportService.submitReportRequest(request);
        assertEquals("YEARLY", response.reportType());
    }

    @Test
    void submitReportRequest_customType_shouldSucceed() {
        when(reportRequestRepository.save(any(ReportRequestEntity.class)))
                .thenAnswer(inv -> {
                    ReportRequestEntity entity = inv.getArgument(0);
                    entity.setId(3L);
                    return entity;
                });

        ReportRequest request = new ReportRequest(
                LocalDate.of(2024, 3, 15), LocalDate.of(2024, 6, 15), "CUSTOM");

        ReportResponse response = reportService.submitReportRequest(request);
        assertEquals("CUSTOM", response.reportType());
    }
}
