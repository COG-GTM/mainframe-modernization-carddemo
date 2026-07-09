package com.carddemo.service.menu;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * MockMvc coverage for menu option selection driven through the CS-3 navigation framework
 * ({@code POST /api/nav} dispatching to the registered {@link MainMenuScreenHandler} /
 * {@link AdminMenuScreenHandler}). Exercises the {@code COMEN01C}/{@code COADM01C}
 * {@code PROCESS-ENTER-KEY} turn: display on first entry, then transfer / invalid-option /
 * PF3 on submit.
 */
@SpringBootTest
@ActiveProfiles("test")
class MenuNavigationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void selectingMainMenuOptionTransfersControlToTarget() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"));

        // First entry displays the menu (ENTER -> RE-ENTER); the option list is the model.
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"))
            .andExpect(jsonPath("$.context").value("REENTER"))
            .andExpect(jsonPath("$.model.length()").value(10))
            .andExpect(jsonPath("$.model[0].tranId").value("CAVW"));

        // Submitting option 1 (Account View) transfers control to COACTVWC.
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fields\":{\"option\":\"1\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fromProgram").value("COMEN01C"))
            .andExpect(jsonPath("$.toProgram").value("COACTVWC"))
            .andExpect(jsonPath("$.currentProgram").value("ACCOUNT_VIEW"))
            .andExpect(jsonPath("$.context").value("ENTER"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void invalidMainMenuOptionShowsErrorAndStaysOnMenu() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON).content("{}"));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fields\":{\"option\":\"99\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Please enter a valid option number..."))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void nonNumericMainMenuOptionShowsError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON).content("{}"));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fields\":{\"option\":\"AB\"}}"))
            .andExpect(jsonPath("$.message").value("Please enter a valid option number..."))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void selectingAdminMenuOptionTransfersControlToTarget() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session))
            .andExpect(jsonPath("$.toProgram").value("COADM01C"));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(jsonPath("$.toProgram").value("COADM01C"))
            .andExpect(jsonPath("$.context").value("REENTER"))
            .andExpect(jsonPath("$.model.length()").value(4));

        // Submitting option 1 (User List) transfers control to COUSR00C.
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fields\":{\"option\":\"1\"}}"))
            .andExpect(jsonPath("$.fromProgram").value("COADM01C"))
            .andExpect(jsonPath("$.toProgram").value("COUSR00C"))
            .andExpect(jsonPath("$.currentProgram").value("USER_LIST"))
            .andExpect(jsonPath("$.context").value("ENTER"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void pf3FromMainMenuReturnsToSignon() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pfKey\":\"PF3\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.toProgram").value("COSGN00C"))
            .andExpect(jsonPath("$.fromProgram").value("COMEN01C"));
    }
}
