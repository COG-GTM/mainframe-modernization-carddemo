package com.carddemo.session.web;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@SpringBootTest
@ActiveProfiles("test")
class NavigationControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void signonRoutesAdminToAdminMenu() throws Exception {
        mockMvc.perform(post("/api/nav/signon"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.toProgram").value("COADM01C"))
            .andExpect(jsonPath("$.currentProgram").value("ADMIN_MENU"))
            .andExpect(jsonPath("$.fromProgram").value("COSGN00C"))
            .andExpect(jsonPath("$.userType").value("ADMIN"))
            .andExpect(jsonPath("$.userId").value("ADMIN001"))
            .andExpect(jsonPath("$.enter").value(true));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void signonRoutesUserToMainMenu() throws Exception {
        mockMvc.perform(post("/api/nav/signon"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"))
            .andExpect(jsonPath("$.currentProgram").value("MAIN_MENU"))
            .andExpect(jsonPath("$.userType").value("USER"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void commareaPersistsAcrossCallsAndTracksReenter() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Sign on -> lands on main menu, first entry (ENTER).
        mockMvc.perform(post("/api/nav/signon").session(session))
            .andExpect(jsonPath("$.userId").value("USER0001"))
            .andExpect(jsonPath("$.enter").value(true));

        // A later call re-presents the same commarea (user id preserved = COMMAREA persistence).
        mockMvc.perform(get("/api/nav").session(session))
            .andExpect(jsonPath("$.userId").value("USER0001"))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"));

        // Launch a function: transfer sets from/to and the target is a first entry (ENTER).
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"CAVW\"}"))
            .andExpect(jsonPath("$.fromProgram").value("COMEN01C"))
            .andExpect(jsonPath("$.toProgram").value("COACTVWC"))
            .andExpect(jsonPath("$.context").value("ENTER"))
            .andExpect(jsonPath("$.enter").value(true));

        // A subsequent turn on the same screen (no handler) becomes a RE-ENTER; state persists.
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pfKey\":\"ENTER\"}"))
            .andExpect(jsonPath("$.toProgram").value("COACTVWC"))
            .andExpect(jsonPath("$.context").value("REENTER"))
            .andExpect(jsonPath("$.enter").value(false))
            .andExpect(jsonPath("$.userId").value("USER0001"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void pf3ReturnsFromFunctionToMenu() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"CAVW\"}"))
            .andExpect(jsonPath("$.toProgram").value("COACTVWC"));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pfKey\":\"PF3\"}"))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"))
            .andExpect(jsonPath("$.fromProgram").value("COACTVWC"))
            .andExpect(jsonPath("$.context").value("ENTER"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void adminOnlyFunctionDeniedForRegularUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"CU00\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("No access - Admin Only option..."))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"));
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/nav/signon"))
            .andExpect(status().isUnauthorized());
    }
}
