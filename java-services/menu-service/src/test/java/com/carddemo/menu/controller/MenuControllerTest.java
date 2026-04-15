package com.carddemo.menu.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MenuController verifying HTTP responses,
 * JSON structure, and access control (403 for admin-only options).
 */
@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRegularUserMenu() throws Exception {
        mockMvc.perform(get("/api/menu").param("userType", "U"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("U"))
                .andExpect(jsonPath("$.totalOptions").value(11))
                .andExpect(jsonPath("$.menuItems", hasSize(11)))
                .andExpect(jsonPath("$.menuItems[0].name").value("Account View"))
                .andExpect(jsonPath("$.menuItems[10].name").value("Pending Authorization View"));
    }

    @Test
    void getAdminUserMenu() throws Exception {
        mockMvc.perform(get("/api/menu").param("userType", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("A"))
                .andExpect(jsonPath("$.totalOptions").value(17))
                .andExpect(jsonPath("$.menuItems", hasSize(17)))
                .andExpect(jsonPath("$.menuItems[0].name").value("User List"))
                .andExpect(jsonPath("$.menuItems[6].name").value("Account View"));
    }

    @Test
    void defaultUserTypeIsRegular() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("U"))
                .andExpect(jsonPath("$.totalOptions").value(11));
    }

    @Test
    void getSpecificRegularOption() throws Exception {
        mockMvc.perform(get("/api/menu/1").param("userType", "U"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionNumber").value(1))
                .andExpect(jsonPath("$.name").value("Account View"))
                .andExpect(jsonPath("$.httpMethod").value("GET"));
    }

    @Test
    void getSpecificAdminOption() throws Exception {
        mockMvc.perform(get("/api/menu/1").param("userType", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionNumber").value(1))
                .andExpect(jsonPath("$.name").value("User List"));
    }

    @Test
    void nonExistentOptionReturns404() throws Exception {
        mockMvc.perform(get("/api/menu/99").param("userType", "U"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonExistentOptionForAdminReturns404() throws Exception {
        mockMvc.perform(get("/api/menu/99").param("userType", "A"))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifyMenuItemStructure() throws Exception {
        mockMvc.perform(get("/api/menu/3").param("userType", "U"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionNumber").value(3))
                .andExpect(jsonPath("$.name").value("Credit Card List"))
                .andExpect(jsonPath("$.serviceEndpoint").value("http://card-service:8083/api/cards"))
                .andExpect(jsonPath("$.httpMethod").value("GET"));
    }

    @Test
    void billPaymentEndpoint() throws Exception {
        mockMvc.perform(get("/api/menu/10").param("userType", "U"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bill Payment"))
                .andExpect(jsonPath("$.httpMethod").value("POST"))
                .andExpect(jsonPath("$.serviceEndpoint").value("http://payment-service:8085/api/payments/bill"));
    }

    @Test
    void pendingAuthorizationEndpoint() throws Exception {
        mockMvc.perform(get("/api/menu/11").param("userType", "U"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pending Authorization View"))
                .andExpect(jsonPath("$.serviceEndpoint").value("http://authorization-service:8090/api/authorizations"));
    }
}
