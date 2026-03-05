package com.carddemo.service;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.enums.UserType;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserAdminService — validates parity with COUSR00C-03C.
 */
@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void getUser_found() {
        UserSecurity user = new UserSecurity();
        user.setUserId("admin001");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setUserType(UserType.ADMIN);

        when(userSecurityRepository.findByUserId("admin001")).thenReturn(Optional.of(user));

        UserSecurity result = userAdminService.getUser("admin001");
        assertEquals("admin001", result.getUserId());
        assertEquals(UserType.ADMIN, result.getUserType());
    }

    @Test
    void getUser_notFound() {
        when(userSecurityRepository.findByUserId("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userAdminService.getUser("missing"));
    }

    @Test
    void addUser_duplicateId() {
        UserCreateRequest dto = new UserCreateRequest();
        dto.setUserId("existing");

        when(userSecurityRepository.findByUserId("existing")).thenReturn(Optional.of(new UserSecurity()));

        assertThrows(IllegalArgumentException.class, () -> userAdminService.addUser(dto));
    }

    @Test
    void deleteUser_success() {
        UserSecurity user = new UserSecurity();
        user.setUserId("user0001");

        when(userSecurityRepository.findByUserId("user0001")).thenReturn(Optional.of(user));

        userAdminService.deleteUser("user0001");
        verify(userSecurityRepository).delete(user);
    }
}
