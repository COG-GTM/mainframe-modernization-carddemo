package com.carddemo.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * End-to-end tests for the ported {@code COSGN00C} sign-on flow, driven against the real seed
 * users loaded from {@code usrsec.txt} (enabled via {@code carddemo.seed.enabled=true}).
 *
 * <p>Seed users: {@code ADMIN001..ADMIN005} (type 'A', password {@code PASSWORD}) and
 * {@code USER0001..USER0005} (type 'U', password {@code PASSWORD}).</p>
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static String body(String userId, String password) {
        return "{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void adminSignonReturnsAdminRoleAndDestination() throws Exception {
        mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("ADMIN001", "PASSWORD")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("ADMIN001"))
            .andExpect(jsonPath("$.firstName").value("MARGARET"))
            .andExpect(jsonPath("$.lastName").value("GOLD"))
            .andExpect(jsonPath("$.userType").value("A"))
            .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
            .andExpect(jsonPath("$.destination").value("ADMIN_MENU"))
            .andExpect(jsonPath("$.program").value("COADM01C"));
    }

    @Test
    void regularSignonReturnsUserRoleAndMainMenu() throws Exception {
        mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("USER0001", "PASSWORD")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("USER0001"))
            .andExpect(jsonPath("$.userType").value("U"))
            .andExpect(jsonPath("$.role").value("ROLE_USER"))
            .andExpect(jsonPath("$.destination").value("MAIN_MENU"))
            .andExpect(jsonPath("$.program").value("COMEN01C"));
    }

    @Test
    void signonUpperCasesInputsLikeCobol() throws Exception {
        // COBOL: MOVE FUNCTION UPPER-CASE(USERIDI/PASSWDI). Lower-case input must still match.
        mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("admin001", "password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("ADMIN001"))
            .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void wrongPasswordReturnsCobolMessage() throws Exception {
        mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("ADMIN001", "WRONGPWD")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }

    @Test
    void unknownUserReturnsCobolMessage() throws Exception {
        mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("NOSUCH01", "PASSWORD")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("User not found. Try again ..."));
    }

    @Test
    void emptyUserIdReturnsCobolMessage() throws Exception {
        mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("", "PASSWORD")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Please enter User ID ..."));
    }

    @Test
    void emptyPasswordReturnsCobolMessage() throws Exception {
        mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("ADMIN001", "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Please enter Password ..."));
    }

    @Test
    void protectedEndpointReturns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/signoff"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAuthorizedAfterSignon() throws Exception {
        // Sign on, capture the established session, then reuse it on a protected endpoint.
        MvcResult result = mockMvc.perform(post("/api/auth/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("USER0001", "PASSWORD")))
            .andExpect(status().isOk())
            .andReturn();

        HttpSession httpSession = result.getRequest().getSession(false);
        MockHttpSession session = (MockHttpSession) httpSession;

        mockMvc.perform(post("/api/auth/signoff").session(session))
            .andExpect(status().isNoContent());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
