package com.carddemo.online.auth;

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

/** COBOL program COSGN00C: signon against the USRSEC records seeded by the foundation. */
@SpringBootTest
@AutoConfigureMockMvc
class SignOnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static String body(String userId, String password) {
        return "{\"userId\":%s,\"password\":%s}"
                .formatted(quote(userId), quote(password));
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    @Test
    void adminSignsOnAndIsSentToTheAdminMenu() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/signon/enter")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("admin001", "admin001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andExpect(jsonPath("$.userId").value("ADMIN001"))
                .andExpect(jsonPath("$.nextProgram").value("COADM01C"))
                .andExpect(jsonPath("$.nextTransactionId").value("CA00"))
                .andExpect(jsonPath("$.header.transactionName").value("CC00"));

        CardDemoCommarea commarea =
                (CardDemoCommarea) session.getAttribute(CardDemoCommarea.SESSION_KEY);
        assertThat(commarea).isNotNull();
        assertThat(commarea.getUserId()).isEqualTo("ADMIN001");
        assertThat(commarea.getUserType()).isEqualTo(CardDemoCommarea.USER_TYPE_ADMIN);
        assertThat(commarea.isAdmin()).isTrue();
        assertThat(commarea.getFromProgram()).isEqualTo("COSGN00C");
        assertThat(commarea.getFromTransactionId()).isEqualTo("CC00");
        assertThat(commarea.getProgramContext()).isZero();
    }

    @Test
    void regularUserSignsOnAndIsSentToTheMainMenu() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/signon/enter")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("USER0001", "USER0001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextProgram").value("COMEN01C"))
                .andExpect(jsonPath("$.nextTransactionId").value("CM00"));

        CardDemoCommarea commarea =
                (CardDemoCommarea) session.getAttribute(CardDemoCommarea.SESSION_KEY);
        assertThat(commarea.getUserType()).isEqualTo(CardDemoCommarea.USER_TYPE_USER);
        assertThat(commarea.isAdmin()).isFalse();
    }

    @Test
    void rejectsAWrongPassword() throws Exception {
        mockMvc.perform(post("/api/signon/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ADMIN001", "NOPE")))
                .andExpect(jsonPath("$.errorMessage").value("Wrong Password. Try again ..."))
                .andExpect(jsonPath("$.cursorField").value("PASSWD"))
                .andExpect(jsonPath("$.nextProgram").doesNotExist());
    }

    @Test
    void rejectsAnUnknownUser() throws Exception {
        mockMvc.perform(post("/api/signon/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("NOSUCH01", "WHATEVER")))
                .andExpect(jsonPath("$.errorMessage").value("User not found. Try again ..."))
                .andExpect(jsonPath("$.cursorField").value("USERID"));
    }

    @Test
    void requiresAUserId() throws Exception {
        mockMvc.perform(post("/api/signon/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("  ", "ADMIN001")))
                .andExpect(jsonPath("$.errorMessage").value("Please enter User ID ..."))
                .andExpect(jsonPath("$.cursorField").value("USERID"));
    }

    @Test
    void requiresAPassword() throws Exception {
        mockMvc.perform(post("/api/signon/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ADMIN001", null)))
                .andExpect(jsonPath("$.errorMessage").value("Please enter Password ..."))
                .andExpect(jsonPath("$.cursorField").value("PASSWD"));
    }

    @Test
    void pf3EndsTheSessionWithTheThankYouMessage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/signon/enter")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("ADMIN001", "ADMIN001")));

        mockMvc.perform(post("/api/signon/pf3").session(session))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Thank you for using CardDemo application..."));

        assertThat(session.getAttribute(CardDemoCommarea.SESSION_KEY)).isNull();
    }

    @Test
    void anyOtherKeyIsRejected() throws Exception {
        mockMvc.perform(post("/api/signon/other-key"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Invalid key pressed. Please see below..."));
    }

    @Test
    void theInitialScreenStartsAFreshCommarea() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/api/signon").session(session))
                .andExpect(jsonPath("$.cursorField").value("USERID"))
                .andExpect(jsonPath("$.header.programName").value("COSGN00C"));

        CardDemoCommarea commarea =
                (CardDemoCommarea) session.getAttribute(CardDemoCommarea.SESSION_KEY);
        assertThat(commarea.getUserId()).isNull();
    }
}
