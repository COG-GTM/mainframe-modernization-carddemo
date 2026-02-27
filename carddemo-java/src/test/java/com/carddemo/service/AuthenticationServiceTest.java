package com.carddemo.service;

import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.entity.UserSecurity;
import com.carddemo.exception.BusinessException;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock private UserSecurityRepository userSecurityRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @InjectMocks private AuthenticationService authenticationService;
    private UserSecurity adminUser;
    private UserSecurity regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new UserSecurity();
        adminUser.setUsrId("ADMIN001");
        adminUser.setUsrFname("ADMIN");
        adminUser.setUsrLname("USER");
        adminUser.setUsrPwd("PASSWORD");
        adminUser.setUsrType("A");

        regularUser = new UserSecurity();
        regularUser.setUsrId("USER0001");
        regularUser.setUsrFname("REGULAR");
        regularUser.setUsrLname("USER");
        regularUser.setUsrPwd("PASSWORD");
        regularUser.setUsrType("U");
    }

    @Test
    void login_admin_success() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));
        when(jwtTokenProvider.createToken("ADMIN001", "ADMIN")).thenReturn("jwt-token");
        LoginResponse response = authenticationService.login(new LoginRequest("ADMIN001", "PASSWORD"));
        assertEquals("jwt-token", response.getToken());
        assertEquals("ADMIN", response.getUserType());
        assertEquals("ADMIN", response.getFirstName());
    }

    @Test
    void login_user_success() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(regularUser));
        when(jwtTokenProvider.createToken("USER0001", "USER")).thenReturn("jwt-token-user");
        LoginResponse response = authenticationService.login(new LoginRequest("USER0001", "PASSWORD"));
        assertEquals("USER", response.getUserType());
    }

    @Test
    void login_invalidUser() {
        when(userSecurityRepository.findById("INVALID")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> authenticationService.login(new LoginRequest("INVALID", "PASSWORD")));
    }

    @Test
    void login_wrongPassword() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));
        assertThrows(BusinessException.class, () -> authenticationService.login(new LoginRequest("ADMIN001", "WRONG")));
    }
}
