package com.carddemo.accountservice.controller;

import com.carddemo.accountservice.dto.AccountViewResponse;
import com.carddemo.accountservice.dto.AccountViewResponse.AccountDetails;
import com.carddemo.accountservice.dto.AccountViewResponse.CardInfo;
import com.carddemo.accountservice.dto.AccountViewResponse.CustomerDetails;
import com.carddemo.accountservice.exception.AccountNotFoundException;
import com.carddemo.accountservice.exception.AccountValidationException;
import com.carddemo.accountservice.exception.CardXrefNotFoundException;
import com.carddemo.accountservice.exception.CustomerNotFoundException;
import com.carddemo.accountservice.exception.GlobalExceptionHandler;
import com.carddemo.accountservice.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private AccountViewResponse buildTestResponse() {
        AccountDetails account = new AccountDetails(
                12345678901L, "Y", new BigDecimal("1000.00"),
                new BigDecimal("5000.00"), new BigDecimal("2000.00"),
                "2020-01-15", "2025-12-31", "2023-06-01",
                new BigDecimal("500.00"), new BigDecimal("200.00"),
                "10001", "GRP001");
        CustomerDetails customer = new CustomerDetails(
                123456789L, "John", "M", "Doe",
                "123 Main St", "Apt 4B", "",
                "NY", "USA", "10001",
                "(555)123-4567", "(555)987-6543",
                "123456789", "DL12345678", "1985-06-15",
                "EFT001", "Y", 750);
        CardInfo card = new CardInfo("4111111111111111");
        return new AccountViewResponse(account, customer, card);
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns 200 with account details")
    void getAccountSuccess() throws Exception {
        when(accountService.viewAccount(12345678901L)).thenReturn(buildTestResponse());

        mockMvc.perform(get("/api/v1/accounts/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.acctId").value(12345678901L))
                .andExpect(jsonPath("$.account.activeStatus").value("Y"))
                .andExpect(jsonPath("$.customer.firstName").value("John"))
                .andExpect(jsonPath("$.customer.lastName").value("Doe"))
                .andExpect(jsonPath("$.card.cardNumber").value("4111111111111111"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns 404 when xref not found")
    void getAccountXrefNotFound() throws Exception {
        when(accountService.viewAccount(12345678901L))
                .thenThrow(new CardXrefNotFoundException());

        mockMvc.perform(get("/api/v1/accounts/12345678901"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Did not find this account in account card xref file"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns 404 when account not found")
    void getAccountNotFound() throws Exception {
        when(accountService.viewAccount(12345678901L))
                .thenThrow(new AccountNotFoundException());

        mockMvc.perform(get("/api/v1/accounts/12345678901"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Did not find this account in account master file"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns 404 when customer not found")
    void getAccountCustomerNotFound() throws Exception {
        when(accountService.viewAccount(12345678901L))
                .thenThrow(new CustomerNotFoundException());

        mockMvc.perform(get("/api/v1/accounts/12345678901"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Did not find associated customer in master file"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns 400 for validation error")
    void getAccountValidationError() throws Exception {
        when(accountService.viewAccount(0L))
                .thenThrow(new AccountValidationException(
                        "Account number must be a non zero 11 digit number"));

        mockMvc.perform(get("/api/v1/accounts/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Account number must be a non zero 11 digit number"));
    }

    @Test
    @DisplayName("PUT /api/v1/accounts/{id} returns 200 on successful update")
    void updateAccountSuccess() throws Exception {
        when(accountService.updateAccount(eq(12345678901L), any())).thenReturn(buildTestResponse());

        String requestBody = """
                {
                    "activeStatus": "N",
                    "creditLimit": 10000.00
                }
                """;

        mockMvc.perform(put("/api/v1/accounts/12345678901")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.acctId").value(12345678901L));
    }

    @Test
    @DisplayName("PUT /api/v1/accounts/{id} returns 400 for multiple validation errors")
    void updateAccountValidationErrors() throws Exception {
        when(accountService.updateAccount(eq(12345678901L), any()))
                .thenThrow(new AccountValidationException(
                        List.of("Account Active Status must be Y or N",
                                "SSN: should not be 000, 666, or between 900 and 999")));

        String requestBody = """
                {
                    "activeStatus": "X",
                    "ssn": "000123456"
                }
                """;

        mockMvc.perform(put("/api/v1/accounts/12345678901")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]")
                        .value("Account Active Status must be Y or N"));
    }
}
