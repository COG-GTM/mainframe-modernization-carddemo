package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.BillPaymentRequest;
import com.carddemo.transaction.dto.BillPaymentResponse;
import com.carddemo.transaction.dto.CreateTransactionRequest;
import com.carddemo.transaction.dto.ReportRequest;
import com.carddemo.transaction.dto.ReportResponse;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.exception.GlobalExceptionHandler;
import com.carddemo.transaction.exception.InvalidRequestException;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.service.BillPaymentService;
import com.carddemo.transaction.service.ReportService;
import com.carddemo.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for TransactionController.
 *
 * COBOL Traceability: Tests the REST API endpoints that replace
 * COTRN00C, COTRN01C, COTRN02C, COBIL00C, and CORPT00C.
 */
class TransactionControllerTest {

    private MockMvc mockMvc;
    private TransactionService transactionService;
    private BillPaymentService billPaymentService;
    private ReportService reportService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        transactionService = mock(TransactionService.class);
        billPaymentService = mock(BillPaymentService.class);
        reportService = mock(ReportService.class);

        TransactionController controller = new TransactionController(
                transactionService, billPaymentService, reportService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Test: List transactions with default pagination.
     * COBOL Traceability: Verifies COTRN00C replacement —
     * paginated browse of TRANSACT VSAM file.
     */
    @Test
    void listTransactions_shouldReturnPaginatedList() throws Exception {
        var txn = sampleTransactionResponse();
        var response = new TransactionListResponse(
                List.of(txn), 0, 10, 1, 1, false, false);

        when(transactionService.listTransactions(
                anyInt(), anyInt(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    /**
     * Test: List transactions with account ID filter.
     * COBOL Traceability: Verifies COTRN00C filter-by-account feature.
     */
    @Test
    void listTransactions_withAccountFilter_shouldFilter() throws Exception {
        var txn = sampleTransactionResponse();
        var response = new TransactionListResponse(
                List.of(txn), 0, 10, 1, 1, false, false);

        when(transactionService.listTransactions(
                anyInt(), anyInt(), anyString(), isNull(), isNull(), isNull()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("accountId", "00000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray());
    }

    /**
     * Test: Get transaction by ID.
     * COBOL Traceability: Verifies COTRN01C replacement —
     * single READ on TRANSACT by TRAN-ID key.
     */
    @Test
    void getTransaction_shouldReturnDetail() throws Exception {
        when(transactionService.getTransaction("0000000000000001"))
                .thenReturn(sampleTransactionResponse());

        mockMvc.perform(get("/api/v1/transactions/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.amount").value(45.67));
    }

    /**
     * Test: Get non-existent transaction returns 404.
     * COBOL Traceability: Verifies DFHRESP(NOTFND) handling.
     */
    @Test
    void getTransaction_notFound_shouldReturn404() throws Exception {
        when(transactionService.getTransaction("9999999999999999"))
                .thenThrow(new ResourceNotFoundException("Transaction not found: 9999999999999999"));

        mockMvc.perform(get("/api/v1/transactions/9999999999999999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Create transaction successfully.
     * COBOL Traceability: Verifies COTRN02C replacement —
     * WRITE to TRANSACT with auto-generated ID.
     */
    @Test
    void createTransaction_shouldReturn201() throws Exception {
        when(transactionService.createTransaction(any(CreateTransactionRequest.class)))
                .thenReturn(sampleTransactionResponse());

        var request = new CreateTransactionRequest(
                "00000000001", null, "01", 5001, "POS TERM",
                "TEST PURCHASE", new BigDecimal("45.67"),
                100000001L, "TEST MERCHANT", "NEW YORK", "10001",
                null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"));
    }

    /**
     * Test: Create transaction with missing required fields.
     * COBOL Traceability: Verifies COTRN02C validation logic.
     */
    @Test
    void createTransaction_missingFields_shouldReturn400() throws Exception {
        var request = new CreateTransactionRequest(
                null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Bill payment creates transaction and updates balance.
     * COBOL Traceability: Verifies COBIL00C replacement —
     * atomic WRITE TRANSACT + REWRITE ACCTDAT.
     */
    @Test
    void processPayment_shouldReturn201() throws Exception {
        var paymentResponse = new BillPaymentResponse(
                "0000000000000011", "00000000001",
                new BigDecimal("500.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("1000.00"),
                "Bill payment processed successfully");

        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenReturn(paymentResponse);

        var request = new BillPaymentRequest("00000000001", null, new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/transactions/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("0000000000000011"))
                .andExpect(jsonPath("$.previousBalance").value(1500.00))
                .andExpect(jsonPath("$.newBalance").value(1000.00));
    }

    /**
     * Test: Bill payment with invalid account returns 404.
     * COBOL Traceability: Verifies COBIL00C NOTFND handling on ACCTDAT.
     */
    @Test
    void processPayment_accountNotFound_shouldReturn404() throws Exception {
        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Account ID NOT found: 99999999999"));

        var request = new BillPaymentRequest("99999999999", null, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/transactions/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Bill payment with zero balance returns 400.
     * COBOL Traceability: Verifies COBIL00C "nothing to pay" check.
     */
    @Test
    void processPayment_invalidAmount_shouldReturn400() throws Exception {
        when(billPaymentService.processPayment(any(BillPaymentRequest.class)))
                .thenThrow(new InvalidRequestException("You have nothing to pay"));

        var request = new BillPaymentRequest("00000000004", null, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/transactions/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Submit report request successfully.
     * COBOL Traceability: Verifies CORPT00C replacement —
     * WRITEQ TD to transient data queue.
     */
    @Test
    void requestReport_shouldReturn201() throws Exception {
        var reportResponse = new ReportResponse(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31),
                "MONTHLY", "PENDING", LocalDateTime.now());

        when(reportService.submitReportRequest(any(ReportRequest.class)))
                .thenReturn(reportResponse);

        var request = new ReportRequest(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), "MONTHLY");

        mockMvc.perform(post("/api/v1/reports/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    /**
     * Test: Report request with invalid date range.
     * COBOL Traceability: Verifies CORPT00C date validation (CSUTLDTC).
     */
    @Test
    void requestReport_invalidDateRange_shouldReturn400() throws Exception {
        when(reportService.submitReportRequest(any(ReportRequest.class)))
                .thenThrow(new InvalidRequestException("End date must be on or after start date"));

        var request = new ReportRequest(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1), "MONTHLY");

        mockMvc.perform(post("/api/v1/reports/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private TransactionResponse sampleTransactionResponse() {
        return new TransactionResponse(
                "0000000000000001", "01", 5001, "POS TERM",
                "GROCERY STORE PURCHASE", new BigDecimal("45.67"),
                100000001L, "WHOLE FOODS MARKET", "NEW YORK", "10001",
                "4111111111111111",
                LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                LocalDateTime.of(2024, 1, 15, 10, 30, 5));
    }
}
