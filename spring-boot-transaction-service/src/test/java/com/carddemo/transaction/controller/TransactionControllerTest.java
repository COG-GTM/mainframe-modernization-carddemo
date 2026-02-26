package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.CardLookupResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.exception.ValidationException;
import com.carddemo.transaction.service.CardLookupService;
import com.carddemo.transaction.service.TransactionService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for TransactionController.
 * Tests the REST API endpoints that replace the CICS transaction interface.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private CardLookupService cardLookupService;

    /**
     * Tests POST /api/v1/transactions - successful creation.
     * Replaces: COTRN02C CT02 ENTER key -> ADD-TRANSACTION -> WRITE-TRANSACT-FILE (NORMAL).
     */
    @Test
    @WithMockUser
    void createTransaction_success() throws Exception {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("0000000000000004");
        response.setCardNumber("4111111111111111");
        response.setTransactionTypeCd("01");
        response.setTransactionCatCd(5001);
        response.setSource("ONLINE");
        response.setDescription("Test purchase");
        response.setAmount(new BigDecimal("-150.75"));
        response.setOriginationDate("2024-01-15");
        response.setProcessingDate("2024-01-15");
        response.setMerchantId(123456789);
        response.setMerchantName("Test Merchant");
        response.setMerchantCity("Seattle");
        response.setMerchantZip("98101");
        response.setMessage("Transaction added successfully. Your Tran ID is 0000000000000004.");

        when(transactionService.createTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "cardNumber": "4111111111111111",
                                    "transactionTypeCd": "01",
                                    "transactionCatCd": 5001,
                                    "source": "ONLINE",
                                    "description": "Test purchase",
                                    "amount": -150.75,
                                    "originationDate": "2024-01-15",
                                    "processingDate": "2024-01-15",
                                    "merchantId": 123456789,
                                    "merchantName": "Test Merchant",
                                    "merchantCity": "Seattle",
                                    "merchantZip": "98101"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("0000000000000004"))
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.message").value(
                        "Transaction added successfully. Your Tran ID is 0000000000000004."));
    }

    /**
     * Tests POST /api/v1/transactions - validation error (missing required fields).
     * Replaces: VALIDATE-INPUT-DATA-FIELDS -> "Type CD can NOT be empty" etc.
     */
    @Test
    @WithMockUser
    void createTransaction_validationError() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    /**
     * Tests POST /api/v1/transactions - card not found.
     * Replaces: READ-CCXREF-FILE -> DFHRESP(NOTFND) -> "Card Number NOT found".
     */
    @Test
    @WithMockUser
    void createTransaction_cardNotFound() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new ResourceNotFoundException("Card Number NOT found"));

        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "cardNumber": "9999999999999999",
                                    "transactionTypeCd": "01",
                                    "transactionCatCd": 5001,
                                    "source": "ONLINE",
                                    "description": "Test purchase",
                                    "amount": -150.75,
                                    "originationDate": "2024-01-15",
                                    "processingDate": "2024-01-15",
                                    "merchantId": 123456789,
                                    "merchantName": "Test Merchant",
                                    "merchantCity": "Seattle",
                                    "merchantZip": "98101"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card Number NOT found"));
    }

    /**
     * Tests POST /api/v1/transactions - neither account nor card provided.
     * Replaces: VALIDATE-INPUT-KEY-FIELDS -> "Account or Card Number must be entered".
     */
    @Test
    @WithMockUser
    void createTransaction_noIdentifier() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new ValidationException("Account or Card Number must be entered"));

        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "transactionTypeCd": "01",
                                    "transactionCatCd": 5001,
                                    "source": "ONLINE",
                                    "description": "Test purchase",
                                    "amount": -150.75,
                                    "originationDate": "2024-01-15",
                                    "processingDate": "2024-01-15",
                                    "merchantId": 123456789,
                                    "merchantName": "Test Merchant",
                                    "merchantCity": "Seattle",
                                    "merchantZip": "98101"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account or Card Number must be entered"));
    }

    /**
     * Tests GET /api/v1/transactions/{id} - successful retrieval.
     * Replaces: COTRN01C CT01 -> READ-TRANSACT-FILE (NORMAL).
     */
    @Test
    @WithMockUser
    void getTransaction_success() throws Exception {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("0000000000000001");
        response.setDescription("Test transaction");

        when(transactionService.getTransaction("0000000000000001")).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"));
    }

    /**
     * Tests GET /api/v1/transactions/{id} - not found.
     * Replaces: READ-TRANSACT-FILE -> DFHRESP(NOTFND) -> "Transaction ID NOT found".
     */
    @Test
    @WithMockUser
    void getTransaction_notFound() throws Exception {
        when(transactionService.getTransaction("9999999999999999"))
                .thenThrow(new ResourceNotFoundException("Transaction ID NOT found"));

        mockMvc.perform(get("/api/v1/transactions/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction ID NOT found"));
    }

    /**
     * Tests GET /api/v1/transactions/last - get most recent transaction.
     * Replaces: COTRN02C PF5 -> COPY-LAST-TRAN-DATA.
     */
    @Test
    @WithMockUser
    void getLastTransaction_success() throws Exception {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("0000000000000003");
        response.setDescription("Latest transaction");

        when(transactionService.getLastTransaction()).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000003"));
    }

    /**
     * Tests GET /api/v1/cards/by-account/{accountId} - account lookup.
     * Replaces: COTRN02C -> READ-CXACAIX-FILE.
     */
    @Test
    @WithMockUser
    void lookupByAccountId_success() throws Exception {
        CardLookupResponse response = new CardLookupResponse(
                "4111111111111111", 12345678901L, 100000001L);

        when(cardLookupService.lookupByAccountId(12345678901L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cards/by-account/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.accountId").value(12345678901L));
    }

    /**
     * Tests GET /api/v1/accounts/by-card/{cardNumber} - card lookup.
     * Replaces: COTRN02C -> READ-CCXREF-FILE.
     */
    @Test
    @WithMockUser
    void lookupByCardNumber_success() throws Exception {
        CardLookupResponse response = new CardLookupResponse(
                "4111111111111111", 12345678901L, 100000001L);

        when(cardLookupService.lookupByCardNumber("4111111111111111")).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/by-card/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.accountId").value(12345678901L));
    }

    /**
     * Tests that unauthenticated requests are rejected.
     * Replaces: COBOL EIBCALEN=0 check -> redirect to COSGN00C sign-on.
     */
    @Test
    void createTransaction_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
