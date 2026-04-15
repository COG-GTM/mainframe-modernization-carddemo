package com.carddemo.report.controller;

import com.carddemo.report.dto.ReportRequest;
import com.carddemo.report.dto.StatementDto;
import com.carddemo.report.dto.TransactionReportDto;
import com.carddemo.report.exception.ResourceNotFoundException;
import com.carddemo.report.service.ReportService;
import com.carddemo.report.service.StatementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @MockBean
    private StatementService statementService;

    @Test
    void generateTransactionReport_returnsOk() throws Exception {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-01-01");
        request.setEndDate("2024-01-31");

        TransactionReportDto report = new TransactionReportDto();
        report.setReportId("test-report-id");
        report.setReportName("Transaction Detail Report");
        report.setStartDate("2024-01-01");
        report.setEndDate("2024-01-31");
        report.setGeneratedAt(LocalDateTime.of(2024, 1, 31, 12, 0));
        report.setGrandTotal(new BigDecimal("231.24"));
        report.setCardGroups(Collections.emptyList());

        when(reportService.generateTransactionReport(any(ReportRequest.class)))
                .thenReturn(report);

        mockMvc.perform(post("/api/reports/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("test-report-id"))
                .andExpect(jsonPath("$.reportName").value("Transaction Detail Report"))
                .andExpect(jsonPath("$.startDate").value("2024-01-01"))
                .andExpect(jsonPath("$.endDate").value("2024-01-31"))
                .andExpect(jsonPath("$.grandTotal").value(231.24));
    }

    @Test
    void generateTransactionReport_badRequest() throws Exception {
        ReportRequest request = new ReportRequest();
        request.setStartDate("2024-01-31");
        request.setEndDate("2024-01-01");

        when(reportService.generateTransactionReport(any(ReportRequest.class)))
                .thenThrow(new IllegalArgumentException("Start date must not be after end date"));

        mockMvc.perform(post("/api/reports/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date must not be after end date"));
    }

    @Test
    void generateStatement_returnsOk() throws Exception {
        ReportRequest request = new ReportRequest();
        request.setAccountId("00000000001");

        StatementDto statement = new StatementDto();
        statement.setStatementDate(LocalDate.of(2024, 1, 31));
        statement.setAccountId("00000000001");
        statement.setCustomerName("John A Smith");
        statement.setCurrentBalance(new BigDecimal("1500.75"));
        statement.setCreditLimit(new BigDecimal("10000.00"));
        statement.setFicoScore(750);
        statement.setBeginningBalance(new BigDecimal("1500.75"));
        statement.setTotalCharges(new BigDecimal("632.54"));
        statement.setTotalPayments(new BigDecimal("500.00"));
        statement.setEndingBalance(new BigDecimal("1633.29"));

        when(statementService.generateStatement(any(ReportRequest.class)))
                .thenReturn(statement);

        mockMvc.perform(post("/api/reports/statements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("00000000001"))
                .andExpect(jsonPath("$.customerName").value("John A Smith"))
                .andExpect(jsonPath("$.ficoScore").value(750))
                .andExpect(jsonPath("$.beginningBalance").value(1500.75))
                .andExpect(jsonPath("$.totalCharges").value(632.54))
                .andExpect(jsonPath("$.totalPayments").value(500.00))
                .andExpect(jsonPath("$.endingBalance").value(1633.29));
    }

    @Test
    void generateStatement_accountNotFound() throws Exception {
        ReportRequest request = new ReportRequest();
        request.setAccountId("99999999999");

        when(statementService.generateStatement(any(ReportRequest.class)))
                .thenThrow(new ResourceNotFoundException("Account not found with id: 99999999999"));

        mockMvc.perform(post("/api/reports/statements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found with id: 99999999999"));
    }

    @Test
    void getTransactionReport_returnsOk() throws Exception {
        TransactionReportDto report = new TransactionReportDto();
        report.setReportId("existing-report-id");
        report.setReportName("Transaction Detail Report");
        report.setGrandTotal(new BigDecimal("100.00"));

        when(reportService.getReportById(eq("existing-report-id")))
                .thenReturn(report);

        mockMvc.perform(get("/api/reports/transactions/existing-report-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("existing-report-id"))
                .andExpect(jsonPath("$.grandTotal").value(100.00));
    }

    @Test
    void getTransactionReport_notFound() throws Exception {
        when(reportService.getReportById(eq("nonexistent-id")))
                .thenThrow(new ResourceNotFoundException("Report not found with id: nonexistent-id"));

        mockMvc.perform(get("/api/reports/transactions/nonexistent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Report not found with id: nonexistent-id"));
    }
}
