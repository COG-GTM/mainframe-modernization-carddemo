package com.carddemo.auth.controller;

import com.carddemo.auth.config.SecurityConfig;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for AuthController endpoints.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_regularUser_shouldReturn200WithToken() throws Exception {
        LoginResponse response = new LoginResponse(
                "jwt-token", "USER0001", "U", "Regular", "User", "COMEN01C");

        when(authService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"USER0001\", \"password\": \"PASSWORD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value("USER0001"))
                .andExpect(jsonPath("$.userType").value("U"))
                .andExpect(jsonPath("$.firstName").value("Regular"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.redirectProgram").value("COMEN01C"));
    }

    @Test
    void login_adminUser_shouldReturn200WithAdminRedirect() throws Exception {
        LoginResponse response = new LoginResponse(
                "admin-jwt-token", "ADMIN001", "A", "Admin", "User", "COADM01C");

        when(authService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"ADMIN001\", \"password\": \"PASSWORD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-jwt-token"))
                .andExpect(jsonPath("$.userType").value("A"))
                .andExpect(jsonPath("$.redirectProgram").value("COADM01C"));
    }

    @Test
    void login_wrongPassword_shouldReturn401() throws Exception {
        when(authService.authenticate(any()))
                .thenThrow(new AuthService.AuthenticationException("Wrong Password. Try again ..."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"USER0001\", \"password\": \"WRONG\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Wrong Password. Try again ..."));
    }

    @Test
    void login_userNotFound_shouldReturn401() throws Exception {
        when(authService.authenticate(any()))
                .thenThrow(new AuthService.AuthenticationException("User not found. Try again ..."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"UNKNOWN\", \"password\": \"PASSWORD\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not found. Try again ..."));
    }

    @Test
    void healthCheck_shouldReturn200WithStatusUp() throws Exception {
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
