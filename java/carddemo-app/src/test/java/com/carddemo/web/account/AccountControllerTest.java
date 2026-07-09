package com.carddemo.web.account;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.carddemo.web.account.dto.AccountUpdateRequest;

/**
 * MockMvc tests for {@link AccountController} ({@code COACTVWC}/{@code COACTUPC}), driven with
 * an authenticated principal against the real seed data (account {@code 00000000050} →
 * customer {@code 000000005}). {@code @Transactional} rolls back the update mutations.
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@Transactional
class AccountControllerTest {

    private static final String ACCT_ID = "00000000050";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void getReturnsAccountAndCustomer() throws Exception {
        mockMvc.perform(get("/api/accounts/{acctId}", ACCT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctId").value(ACCT_ID))
            .andExpect(jsonPath("$.custId").value("000000050"))
            .andExpect(jsonPath("$.lastName").value("Von"))
            .andExpect(jsonPath("$.ssn").value("931-24-8469"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void getUnknownAccountReturns404WithCobolMessage() throws Exception {
        mockMvc.perform(get("/api/accounts/{acctId}", "00000099999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("Did not find this account in account card xref file"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void putWithValidChangesCommits() throws Exception {
        mockMvc.perform(put("/api/accounts/{acctId}", ACCT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest("600"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Changes committed to database"))
            .andExpect(jsonPath("$.account.ficoScore").value(600));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void putWithInvalidFieldReturns400WithCobolMessage() throws Exception {
        mockMvc.perform(put("/api/accounts/{acctId}", ACCT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest("999"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("FICO Score: should be between 300 and 850"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("FICO Score"));
    }

    /** Fully valid update payload (MI/48226 is a valid state-zip combo, area code 978 valid). */
    private static AccountUpdateRequest validRequest(String fico) {
        return new AccountUpdateRequest(
            "Y", "2011", "04", "22", "5000.00",
            "2025", "03", "09", "1000.00",
            "2024", "03", "09", "100.00",
            "0.00", "0.00", "A0001",
            "611", "26", "4288", "1971", "09", "29", fico,
            "Treva", "Manley", "Schowalter", "5653 Legros Plaza", "Apt 968", "Alvinaport",
            "MI", "48226", "USA",
            "978", "775", "4633", "", "", "",
            "0006365573", "0000000001", "Y");
    }
}
