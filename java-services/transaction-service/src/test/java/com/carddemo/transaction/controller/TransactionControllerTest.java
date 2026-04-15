package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.TransactionCreateRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listTransactions_returnsOk() throws Exception {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionId("0000000000000001");
        dto.setTypeCode("01");
        dto.setAmount(new BigDecimal("45.67"));
        dto.setCardNumber("4567890123456789");

        TransactionListResponse response = new TransactionListResponse(
                List.of(dto), 0, 10, 1, 1);

        when(transactionService.listTransactions(isNull(), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listTransactions_byCardNumber_returnsFiltered() throws Exception {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionId("0000000000000001");
        dto.setCardNumber("4567890123456789");

        TransactionListResponse response = new TransactionListResponse(
                List.of(dto), 0, 10, 1, 1);

        when(transactionService.listTransactions(eq("4567890123456789"), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("cardNumber", "4567890123456789")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].cardNumber").value("4567890123456789"));
    }

    @Test
    void getTransaction_existingId_returnsOk() throws Exception {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionId("0000000000000001");
        dto.setTypeCode("01");
        dto.setDescription("Test Purchase");
        dto.setAmount(new BigDecimal("45.67"));

        when(transactionService.getTransaction("0000000000000001")).thenReturn(dto);

        mockMvc.perform(get("/api/transactions/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.typeCode").value("01"))
                .andExpect(jsonPath("$.description").value("Test Purchase"));
    }

    @Test
    void getTransaction_nonExistingId_returns404() throws Exception {
        when(transactionService.getTransaction("9999999999999999"))
                .thenThrow(new ResourceNotFoundException("Transaction ID NOT found: 9999999999999999"));

        mockMvc.perform(get("/api/transactions/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction ID NOT found: 9999999999999999"));
    }

    @Test
    void createTransaction_validRequest_returns201() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNumber("4567890123456789");
        request.setTypeCode("01");
        request.setCategoryCode(5001);
        request.setSource("ONLINE");
        request.setDescription("New Purchase");
        request.setAmount(new BigDecimal("99.99"));
        request.setMerchantId(111222333);
        request.setMerchantName("Shop");
        request.setMerchantCity("Boston");
        request.setMerchantZip("02101");

        TransactionDto responseDto = new TransactionDto();
        responseDto.setTransactionId("0000000000000006");
        responseDto.setTypeCode("01");
        responseDto.setAmount(new BigDecimal("99.99"));
        responseDto.setCardNumber("4567890123456789");

        when(transactionService.createTransaction(any(TransactionCreateRequest.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("0000000000000006"))
                .andExpect(jsonPath("$.amount").value(99.99));
    }

    @Test
    void createTransaction_missingCardNumber_returns400() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setTypeCode("01");
        request.setAmount(new BigDecimal("99.99"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_missingAmount_returns400() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setCardNumber("4567890123456789");
        request.setTypeCode("01");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
