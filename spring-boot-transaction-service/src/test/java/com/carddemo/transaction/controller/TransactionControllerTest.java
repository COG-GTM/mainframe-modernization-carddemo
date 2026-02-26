package com.carddemo.transaction.controller;

import com.carddemo.transaction.config.SecurityConfig;
import com.carddemo.transaction.dto.AddTransactionResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.exception.CardNotFoundException;
import com.carddemo.transaction.exception.GlobalExceptionHandler;
import com.carddemo.transaction.exception.TransactionNotFoundException;
import com.carddemo.transaction.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the TransactionController REST endpoints.
 *
 * Tests the REST API that replaces the CICS BMS screen interactions:
 *   POST /api/v1/transactions       -> ENTER key + Y confirm
 *   GET  /api/v1/transactions/{id}  -> Direct record lookup
 *   GET  /api/v1/transactions/latest -> PF5 (Copy Last)
 */
@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        TransactionControllerTest.MockServiceConfig.class})
class TransactionControllerTest {

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public TransactionService transactionService() {
            return Mockito.mock(TransactionService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(transactionService);
    }

    @Test
    @DisplayName("POST /api/v1/transactions - successful creation (201)")
    void addTransaction_success() throws Exception {
        AddTransactionResponse mockResponse = new AddTransactionResponse(
                42L, "Transaction added successfully. Your Tran ID is 42.");

        when(transactionService.addTransaction(any())).thenReturn(mockResponse);

        String requestJson = """
                {
                    "cardNumber": "4000123456789010",
                    "typeCode": "01",
                    "categoryCode": 5411,
                    "source": "ONLINE",
                    "description": "Test purchase at store",
                    "amount": -125.50,
                    "originatedDate": "2026-02-24",
                    "processedDate": "2026-02-24",
                    "merchantId": 123456789,
                    "merchantName": "Test Store",
                    "merchantCity": "Seattle",
                    "merchantZip": "98101"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(42))
                .andExpect(jsonPath("$.message").value(
                        "Transaction added successfully. Your Tran ID is 42."));
    }

    @Test
    @DisplayName("POST /api/v1/transactions - validation error: missing required fields (400)")
    void addTransaction_validationError() throws Exception {
        // Replaces: VALIDATE-INPUT-DATA-FIELDS error messages
        String requestJson = """
                {
                    "typeCode": "",
                    "source": ""
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions - card not found (404)")
    void addTransaction_cardNotFound() throws Exception {
        // Replaces: DFHRESP(NOTFND) in READ-CCXREF-FILE
        when(transactionService.addTransaction(any()))
                .thenThrow(new CardNotFoundException("9999999999999999"));

        String requestJson = """
                {
                    "cardNumber": "9999999999999999",
                    "typeCode": "01",
                    "categoryCode": 5411,
                    "source": "ONLINE",
                    "description": "Test",
                    "amount": -10.00,
                    "originatedDate": "2026-02-24",
                    "processedDate": "2026-02-24",
                    "merchantId": 123456789,
                    "merchantName": "Store",
                    "merchantCity": "City",
                    "merchantZip": "12345"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card Number NOT found: 9999999999999999"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} - found (200)")
    void getTransaction_found() throws Exception {
        TransactionResponse mockResponse = createSampleResponse(1L);
        when(transactionService.getTransaction(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.typeCode").value("01"))
                .andExpect(jsonPath("$.merchantName").value("Test Store"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} - not found (404)")
    void getTransaction_notFound() throws Exception {
        // Replaces: DFHRESP(NOTFND) in STARTBR-TRANSACT-FILE
        when(transactionService.getTransaction(999L))
                .thenThrow(new TransactionNotFoundException(999L));

        mockMvc.perform(get("/api/v1/transactions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction ID NOT found: 999"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/latest - found (200)")
    void getLatestTransaction_found() throws Exception {
        // Replaces: PF5 COPY-LAST-TRAN-DATA flow
        TransactionResponse mockResponse = createSampleResponse(100L);
        when(transactionService.getLatestTransaction()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/transactions/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/transactions - invalid amount format (400)")
    void addTransaction_invalidAmountFormat() throws Exception {
        // Replaces: 'Amount should be in format -99999999.99'
        String requestJson = """
                {
                    "cardNumber": "4000123456789010",
                    "typeCode": "01",
                    "categoryCode": 5411,
                    "source": "ONLINE",
                    "description": "Test",
                    "amount": 999999999.999,
                    "originatedDate": "2026-02-24",
                    "processedDate": "2026-02-24",
                    "merchantId": 123456789,
                    "merchantName": "Store",
                    "merchantCity": "City",
                    "merchantZip": "12345"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/transactions - missing both accountId and cardNumber (400)")
    void addTransaction_missingAccountAndCard() throws Exception {
        // Replaces: "Account or Card Number must be entered..."
        String requestJson = """
                {
                    "typeCode": "01",
                    "categoryCode": 5411,
                    "source": "ONLINE",
                    "description": "Test",
                    "amount": -10.00,
                    "originatedDate": "2026-02-24",
                    "processedDate": "2026-02-24",
                    "merchantId": 123456789,
                    "merchantName": "Store",
                    "merchantCity": "City",
                    "merchantZip": "12345"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    private TransactionResponse createSampleResponse(Long id) {
        TransactionResponse r = new TransactionResponse();
        r.setTransactionId(id);
        r.setTypeCode("01");
        r.setCategoryCode(5411);
        r.setSource("ONLINE");
        r.setDescription("Test purchase");
        r.setAmount(new BigDecimal("50.00"));
        r.setMerchantId(123456789L);
        r.setMerchantName("Test Store");
        r.setMerchantCity("Seattle");
        r.setMerchantZip("98101");
        r.setCardNumber("4000123456789010");
        r.setOriginatedTs(LocalDateTime.of(2026, 2, 24, 0, 0));
        r.setProcessedTs(LocalDateTime.of(2026, 2, 24, 0, 0));
        return r;
    }
}
