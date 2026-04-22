package com.carddemo.auth.controller;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.exception.AuthenticationException;
import com.carddemo.auth.exception.GlobalExceptionHandler;
import com.carddemo.auth.security.SecurityConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_withValidCredentials_returns200() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .token("test-jwt-token")
                .userId("USER0001")
                .firstName("John")
                .lastName("Doe")
                .userType("U")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest("USER0001", "PASSWORD");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.userId").value("USER0001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.userType").value("U"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Wrong Password. Try again ..."));

        LoginRequest request = new LoginRequest("USER0001", "WRONGPWD");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }

    @Test
    void login_withUserNotFound_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("User not found. Try again ..."));

        LoginRequest request = new LoginRequest("UNKNOWN1", "PASSWORD");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found. Try again ..."));
    }

    @Test
    void validate_withValidToken_returns200() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .token("valid-token")
                .userId("USER0001")
                .firstName("John")
                .lastName("Doe")
                .userType("U")
                .build();

        when(authService.validateToken("valid-token")).thenReturn(response);

        mockMvc.perform(post("/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"valid-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("USER0001"))
                .andExpect(jsonPath("$.userType").value("U"));
    }
}
