package com.carddemo.account.controller;

import com.carddemo.account.dto.AccountRequest;
import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.dto.BalanceUpdateRequest;
import com.carddemo.account.service.AccountService;
import com.carddemo.account.service.InterestCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private InterestCalculationService interestCalculationService;

    private AccountResponse createTestResponse() {
        AccountResponse response = new AccountResponse();
        response.setAcctId("00000000001");
        response.setAcctActiveStatus("Y");
        response.setAcctCurrBal(new BigDecimal("1500.00"));
        response.setAcctCreditLimit(new BigDecimal("5000.00"));
        response.setAcctCashCreditLimit(new BigDecimal("1500.00"));
        response.setAcctOpenDate("2020-01-15");
        response.setAcctExpirationDate("2025-01-15");
        response.setAcctReissueDate("2023-01-15");
        response.setAcctCurrCycCredit(new BigDecimal("200.00"));
        response.setAcctCurrCycDebit(new BigDecimal("350.00"));
        response.setAcctAddrZip("60601");
        response.setAcctGroupId("GROUP01");
        return response;
    }

    @Test
    void healthCheck() throws Exception {
        mockMvc.perform(get("/accounts/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("account-service"));
    }

    @Test
    void getAccount_found() throws Exception {
        when(accountService.getAccount("00000000001"))
                .thenReturn(Optional.of(createTestResponse()));

        mockMvc.perform(get("/accounts/00000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value("00000000001"))
                .andExpect(jsonPath("$.acctActiveStatus").value("Y"))
                .andExpect(jsonPath("$.acctCurrBal").value(1500.00))
                .andExpect(jsonPath("$.acctCreditLimit").value(5000.00))
                .andExpect(jsonPath("$.acctAddrZip").value("60601"))
                .andExpect(jsonPath("$.acctGroupId").value("GROUP01"));
    }

    @Test
    void getAccount_notFound() throws Exception {
        when(accountService.getAccount("99999999999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/accounts/99999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAccount_success() throws Exception {
        AccountResponse updatedResponse = createTestResponse();
        updatedResponse.setAcctActiveStatus("N");
        updatedResponse.setAcctCreditLimit(new BigDecimal("7500.00"));

        when(accountService.updateAccount(eq("00000000001"), any(AccountRequest.class)))
                .thenReturn(updatedResponse);

        AccountRequest request = new AccountRequest();
        request.setAcctActiveStatus("N");
        request.setAcctCreditLimit(new BigDecimal("7500.00"));

        mockMvc.perform(put("/accounts/00000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctActiveStatus").value("N"))
                .andExpect(jsonPath("$.acctCreditLimit").value(7500.00));
    }

    @Test
    void updateAccount_badRequest() throws Exception {
        when(accountService.updateAccount(eq("00000000001"), any(AccountRequest.class)))
                .thenThrow(new IllegalArgumentException("Account active status must be Y or N"));

        AccountRequest request = new AccountRequest();
        request.setAcctActiveStatus("X");

        mockMvc.perform(put("/accounts/00000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculateInterest_success() throws Exception {
        AccountResponse response = createTestResponse();
        response.setAcctCurrBal(new BigDecimal("1528.74"));
        response.setAcctCurrCycCredit(BigDecimal.ZERO);
        response.setAcctCurrCycDebit(BigDecimal.ZERO);

        when(interestCalculationService.calculateInterest(eq("00000000001"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/accounts/00000000001/calculate-interest")
                        .param("interestRate", "22.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctCurrBal").value(1528.74))
                .andExpect(jsonPath("$.acctCurrCycCredit").value(0))
                .andExpect(jsonPath("$.acctCurrCycDebit").value(0));
    }

    @Test
    void calculateInterest_accountNotFound() throws Exception {
        when(interestCalculationService.calculateInterest(eq("99999999999"), any()))
                .thenThrow(new IllegalArgumentException("Account not found: 99999999999"));

        mockMvc.perform(post("/accounts/99999999999/calculate-interest"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBalance_success() throws Exception {
        AccountResponse response = createTestResponse();
        response.setAcctCurrBal(new BigDecimal("2000.00"));

        when(accountService.updateBalance(eq("00000000001"), any(BalanceUpdateRequest.class)))
                .thenReturn(response);

        BalanceUpdateRequest request = new BalanceUpdateRequest(new BigDecimal("500.00"));

        mockMvc.perform(put("/accounts/00000000001/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctCurrBal").value(2000.00));
    }

    @Test
    void updateBalance_accountNotFound() throws Exception {
        when(accountService.updateBalance(eq("99999999999"), any(BalanceUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("Account not found: 99999999999"));

        BalanceUpdateRequest request = new BalanceUpdateRequest(new BigDecimal("100.00"));

        mockMvc.perform(put("/accounts/99999999999/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
