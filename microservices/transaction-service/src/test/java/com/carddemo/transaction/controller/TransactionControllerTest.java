package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for TransactionController.
 * Tests REST endpoints translated from CICS transactions.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listTransactions_returnsPage() throws Exception {
        TransactionResponse resp = createSampleResponse("0000000000000001");
        Page<TransactionResponse> page = new PageImpl<>(List.of(resp));

        when(transactionService.listTransactions(isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tranId").value("0000000000000001"))
                .andExpect(jsonPath("$.content[0].tranCardNum").value("4111111111111111"));
    }

    @Test
    void listTransactions_withCardNumFilter() throws Exception {
        TransactionResponse resp = createSampleResponse("0000000000000001");
        Page<TransactionResponse> page = new PageImpl<>(List.of(resp));

        when(transactionService.listTransactions(eq("4111111111111111"), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/transactions").param("cardNum", "4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tranCardNum").value("4111111111111111"));
    }

    @Test
    void getTransaction_found() throws Exception {
        TransactionResponse resp = createSampleResponse("0000000000000001");
        when(transactionService.getTransactionById("0000000000000001"))
                .thenReturn(Optional.of(resp));

        mockMvc.perform(get("/transactions/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value("0000000000000001"))
                .andExpect(jsonPath("$.tranAmt").value(125.50));
    }

    @Test
    void getTransaction_notFound() throws Exception {
        when(transactionService.getTransactionById("9999999999999999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/9999999999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_success() throws Exception {
        TransactionResponse resp = createSampleResponse("0000000000000001");
        when(transactionService.createTransaction(any())).thenReturn(resp);

        String requestJson = """
                {
                    "tranTypeCd": "01",
                    "tranCatCd": 5001,
                    "tranSource": "ONLINE",
                    "tranDesc": "Grocery Store Purchase",
                    "tranAmt": 125.50,
                    "tranMerchantId": "100000001",
                    "tranMerchantName": "FreshMart Groceries",
                    "tranMerchantCity": "New York",
                    "tranMerchantZip": "10001",
                    "tranCardNum": "4111111111111111",
                    "tranOrigTs": "2024-01-15-10.30.00.000000"
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").value("0000000000000001"));
    }

    @Test
    void createTransaction_cardNotFound() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new IllegalArgumentException("Card number 9999 not found in cross-reference"));

        String requestJson = """
                {
                    "tranTypeCd": "01",
                    "tranCatCd": 5001,
                    "tranSource": "ONLINE",
                    "tranDesc": "Test Transaction",
                    "tranAmt": 50.00,
                    "tranMerchantId": "100000001",
                    "tranMerchantName": "Test Merchant",
                    "tranMerchantCity": "Test City",
                    "tranMerchantZip": "12345",
                    "tranCardNum": "9999999999999999",
                    "tranOrigTs": "2024-01-15-10.30.00.000000"
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Card number 9999 not found in cross-reference"));
    }

    @Test
    void batchPost_success() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalProcessed", 2);
        result.put("postedCount", 2);
        result.put("rejectedCount", 0);
        result.put("posted", List.of());
        result.put("rejected", List.of());

        when(transactionService.batchPostTransactions(any())).thenReturn(result);

        String requestJson = """
                {
                    "transactions": [
                        {
                            "tranTypeCd": "01",
                            "tranCatCd": 5001,
                            "tranSource": "BATCH",
                            "tranDesc": "Batch Transaction 1",
                            "tranAmt": 100.00,
                            "tranMerchantId": "100000001",
                            "tranMerchantName": "Merchant 1",
                            "tranMerchantCity": "City 1",
                            "tranMerchantZip": "10001",
                            "tranCardNum": "4111111111111111",
                            "tranOrigTs": "2024-01-15"
                        },
                        {
                            "tranTypeCd": "02",
                            "tranCatCd": 5002,
                            "tranSource": "BATCH",
                            "tranDesc": "Batch Transaction 2",
                            "tranAmt": 200.00,
                            "tranMerchantId": "100000002",
                            "tranMerchantName": "Merchant 2",
                            "tranMerchantCity": "City 2",
                            "tranMerchantZip": "20002",
                            "tranCardNum": "4222222222222222",
                            "tranOrigTs": "2024-01-16"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/transactions/batch-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.postedCount").value(2))
                .andExpect(jsonPath("$.rejectedCount").value(0));
    }

    @Test
    void healthCheck_returnsUp() throws Exception {
        mockMvc.perform(get("/transactions/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("transaction-service"));
    }

    private TransactionResponse createSampleResponse(String tranId) {
        TransactionResponse resp = new TransactionResponse();
        resp.setTranId(tranId);
        resp.setTranTypeCd("01");
        resp.setTranCatCd(5001);
        resp.setTranSource("ONLINE");
        resp.setTranDesc("Grocery Store Purchase");
        resp.setTranAmt(new BigDecimal("125.50"));
        resp.setTranMerchantId("100000001");
        resp.setTranMerchantName("FreshMart Groceries");
        resp.setTranMerchantCity("New York");
        resp.setTranMerchantZip("10001");
        resp.setTranCardNum("4111111111111111");
        resp.setTranOrigTs("2024-01-15-10.30.00.000000");
        resp.setTranProcTs("2024-01-15-10.30.05.000000");
        return resp;
    }
}
