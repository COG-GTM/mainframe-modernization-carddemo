package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.entity.User;
import com.carddemo.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests for AuthService - mirrors the COBOL COSGN00C.cbl authentication logic.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void authenticate_regularUser_shouldReturnTokenWithUserType() {
        String hashedPassword = passwordEncoder.encode("PASSWORD");
        User user = new User("USER0001", "Regular", "User", hashedPassword, "U");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");

        LoginRequest request = new LoginRequest("USER0001", "PASSWORD");
        LoginResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("USER0001", response.getUserId());
        assertEquals("U", response.getUserType());
        assertEquals("Regular", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertEquals("COMEN01C", response.getRedirectProgram());
    }

    @Test
    void authenticate_adminUser_shouldReturnTokenWithAdminType() {
        String hashedPassword = passwordEncoder.encode("PASSWORD");
        User admin = new User("ADMIN001", "Admin", "User", hashedPassword, "A");

        when(userRepository.findById("ADMIN001")).thenReturn(Optional.of(admin));
        when(jwtService.generateToken(admin)).thenReturn("mock-admin-jwt-token");

        LoginRequest request = new LoginRequest("ADMIN001", "PASSWORD");
        LoginResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("mock-admin-jwt-token", response.getToken());
        assertEquals("ADMIN001", response.getUserId());
        assertEquals("A", response.getUserType());
        assertEquals("COADM01C", response.getRedirectProgram());
    }

    @Test
    void authenticate_wrongPassword_shouldThrowException() {
        String hashedPassword = passwordEncoder.encode("CORRECT_PWD");
        User user = new User("USER0001", "Regular", "User", hashedPassword, "U");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("USER0001", "WRONG_PWD");

        AuthService.AuthenticationException exception = assertThrows(
                AuthService.AuthenticationException.class,
                () -> authService.authenticate(request)
        );
        assertEquals("Wrong Password. Try again ...", exception.getMessage());
    }

    @Test
    void authenticate_userNotFound_shouldThrowException() {
        when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("UNKNOWN", "PASSWORD");

        AuthService.AuthenticationException exception = assertThrows(
                AuthService.AuthenticationException.class,
                () -> authService.authenticate(request)
        );
        assertEquals("User not found. Try again ...", exception.getMessage());
    }

    @Test
    void authenticate_shouldUppercaseUserId() {
        String hashedPassword = passwordEncoder.encode("PASSWORD");
        User user = new User("USER0001", "Regular", "User", hashedPassword, "U");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");

        LoginRequest request = new LoginRequest("user0001", "PASSWORD");
        LoginResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("USER0001", response.getUserId());
    }
}
