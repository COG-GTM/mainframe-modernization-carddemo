package com.carddemo.auth.service;

import com.carddemo.auth.dto.CreateUserRequest;
import com.carddemo.auth.dto.UpdateUserRequest;
import com.carddemo.auth.dto.UserDto;
import com.carddemo.auth.entity.UserEntity;
import com.carddemo.auth.exception.UserAlreadyExistsException;
import com.carddemo.auth.exception.UserNotFoundException;
import com.carddemo.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void listUsers_shouldReturnPagedResults() {
        UserEntity user1 = new UserEntity("ADMIN001", "ADMIN", "USER", "hashed", "A");
        UserEntity user2 = new UserEntity("USER0001", "REGULAR", "USER", "hashed", "U");
        Page<UserEntity> page = new PageImpl<>(List.of(user1, user2));
        when(userRepository.findAllByOrderByUserIdAsc(any(Pageable.class))).thenReturn(page);

        Page<UserDto> result = userService.listUsers(0, 10);

        assertEquals(2, result.getTotalElements());
        assertEquals("ADMIN001", result.getContent().get(0).userId());
    }

    @Test
    void listUsers_shouldUseDefaultPageSize_whenSizeIsZero() {
        Page<UserEntity> page = new PageImpl<>(List.of());
        when(userRepository.findAllByOrderByUserIdAsc(any(Pageable.class))).thenReturn(page);

        userService.listUsers(0, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAllByOrderByUserIdAsc(captor.capture());
        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    void getUser_shouldReturnUser_whenFound() {
        UserEntity user = new UserEntity("ADMIN001", "ADMIN", "USER", "hashed", "A");
        when(userRepository.findByUserIdIgnoreCase("ADMIN001")).thenReturn(Optional.of(user));

        UserDto dto = userService.getUser("admin001");

        assertEquals("ADMIN001", dto.userId());
        assertEquals("ADMIN", dto.firstName());
        assertEquals("A", dto.userType());
    }

    @Test
    void getUser_shouldThrow_whenNotFound() {
        when(userRepository.findByUserIdIgnoreCase("UNKNOWN1")).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUser("unknown1"));
        assertEquals("User not found. Try again ...", ex.getMessage());
    }

    @Test
    void createUser_shouldSaveNormalizedUser() {
        when(userRepository.findByUserIdIgnoreCase("NEWUSER1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("PASSWORD")).thenReturn("hashedPwd");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateUserRequest request = new CreateUserRequest("newuser1", "John", "Doe", "password", "u");
        UserDto result = userService.createUser(request);

        assertEquals("NEWUSER1", result.userId());
        assertEquals("John", result.firstName());
        assertEquals("U", result.userType());
    }

    @Test
    void createUser_shouldThrow_whenUserAlreadyExists() {
        UserEntity existing = new UserEntity("ADMIN001", "ADMIN", "USER", "hashed", "A");
        when(userRepository.findByUserIdIgnoreCase("ADMIN001")).thenReturn(Optional.of(existing));

        CreateUserRequest request = new CreateUserRequest("admin001", "New", "Admin", "password", "A");
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(request));
    }

    @Test
    void updateUser_shouldUpdateOnlyProvidedFields() {
        UserEntity existing = new UserEntity("ADMIN001", "ADMIN", "USER", "oldHash", "A");
        when(userRepository.findByUserIdIgnoreCase("ADMIN001")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("NewFirst", null, null, null);
        UserDto result = userService.updateUser("admin001", request);

        assertEquals("NewFirst", result.firstName());
        assertEquals("USER", result.lastName());
    }

    @Test
    void updateUser_shouldThrow_whenNotFound() {
        when(userRepository.findByUserIdIgnoreCase("UNKNOWN1")).thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest("First", null, null, null);
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser("unknown1", request));
    }

    @Test
    void deleteUser_shouldDeleteUser_whenFound() {
        UserEntity existing = new UserEntity("USER0001", "REGULAR", "USER", "hashed", "U");
        when(userRepository.findByUserIdIgnoreCase("USER0001")).thenReturn(Optional.of(existing));

        userService.deleteUser("user0001");

        verify(userRepository).delete(existing);
    }

    @Test
    void deleteUser_shouldThrow_whenNotFound() {
        when(userRepository.findByUserIdIgnoreCase("UNKNOWN1")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser("unknown1"));
    }

    @Test
    void updateUser_shouldUpdatePassword_whenProvided() {
        UserEntity existing = new UserEntity("ADMIN001", "ADMIN", "USER", "oldHash", "A");
        when(userRepository.findByUserIdIgnoreCase("ADMIN001")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("NEWPASS1")).thenReturn("newHash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest(null, null, "newpass1", null);
        userService.updateUser("admin001", request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("newHash", captor.getValue().getPassword());
    }

    @Test
    void updateUser_shouldUpdateUserType_whenProvided() {
        UserEntity existing = new UserEntity("ADMIN001", "ADMIN", "USER", "hash", "A");
        when(userRepository.findByUserIdIgnoreCase("ADMIN001")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest(null, null, null, "u");
        UserDto result = userService.updateUser("admin001", request);

        assertEquals("U", result.userType());
    }
}
