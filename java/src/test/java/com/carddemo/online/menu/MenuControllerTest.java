package com.carddemo.online.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.model.dto.CardDemoCommarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** COBOL programs COMEN01C and COADM01C driven through their REST endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession signOn(String userId) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/signon/enter")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"%s\",\"password\":\"%s\"}".formatted(userId, userId)))
                .andExpect(status().isOk());
        return session;
    }

    @Test
    void mainMenuRoutesToTheSelectedProgram() throws Exception {
        MockHttpSession session = signOn("USER0001");

        mockMvc.perform(post("/api/menu/enter")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"option\":\"6\"}"))
                .andExpect(jsonPath("$.nextProgram").value("COTRN00C"))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void mainMenuRejectsAnInvalidOption() throws Exception {
        MockHttpSession session = signOn("USER0001");

        mockMvc.perform(post("/api/menu/enter")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"option\":\"99\"}"))
                .andExpect(jsonPath("$.errorMessage").value("Please enter a valid option number..."))
                .andExpect(jsonPath("$.nextProgram").doesNotExist());
    }

    @Test
    void withoutACommareaTheMenuReturnsToTheSignonScreen() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(jsonPath("$.nextProgram").value("COSGN00C"));

        mockMvc.perform(post("/api/admin/menu/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"option\":\"1\"}"))
                .andExpect(jsonPath("$.nextProgram").value("COSGN00C"));
    }

    @Test
    void adminMenuRoutesToTheUserList() throws Exception {
        MockHttpSession session = signOn("ADMIN001");

        mockMvc.perform(get("/api/admin/menu").session(session))
                .andExpect(jsonPath("$.options[0]").value("01. User List (Security)"));

        mockMvc.perform(post("/api/admin/menu/enter")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"option\":\"1\"}"))
                .andExpect(jsonPath("$.nextProgram").value("COUSR00C"))
                .andExpect(jsonPath("$.nextTransactionId").value("CU00"));
    }

    @Test
    void pf3ReturnsToTheSignonProgram() throws Exception {
        MockHttpSession session = signOn("ADMIN001");

        mockMvc.perform(post("/api/admin/menu/pf3").session(session))
                .andExpect(jsonPath("$.nextProgram").value("COSGN00C"));
    }

    @Test
    void anyOtherKeyIsRejected() throws Exception {
        MockHttpSession session = signOn("USER0001");

        mockMvc.perform(post("/api/menu/other-key").session(session))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Invalid key pressed. Please see below..."));
    }

    @Test
    void theMenuScreenMarksTheCommareaAsReentered() throws Exception {
        MockHttpSession session = signOn("USER0001");

        mockMvc.perform(get("/api/menu").session(session))
                .andExpect(jsonPath("$.options.length()").value(10));

        CardDemoCommarea commarea =
                (CardDemoCommarea) session.getAttribute(CardDemoCommarea.SESSION_KEY);
        assertThat(commarea.getProgramContext()).isEqualTo(1);
    }
}
