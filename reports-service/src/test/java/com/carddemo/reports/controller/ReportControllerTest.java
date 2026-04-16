package com.carddemo.reports.controller;

import com.carddemo.reports.dto.ReportResponse;
import com.carddemo.reports.exception.GlobalExceptionHandler;
import com.carddemo.reports.exception.ReportNotFoundException;
import com.carddemo.reports.exception.ReportNotReadyException;
import com.carddemo.reports.exception.ReportValidationException;
import com.carddemo.reports.model.ReportStatus;
import com.carddemo.reports.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitReport_validMonthly_returns202() throws Exception {
        ReportResponse response = buildResponse("test-id", "01", "Monthly",
                ReportStatus.SUBMITTED);
        when(reportService.submitReport(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportType":"01","month":6,"year":2025}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.reportType").value("01"))
                .andExpect(jsonPath("$.reportTypeName").value("Monthly"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void submitReport_validationError_returns400() throws Exception {
        when(reportService.submitReport(any()))
                .thenThrow(new ReportValidationException("Report Type must be 01, 02, or 03"));

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportType":"04"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void submitReport_missingReportType_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReportStatus_existingJob_returns200() throws Exception {
        ReportResponse response = buildResponse("test-id", "02", "Yearly",
                ReportStatus.COMPLETED);
        when(reportService.getReportStatus("test-id")).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getReportStatus_notFound_returns404() throws Exception {
        when(reportService.getReportStatus("bad-id"))
                .thenThrow(new ReportNotFoundException("bad-id"));

        mockMvc.perform(get("/api/v1/reports/bad-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Report not found with id: bad-id"));
    }

    @Test
    void downloadReport_notReady_returns409() throws Exception {
        when(reportService.getReportFilePath("test-id"))
                .thenThrow(new ReportNotReadyException("test-id", ReportStatus.PROCESSING));

        mockMvc.perform(get("/api/v1/reports/test-id/download"))
                .andExpect(status().isConflict());
    }

    private ReportResponse buildResponse(String id, String typeCode, String typeName,
                                         ReportStatus status) {
        ReportResponse response = new ReportResponse();
        response.setId(id);
        response.setReportType(typeCode);
        response.setReportTypeName(typeName);
        response.setStatus(status);
        response.setJobName("TRNRPT-TEST1234");
        response.setStartDate(LocalDate.of(2025, 1, 1));
        response.setEndDate(LocalDate.of(2025, 12, 31));
        response.setSubmittedAt(LocalDateTime.now());
        return response;
    }
}
