package com.carddemo.payment.controller;

import com.carddemo.payment.dto.PaymentResponse;
import com.carddemo.payment.exception.GlobalExceptionHandler;
import com.carddemo.payment.exception.PaymentException;
import com.carddemo.payment.exception.ResourceNotFoundException;
import com.carddemo.payment.model.Account;
import com.carddemo.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getBalance_success() throws Exception {
        Account account = new Account();
        account.setAcctId(12345678901L);
        account.setCurrentBalance(new BigDecimal("1500.00"));
        account.setActiveStatus("Y");

        when(paymentService.getAccountBalance(12345678901L)).thenReturn(account);

        mockMvc.perform(get("/api/payments/balance/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(12345678901L))
                .andExpect(jsonPath("$.currentBalance").value(1500.00))
                .andExpect(jsonPath("$.activeStatus").value("Y"));
    }

    @Test
    void getBalance_notFound() throws Exception {
        when(paymentService.getAccountBalance(99999999999L))
                .thenThrow(new ResourceNotFoundException("Account ID NOT found..."));

        mockMvc.perform(get("/api/payments/balance/99999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account ID NOT found..."));
    }

    @Test
    void processBillPayment_success() throws Exception {
        PaymentResponse response = new PaymentResponse(
                "0000000000000101",
                12345678901L,
                new BigDecimal("1500.00"),
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                "Payment successful. Your Transaction ID is 0000000000000101."
        );
        when(paymentService.processBillPayment(any())).thenReturn(response);

        String requestBody = "{\"accountId\": 12345678901}";

        mockMvc.perform(post("/api/payments/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000101"))
                .andExpect(jsonPath("$.accountId").value(12345678901L))
                .andExpect(jsonPath("$.amountPaid").value(1500.00))
                .andExpect(jsonPath("$.newBalance").value(0))
                .andExpect(jsonPath("$.message").value("Payment successful. Your Transaction ID is 0000000000000101."));
    }

    @Test
    void processBillPayment_zeroBalance() throws Exception {
        when(paymentService.processBillPayment(any()))
                .thenThrow(new PaymentException("You have nothing to pay..."));

        String requestBody = "{\"accountId\": 12345678902}";

        mockMvc.perform(post("/api/payments/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You have nothing to pay..."));
    }

    @Test
    void processBillPayment_accountNotFound() throws Exception {
        when(paymentService.processBillPayment(any()))
                .thenThrow(new ResourceNotFoundException("Account ID NOT found..."));

        String requestBody = "{\"accountId\": 99999999999}";

        mockMvc.perform(post("/api/payments/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account ID NOT found..."));
    }

    @Test
    void processBillPayment_missingAccountId() throws Exception {
        String requestBody = "{}";

        mockMvc.perform(post("/api/payments/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
