package com.carddemo.web.menu;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * MockMvc coverage for the menu list endpoints (CS-8, {@code COMEN01C}/{@code COADM01C}).
 */
@SpringBootTest
@ActiveProfiles("test")
class MenuControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void mainMenuListsRegularUserOptions() throws Exception {
        mockMvc.perform(get("/api/menu"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.menu").value("MAIN_MENU"))
            .andExpect(jsonPath("$.programName").value("COMEN01C"))
            .andExpect(jsonPath("$.tranId").value("CM00"))
            .andExpect(jsonPath("$.userType").value("USER"))
            .andExpect(jsonPath("$.userId").value("USER0001"))
            .andExpect(jsonPath("$.options.length()").value(10))
            .andExpect(jsonPath("$.options[0].number").value("01"))
            .andExpect(jsonPath("$.options[0].name").value("Account View"))
            .andExpect(jsonPath("$.options[0].tranId").value("CAVW"))
            .andExpect(jsonPath("$.options[0].programName").value("COACTVWC"))
            .andExpect(jsonPath("$.options[9].name").value("Bill Payment"))
            .andExpect(jsonPath("$.options[9].tranId").value("CB00"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void adminMenuListsAdminOptionsForAdmin() throws Exception {
        mockMvc.perform(get("/api/menu/admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.menu").value("ADMIN_MENU"))
            .andExpect(jsonPath("$.programName").value("COADM01C"))
            .andExpect(jsonPath("$.tranId").value("CA00"))
            .andExpect(jsonPath("$.userType").value("ADMIN"))
            .andExpect(jsonPath("$.options.length()").value(4))
            .andExpect(jsonPath("$.options[0].name").value("User List (Security)"))
            .andExpect(jsonPath("$.options[0].tranId").value("CU00"))
            .andExpect(jsonPath("$.options[0].adminOnly").value(true))
            .andExpect(jsonPath("$.options[3].name").value("User Delete (Security)"))
            .andExpect(jsonPath("$.options[3].tranId").value("CU03"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void adminMenuDeniedForRegularUser() throws Exception {
        mockMvc.perform(get("/api/menu/admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    void mainMenuRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/menu"))
            .andExpect(status().isUnauthorized());
    }
}
