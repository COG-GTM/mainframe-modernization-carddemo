package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.*;
import com.carddemo.transaction.exception.TransactionNotFoundException;
import com.carddemo.transaction.exception.TransactionValidationException;
import com.carddemo.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for TransactionController REST endpoints.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- GET /api/v1/transactions ---

    @Test
    void listTransactions_returnsOk() throws Exception {
        TransactionListResponse response = new TransactionListResponse(
                List.of(), 1, 10, 0, 0, false, false);
        when(transactionService.listTransactions(0, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void listTransactions_withNonNumericFilter_returns400() throws Exception {
        when(transactionService.listTransactions(eq(0), isNull(), eq("ABC")))
                .thenThrow(new TransactionValidationException("Tran ID must be Numeric ..."));

        mockMvc.perform(get("/api/v1/transactions").param("tranId", "ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tran ID must be Numeric ..."));
    }

    // --- GET /api/v1/transactions/{id} ---

    @Test
    void viewTransaction_found_returnsOk() throws Exception {
        TransactionResponse response = new TransactionResponse(
                "0000000000000001", "01", 1234, "ONLINE", "Test",
                new BigDecimal("100.00"), 123456789L, "Merchant", "City", "12345",
                "4567890123456789", "2024-01-15", "2024-01-15");
        when(transactionService.viewTransaction("0000000000000001")).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value("0000000000000001"))
                .andExpect(jsonPath("$.tranDesc").value("Test"));
    }

    @Test
    void viewTransaction_notFound_returns404() throws Exception {
        when(transactionService.viewTransaction("9999999999999999"))
                .thenThrow(new TransactionNotFoundException("9999999999999999"));

        mockMvc.perform(get("/api/v1/transactions/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction ID NOT found..."));
    }

    @Test
    void viewTransaction_emptyId_returns400() throws Exception {
        when(transactionService.viewTransaction(" "))
                .thenThrow(new TransactionValidationException("Tran ID can NOT be empty..."));

        mockMvc.perform(get("/api/v1/transactions/ "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tran ID can NOT be empty..."));
    }

    // --- POST /api/v1/transactions ---

    @Test
    void addTransaction_valid_returns201() throws Exception {
        TransactionAddRequest request = new TransactionAddRequest(
                "12345678901", null, "01", "1234", "ONLINE", "Test purchase",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Test Merchant", "New York", "10001", true);

        TransactionResponse response = new TransactionResponse(
                "0000000000000001", "01", 1234, "ONLINE", "Test purchase",
                new BigDecimal("100.00"), 123456789L, "Test Merchant", "New York", "10001",
                "4567890123456789", "2024-01-15", "2024-01-15");
        when(transactionService.addTransaction(any(TransactionAddRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").value("0000000000000001"));
    }

    @Test
    void addTransaction_validationError_returns400() throws Exception {
        TransactionAddRequest request = new TransactionAddRequest(
                null, null, "01", "1234", "ONLINE", "Test",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345", true);

        when(transactionService.addTransaction(any(TransactionAddRequest.class)))
                .thenThrow(new TransactionValidationException(
                        "Account or Card Number must be entered..."));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Account or Card Number must be entered..."));
    }

    // --- POST /api/v1/transactions/bill-payment ---

    @Test
    void processBillPayment_valid_returns201() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest("12345678901", true);
        BillPaymentResponse response = new BillPaymentResponse(
                "0000000000000099", new BigDecimal("500.00"), BigDecimal.ZERO,
                "Payment successful.");
        when(transactionService.processBillPayment(any(BillPaymentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions/bill-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("0000000000000099"))
                .andExpect(jsonPath("$.amountPaid").value(500.00));
    }

    @Test
    void processBillPayment_validationError_returns400() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest("", true);
        when(transactionService.processBillPayment(any(BillPaymentRequest.class)))
                .thenThrow(new TransactionValidationException("Acct ID can NOT be empty..."));

        mockMvc.perform(post("/api/v1/transactions/bill-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Acct ID can NOT be empty..."));
    }
}
