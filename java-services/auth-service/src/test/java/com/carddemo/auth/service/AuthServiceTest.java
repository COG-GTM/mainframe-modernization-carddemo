package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.exception.AuthenticationException;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        regularUser = User.builder()
                .userId("USER0001")
                .firstName("John")
                .lastName("Doe")
                .password("PASSWORD")
                .userType("U")
                .build();

        adminUser = User.builder()
                .userId("ADMIN001")
                .firstName("Admin")
                .lastName("User")
                .password("PASSWORD")
                .userType("A")
                .build();
    }

    @Test
    void login_withValidRegularUser_returnsLoginResponse() {
        when(userRepository.findByUserId("USER0001")).thenReturn(Optional.of(regularUser));
        when(jwtUtil.generateToken("USER0001", "U", "John", "Doe")).thenReturn("mock-jwt-token");

        LoginRequest request = new LoginRequest("USER0001", "PASSWORD");
        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getUserId()).isEqualTo("USER0001");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getUserType()).isEqualTo("U");
    }

    @Test
    void login_withValidAdminUser_returnsLoginResponse() {
        when(userRepository.findByUserId("ADMIN001")).thenReturn(Optional.of(adminUser));
        when(jwtUtil.generateToken("ADMIN001", "A", "Admin", "User")).thenReturn("mock-admin-token");

        LoginRequest request = new LoginRequest("ADMIN001", "PASSWORD");
        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mock-admin-token");
        assertThat(response.getUserType()).isEqualTo("A");
    }

    @Test
    void login_convertsUserIdToUpperCase() {
        when(userRepository.findByUserId("USER0001")).thenReturn(Optional.of(regularUser));
        when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("mock-jwt-token");

        LoginRequest request = new LoginRequest("user0001", "PASSWORD");
        LoginResponse response = authService.login(request);

        assertThat(response.getUserId()).isEqualTo("USER0001");
    }

    @Test
    void login_withUserNotFound_throwsAuthenticationException() {
        when(userRepository.findByUserId("UNKNOWN1")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("UNKNOWN1", "PASSWORD");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User not found. Try again ...");
    }

    @Test
    void login_withWrongPassword_throwsAuthenticationException() {
        when(userRepository.findByUserId("USER0001")).thenReturn(Optional.of(regularUser));

        LoginRequest request = new LoginRequest("USER0001", "WRONGPWD");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Wrong Password. Try again ...");
    }
}
