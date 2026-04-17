package com.carddemo.account.controller;

import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.dto.AccountUpdateRequest.AccountFields;
import com.carddemo.account.dto.AccountUpdateRequest.CustomerFields;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AccountController.
 * Validates that the migrated REST API reproduces the behavior of the original
 * COACTVWC.cbl (Account View) and COACTUPC.cbl (Account Update) programs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ===== Account View Tests (replaces COACTVWC.cbl / CAVW) =====

    @Test
    void getAccount_returnsCombinedAccountCustomerAndCards() throws Exception {
        // Account 80001000001 is linked to customer 100000001 and has 2 cards
        mockMvc.perform(get("/api/v1/accounts/80001000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.accountId", is(80001000001L)))
                .andExpect(jsonPath("$.account.activeStatus", is("Y")))
                .andExpect(jsonPath("$.account.currentBalance", is(1500.00)))
                .andExpect(jsonPath("$.account.creditLimit", is(10000.00)))
                .andExpect(jsonPath("$.account.cashCreditLimit", is(5000.00)))
                .andExpect(jsonPath("$.account.groupId", is("GROUP001")))
                .andExpect(jsonPath("$.customer.custId", is(100000001)))
                .andExpect(jsonPath("$.customer.firstName", is("John")))
                .andExpect(jsonPath("$.customer.lastName", is("Smith")))
                .andExpect(jsonPath("$.customer.addrStateCd", is("NY")))
                .andExpect(jsonPath("$.customer.ficoCreditScore", is(750)))
                .andExpect(jsonPath("$.linkedCardNumbers", hasSize(2)));
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/99999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")));
    }

    @Test
    void getAccount_withSingleCard() throws Exception {
        // Account 80001000002 has 1 card linked
        mockMvc.perform(get("/api/v1/accounts/80001000002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.accountId", is(80001000002L)))
                .andExpect(jsonPath("$.customer.custId", is(100000002)))
                .andExpect(jsonPath("$.customer.firstName", is("Jane")))
                .andExpect(jsonPath("$.linkedCardNumbers", hasSize(1)));
    }

    @Test
    void getAccount_withNullCustomerFields() throws Exception {
        // Account 80001000005 has customer with null middleName
        mockMvc.perform(get("/api/v1/accounts/80001000005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.middleName", nullValue()));
    }

    // ===== Account Update Tests (replaces COACTUPC.cbl / CAUP) =====

    @Test
    void updateAccount_updatesAccountAndCustomerAtomically() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest(
                new AccountFields(
                        "N", new BigDecimal("2000.00"), null, null,
                        null, null, null, null, null, "NEWGROUP"
                ),
                new CustomerFields(
                        "Jonathan", null, null, "456 New Address",
                        null, null, null, null, null,
                        null, null, null, null, null, null, null, null
                )
        );

        mockMvc.perform(put("/api/v1/accounts/80001000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.activeStatus", is("N")))
                .andExpect(jsonPath("$.account.currentBalance", is(2000.00)))
                .andExpect(jsonPath("$.account.groupId", is("NEWGROUP")))
                // Customer fields should also be updated in the same transaction
                .andExpect(jsonPath("$.customer.firstName", is("Jonathan")))
                .andExpect(jsonPath("$.customer.addrLine1", is("456 New Address")))
                // Unchanged fields should retain original values
                .andExpect(jsonPath("$.customer.lastName", is("Smith")));
    }

    @Test
    void updateAccount_notFound_returns404() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest(
                new AccountFields("Y", null, null, null, null, null, null, null, null, null),
                null
        );

        mockMvc.perform(put("/api/v1/accounts/99999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAccount_onlyAccountFields() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest(
                new AccountFields(null, null, new BigDecimal("25000.00"), null, null, null, null, null, null, null),
                null
        );

        mockMvc.perform(put("/api/v1/accounts/80001000002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.creditLimit", is(25000.00)))
                // Customer should remain unchanged
                .andExpect(jsonPath("$.customer.firstName", is("Jane")));
    }

    @Test
    void updateAccount_onlyCustomerFields() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest(
                null,
                new CustomerFields(null, null, "NewLastName", null, null, null, null, null, null, null, null, null, null, null, null, null, 800)
        );

        mockMvc.perform(put("/api/v1/accounts/80001000003")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.lastName", is("NewLastName")))
                .andExpect(jsonPath("$.customer.ficoCreditScore", is(800)))
                // Account should remain unchanged
                .andExpect(jsonPath("$.account.currentBalance", is(800.25)));
    }

    // ===== Account List Tests =====

    @Test
    void listAccounts_returnsFirstPage() throws Exception {
        mockMvc.perform(get("/api/v1/accounts?page=0&size=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts", hasSize(3)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(3)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void listAccounts_returnsSecondPage() throws Exception {
        mockMvc.perform(get("/api/v1/accounts?page=1&size=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts", hasSize(2)))
                .andExpect(jsonPath("$.page", is(1)));
    }

    @Test
    void listAccounts_defaultPagination() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts", hasSize(5)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)));
    }
}
