package com.carddemo.service;

import com.carddemo.dto.UserSecurityRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserSecurityRepository userSecurityRepository;
    @InjectMocks private UserService userService;
    private UserSecurity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserSecurity();
        testUser.setUsrId("USER0001");
        testUser.setUsrFname("TEST");
        testUser.setUsrLname("USER");
        testUser.setUsrPwd("PASSWORD");
        testUser.setUsrType("U");
    }

    @Test
    void listUsers() {
        when(userSecurityRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(testUser)));
        Page<UserSecurity> result = userService.listUsers(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUser_success() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        UserSecurity result = userService.getUser("USER0001");
        assertEquals("TEST", result.getUsrFname());
    }

    @Test
    void getUser_notFound() {
        when(userSecurityRepository.findById("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUser("INVALID"));
    }

    @Test
    void createUser() {
        when(userSecurityRepository.save(any(UserSecurity.class))).thenReturn(testUser);
        UserSecurityRequest req = new UserSecurityRequest();
        req.setUsrId("NEW001");
        req.setUsrFname("NEW");
        req.setUsrLname("USER");
        req.setUsrPwd("PASS");
        req.setUsrType("U");
        UserSecurity result = userService.createUser(req);
        assertNotNull(result);
    }

    @Test
    void updateUser() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(userSecurityRepository.save(any(UserSecurity.class))).thenReturn(testUser);
        UserSecurityRequest req = new UserSecurityRequest();
        req.setUsrFname("UPDATED");
        UserSecurity result = userService.updateUser("USER0001", req);
        assertNotNull(result);
    }

    @Test
    void deleteUser() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        userService.deleteUser("USER0001");
        verify(userSecurityRepository).delete(testUser);
    }

    @Test
    void deleteUser_notFound() {
        when(userSecurityRepository.findById("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser("INVALID"));
    }
}
