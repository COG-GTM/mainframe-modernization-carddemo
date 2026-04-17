package com.carddemo.billing.controller;

import com.carddemo.billing.dto.BillPaymentRequest;
import com.carddemo.billing.dto.BillPaymentResponse;
import com.carddemo.billing.dto.ReportRequest;
import com.carddemo.billing.dto.ReportResponse;
import com.carddemo.billing.service.BillingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for BillingController.
 *
 * Validates REST endpoints that modernize COBIL00C.cbl (bill payment)
 * and CORPT00C.cbl (report submission) CICS transactions.
 */
@WebMvcTest(BillingController.class)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /billing/pay/{accountId} returns 200 on success")
    void testProcessPaymentSuccess() throws Exception {
        // Arrange
        String accountId = "00000000001";
        BillPaymentRequest request = new BillPaymentRequest(new BigDecimal("150.00"), "4111111111111111");

        BillPaymentResponse response = new BillPaymentResponse(
                "0000000000000001",
                accountId,
                new BigDecimal("150.00"),
                new BigDecimal("350.00"),
                "SUCCESS",
                Instant.parse("2024-01-15T10:30:00Z")
        );

        when(billingService.processPayment(eq(accountId), any(BillPaymentRequest.class)))
                .thenReturn(Mono.just(response));

        // Act - start async request
        MvcResult mvcResult = mockMvc.perform(post("/billing/pay/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert - dispatch the async result
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.newBalance").value(350.00))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /billing/pay/{accountId} returns 500 on saga failure")
    void testProcessPaymentFailure() throws Exception {
        // Arrange
        String accountId = "00000000001";
        BillPaymentRequest request = new BillPaymentRequest(new BigDecimal("150.00"), "4111111111111111");

        when(billingService.processPayment(eq(accountId), any(BillPaymentRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Account service unavailable")));

        // Act - start async request
        MvcResult mvcResult = mockMvc.perform(post("/billing/pay/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Assert - dispatch the async result
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED: Account service unavailable"));
    }

    @Test
    @DisplayName("POST /billing/reports/submit returns 200 on success")
    void testSubmitReportSuccess() throws Exception {
        // Arrange
        ReportRequest request = new ReportRequest("00000000001", "2024-01-01", "2024-12-31");

        ReportResponse response = new ReportResponse(
                "SUBMITTED",
                "00000000001",
                "2024-01-01",
                "2024-12-31",
                Instant.parse("2024-01-15T10:30:00Z")
        );

        when(billingService.submitReport(any(ReportRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/billing/reports/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.accountId").value("00000000001"))
                .andExpect(jsonPath("$.startDate").value("2024-01-01"))
                .andExpect(jsonPath("$.endDate").value("2024-12-31"));
    }

    @Test
    @DisplayName("GET /billing/health returns 200 with status UP")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/billing/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("billing-service"));
    }

    @Test
    @DisplayName("POST /billing/pay/{accountId} returns 400 for invalid request")
    void testProcessPaymentValidation() throws Exception {
        // Arrange - missing required fields
        String accountId = "00000000001";
        String invalidRequest = "{}";

        // Act & Assert
        mockMvc.perform(post("/billing/pay/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
