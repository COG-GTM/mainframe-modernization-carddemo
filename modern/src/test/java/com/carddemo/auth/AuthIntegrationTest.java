package com.carddemo.auth;

import com.carddemo.auth.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the auth service using @SpringBootTest with H2 in-memory DB.
 *
 * Tests the full stack: controller -> service -> repository -> H2 database
 * with Flyway migrations applied (V1 creates table, V2 seeds test users).
 *
 * Test cases mirror COSGN00C.cbl authentication flow:
 *   - Admin login (SEC-USR-TYPE = 'A') -> JWT with admin claims
 *   - User login (SEC-USR-TYPE = 'U') -> JWT with user claims
 *   - Wrong password -> 401 "Wrong Password. Try again ..."
 *   - User not found -> 401 "User not found. Try again ..."
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/auth/login - admin user returns JWT with userType=A")
    void login_adminUser_returnsJwtWithAdminType() throws Exception {
        LoginRequest request = new LoginRequest("ADMIN001", "ADMIN001");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userId", is("ADMIN001")))
                .andExpect(jsonPath("$.firstName", is("Admin")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.userType", is("A")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - regular user returns JWT with userType=U")
    void login_regularUser_returnsJwtWithUserType() throws Exception {
        LoginRequest request = new LoginRequest("USER0001", "USER0001");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userId", is("USER0001")))
                .andExpect(jsonPath("$.firstName", is("Regular")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.userType", is("U")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest request = new LoginRequest("ADMIN001", "WRONGPWD");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Wrong Password. Try again ...")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - user not found returns 401")
    void login_userNotFound_returns401() throws Exception {
        LoginRequest request = new LoginRequest("UNKNOWN1", "PASSWORD");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("User not found. Try again ...")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - missing userId returns 400")
    void login_missingUserId_returns400() throws Exception {
        String body = "{\"password\":\"ADMIN001\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - case insensitive userId and password")
    void login_caseInsensitive_succeeds() throws Exception {
        LoginRequest request = new LoginRequest("admin001", "admin001");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("ADMIN001")))
                .andExpect(jsonPath("$.userType", is("A")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - authenticated user returns user info")
    void me_authenticatedUser_returnsUserInfo() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest("ADMIN001", "ADMIN001");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()).get("token").asText();

        // Then use the token to access /me
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("ADMIN001")))
                .andExpect(jsonPath("$.firstName", is("Admin")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.userType", is("A")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - unauthenticated returns 401/403")
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - returns success message")
    void logout_authenticated_returnsSuccessMessage() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest("USER0001", "USER0001");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()).get("token").asText();

        // Then logout
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Thank you for using CardDemo!")));
    }
}
