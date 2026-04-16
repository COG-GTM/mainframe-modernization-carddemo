package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.entity.UserEntity;
import com.carddemo.auth.exception.AuthenticationException;
import com.carddemo.auth.exception.UserNotFoundException;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        UserEntity user = new UserEntity("ADMIN001", "ADMIN", "USER", "hashedPwd", "A");
        when(userRepository.findByUserIdIgnoreCase("ADMIN001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("PASSWORD", "hashedPwd")).thenReturn(true);
        when(jwtTokenProvider.generateToken("ADMIN001", "A")).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("admin001", "password"));

        assertEquals("jwt-token", response.token());
        assertEquals("ADMIN001", response.userId());
        assertEquals("A", response.userType());
    }

    @Test
    void login_shouldUpperCaseUserIdAndPassword() {
        UserEntity user = new UserEntity("USER0001", "REGULAR", "USER", "hashedPwd", "U");
        when(userRepository.findByUserIdIgnoreCase("USER0001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("PASSWORD", "hashedPwd")).thenReturn(true);
        when(jwtTokenProvider.generateToken("USER0001", "U")).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("user0001", "password"));

        assertEquals("USER0001", response.userId());
        assertEquals("U", response.userType());
    }

    @Test
    void login_shouldThrow_whenUserIdIsBlank() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginRequest("  ", "password")));
        assertEquals("Please enter User ID ...", ex.getMessage());
    }

    @Test
    void login_shouldThrow_whenPasswordIsBlank() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginRequest("admin001", "  ")));
        assertEquals("Please enter Password ...", ex.getMessage());
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUserIdIgnoreCase("UNKNOWN1")).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> authService.login(new LoginRequest("unknown1", "password")));
        assertEquals("User not found. Try again ...", ex.getMessage());
    }

    @Test
    void login_shouldThrow_whenPasswordIsWrong() {
        UserEntity user = new UserEntity("ADMIN001", "ADMIN", "USER", "hashedPwd", "A");
        when(userRepository.findByUserIdIgnoreCase("ADMIN001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WRONGPWD", "hashedPwd")).thenReturn(false);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginRequest("admin001", "wrongpwd")));
        assertEquals("Wrong Password. Try again ...", ex.getMessage());
    }

    @Test
    void login_shouldThrow_whenRepositoryThrowsUnexpectedError() {
        when(userRepository.findByUserIdIgnoreCase(anyString()))
                .thenThrow(new RuntimeException("DB connection failed"));

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login(new LoginRequest("admin001", "password")));
        assertEquals("Unable to verify the User ...", ex.getMessage());
    }

    @Test
    void login_shouldReturnRegularUserType() {
        UserEntity user = new UserEntity("USER0001", "REGULAR", "USER", "hashedPwd", "U");
        when(userRepository.findByUserIdIgnoreCase("USER0001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("PASSWORD", "hashedPwd")).thenReturn(true);
        when(jwtTokenProvider.generateToken("USER0001", "U")).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("user0001", "password"));

        assertEquals("REGULAR", response.firstName());
        assertEquals("USER", response.lastName());
    }
}
