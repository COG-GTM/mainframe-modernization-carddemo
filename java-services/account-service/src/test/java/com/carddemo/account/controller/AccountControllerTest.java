package com.carddemo.account.controller;

import com.carddemo.account.dto.AccountDto;
import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.exception.GlobalExceptionHandler;
import com.carddemo.account.exception.ResourceNotFoundException;
import com.carddemo.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountDto sampleDto() {
        return AccountDto.builder()
                .accountId(10000000001L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("1500.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1500.00"))
                .openDate("2020-03-15")
                .expirationDate("2026-03-15")
                .reissueDate("2024-03-15")
                .currentCycleCredit(new BigDecimal("200.00"))
                .currentCycleDebit(new BigDecimal("150.00"))
                .addressZip("60601")
                .groupId("GRP001")
                .build();
    }

    @Test
    void getAccount_existingId_returnsOk() throws Exception {
        when(accountService.getAccountById(10000000001L)).thenReturn(sampleDto());

        mockMvc.perform(get("/api/accounts/10000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(10000000001L))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.creditLimit").value(5000.00))
                .andExpect(jsonPath("$.openDate").value("2020-03-15"));
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        when(accountService.getAccountById(99999999999L))
                .thenThrow(new ResourceNotFoundException("Account ID NOT found"));

        mockMvc.perform(get("/api/accounts/99999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account ID NOT found"));
    }

    @Test
    void listAccounts_returnsPaginatedResults() throws Exception {
        Page<AccountDto> page = new PageImpl<>(
                List.of(sampleDto()), PageRequest.of(0, 10), 1);
        when(accountService.listAccounts(any())).thenReturn(page);

        mockMvc.perform(get("/api/accounts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountId").value(10000000001L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updateAccount_validRequest_returnsUpdatedAccount() throws Exception {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .activeStatus("N")
                .creditLimit(new BigDecimal("8000.00"))
                .cashCreditLimit(new BigDecimal("2500.00"))
                .openDate("2020-03-15")
                .expirationDate("2028-03-15")
                .reissueDate("2026-03-15")
                .groupId("GRP002")
                .build();

        AccountDto updatedDto = AccountDto.builder()
                .accountId(10000000001L)
                .activeStatus("N")
                .currentBalance(new BigDecimal("1500.00"))
                .creditLimit(new BigDecimal("8000.00"))
                .cashCreditLimit(new BigDecimal("2500.00"))
                .openDate("2020-03-15")
                .expirationDate("2028-03-15")
                .reissueDate("2026-03-15")
                .currentCycleCredit(new BigDecimal("200.00"))
                .currentCycleDebit(new BigDecimal("150.00"))
                .addressZip("60601")
                .groupId("GRP002")
                .build();

        when(accountService.updateAccount(eq(10000000001L), any(AccountUpdateRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/accounts/10000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value("N"))
                .andExpect(jsonPath("$.creditLimit").value(8000.00))
                .andExpect(jsonPath("$.groupId").value("GRP002"));
    }

    @Test
    void updateAccount_invalidStatus_returns400() throws Exception {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .activeStatus("X")
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1500.00"))
                .openDate("2020-03-15")
                .expirationDate("2026-03-15")
                .build();

        mockMvc.perform(put("/api/accounts/10000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void updateAccount_negativeCreditLimit_returns400() throws Exception {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .activeStatus("Y")
                .creditLimit(new BigDecimal("-100.00"))
                .cashCreditLimit(new BigDecimal("1500.00"))
                .openDate("2020-03-15")
                .expirationDate("2026-03-15")
                .build();

        mockMvc.perform(put("/api/accounts/10000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void updateAccount_invalidDateFormat_returns400() throws Exception {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .activeStatus("Y")
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1500.00"))
                .openDate("03/15/2020")
                .expirationDate("2026-03-15")
                .build();

        mockMvc.perform(put("/api/accounts/10000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getAccountByCardNumber_existingCard_returnsOk() throws Exception {
        when(accountService.getAccountByCardNumber("4111111111111111")).thenReturn(sampleDto());

        mockMvc.perform(get("/api/accounts/card/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(10000000001L));
    }

    @Test
    void getAccountByCardNumber_notFound_returns404() throws Exception {
        when(accountService.getAccountByCardNumber("9999999999999999"))
                .thenThrow(new ResourceNotFoundException(
                        "Did not find this account in account card xref file"));

        mockMvc.perform(get("/api/accounts/card/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Did not find this account in account card xref file"));
    }
}
