package com.carddemo.auth;

import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.UserInfoResponse;
import com.carddemo.auth.entity.UserSecurityEntity;
import com.carddemo.auth.exception.AuthenticationException;
import com.carddemo.auth.repository.UserSecurityRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import com.carddemo.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService.
 *
 * Test cases mirror the COBOL COSGN00C.cbl READ-USER-SEC-FILE logic:
 *   - RESP=0, password match -> success (admin or user)
 *   - RESP=0, password mismatch -> "Wrong Password. Try again ..."
 *   - RESP=13 (NOTFND) -> "User not found. Try again ..."
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userSecurityRepository, jwtTokenProvider);
    }

    @Test
    @DisplayName("Successful admin login - SEC-USR-TYPE = 'A', XCTL to COADM01C")
    void authenticate_adminUser_returnsTokenWithAdminType() {
        UserSecurityEntity admin = new UserSecurityEntity(
                "ADMIN001", "Admin", "User", "ADMIN001", "A");
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(admin));
        when(jwtTokenProvider.createToken("ADMIN001", "A")).thenReturn("mock-jwt-token");

        LoginResponse response = authService.authenticate("admin001", "admin001");

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertEquals("ADMIN001", response.userId());
        assertEquals("Admin", response.firstName());
        assertEquals("User", response.lastName());
        assertEquals("A", response.userType());
    }

    @Test
    @DisplayName("Successful regular user login - SEC-USR-TYPE = 'U', XCTL to COMEN01C")
    void authenticate_regularUser_returnsTokenWithUserType() {
        UserSecurityEntity user = new UserSecurityEntity(
                "USER0001", "Regular", "User", "USER0001", "U");
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createToken("USER0001", "U")).thenReturn("mock-jwt-token");

        LoginResponse response = authService.authenticate("USER0001", "USER0001");

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertEquals("USER0001", response.userId());
        assertEquals("U", response.userType());
    }

    @Test
    @DisplayName("Wrong password - mirrors 'Wrong Password. Try again ...' error")
    void authenticate_wrongPassword_throwsAuthenticationException() {
        UserSecurityEntity user = new UserSecurityEntity(
                "USER0001", "Regular", "User", "USER0001", "U");
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(user));

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.authenticate("USER0001", "WRONGPWD"));

        assertEquals("Wrong Password. Try again ...", ex.getMessage());
    }

    @Test
    @DisplayName("User not found - mirrors RESP=13 NOTFND error")
    void authenticate_userNotFound_throwsAuthenticationException() {
        when(userSecurityRepository.findById("UNKNOWN1")).thenReturn(Optional.empty());

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.authenticate("unknown1", "password"));

        assertEquals("User not found. Try again ...", ex.getMessage());
    }

    @Test
    @DisplayName("getUserInfo returns user details for valid userId")
    void getUserInfo_existingUser_returnsUserInfo() {
        UserSecurityEntity user = new UserSecurityEntity(
                "ADMIN001", "Admin", "User", "ADMIN001", "A");
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(user));

        UserInfoResponse response = authService.getUserInfo("ADMIN001");

        assertEquals("ADMIN001", response.userId());
        assertEquals("Admin", response.firstName());
        assertEquals("User", response.lastName());
        assertEquals("A", response.userType());
    }

    @Test
    @DisplayName("getUserInfo throws exception for non-existent userId")
    void getUserInfo_nonExistentUser_throwsAuthenticationException() {
        when(userSecurityRepository.findById("UNKNOWN1")).thenReturn(Optional.empty());

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.getUserInfo("UNKNOWN1"));

        assertEquals("User not found. Try again ...", ex.getMessage());
    }
}
