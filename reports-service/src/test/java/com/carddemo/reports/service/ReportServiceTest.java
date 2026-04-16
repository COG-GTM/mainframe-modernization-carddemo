package com.carddemo.reports.service;

import com.carddemo.reports.dto.ReportRequest;
import com.carddemo.reports.dto.ReportResponse;
import com.carddemo.reports.exception.ReportNotFoundException;
import com.carddemo.reports.exception.ReportNotReadyException;
import com.carddemo.reports.model.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportGenerationService generationService;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        ReportValidationService validationService = new ReportValidationService();
        reportService = new ReportService(validationService, generationService);
    }

    @Test
    void submitReport_monthly_returnsAcceptedResponse() {
        ReportRequest request = new ReportRequest("01", 6, 2025, null, null);
        ReportResponse response = reportService.submitReport(request);

        assertNotNull(response.getId());
        assertEquals("01", response.getReportType());
        assertEquals("Monthly", response.getReportTypeName());
        assertEquals(ReportStatus.SUBMITTED, response.getStatus());
        assertNotNull(response.getJobName());
        assertTrue(response.getJobName().startsWith("TRNRPT-"));
        assertNotNull(response.getSubmittedAt());
        verify(generationService).generateReport(any());
    }

    @Test
    void submitReport_yearly_returnsAcceptedResponse() {
        ReportRequest request = new ReportRequest("02", null, 2025, null, null);
        ReportResponse response = reportService.submitReport(request);

        assertEquals("02", response.getReportType());
        assertEquals("Yearly", response.getReportTypeName());
        assertEquals(ReportStatus.SUBMITTED, response.getStatus());
    }

    @Test
    void submitReport_custom_returnsAcceptedResponse() {
        ReportRequest request = new ReportRequest("03", null, null,
                "2025-01-01", "2025-06-30");
        ReportResponse response = reportService.submitReport(request);

        assertEquals("03", response.getReportType());
        assertEquals("Custom", response.getReportTypeName());
        assertEquals(ReportStatus.SUBMITTED, response.getStatus());
    }

    @Test
    void getReportStatus_existingJob_returnsStatus() {
        ReportRequest request = new ReportRequest("01", 6, 2025, null, null);
        ReportResponse submitted = reportService.submitReport(request);

        ReportResponse status = reportService.getReportStatus(submitted.getId());
        assertEquals(submitted.getId(), status.getId());
        assertEquals(ReportStatus.SUBMITTED, status.getStatus());
    }

    @Test
    void getReportStatus_nonExistentId_throwsNotFoundException() {
        assertThrows(ReportNotFoundException.class,
                () -> reportService.getReportStatus("nonexistent-id"));
    }

    @Test
    void getReportFilePath_nonExistentId_throwsNotFoundException() {
        assertThrows(ReportNotFoundException.class,
                () -> reportService.getReportFilePath("nonexistent-id"));
    }

    @Test
    void getReportFilePath_submittedJob_throwsNotReadyException() {
        ReportRequest request = new ReportRequest("01", 6, 2025, null, null);
        ReportResponse submitted = reportService.submitReport(request);

        assertThrows(ReportNotReadyException.class,
                () -> reportService.getReportFilePath(submitted.getId()));
    }
}
