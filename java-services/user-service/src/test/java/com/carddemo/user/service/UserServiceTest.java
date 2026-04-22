package com.carddemo.user.service;

import com.carddemo.user.dto.UserCreateRequest;
import com.carddemo.user.dto.UserDto;
import com.carddemo.user.dto.UserListResponse;
import com.carddemo.user.dto.UserUpdateRequest;
import com.carddemo.user.exception.DuplicateResourceException;
import com.carddemo.user.exception.ResourceNotFoundException;
import com.carddemo.user.model.User;
import com.carddemo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("USER0001", "John", "Smith", "PASSWORD", "U");
    }

    @Test
    void listUsers_returnsPagedResults() {
        Page<User> page = new PageImpl<>(
                List.of(sampleUser),
                PageRequest.of(0, 10, Sort.by("userId").ascending()),
                1
        );
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        UserListResponse response = userService.listUsers(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getUsers().size());
        assertEquals(0, response.getCurrentPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());

        UserDto dto = response.getUsers().get(0);
        assertEquals("USER0001", dto.getUserId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("U", dto.getUserType());
    }

    @Test
    void getUser_existingUser_returnsDto() {
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));

        UserDto dto = userService.getUser("USER0001");

        assertEquals("USER0001", dto.getUserId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("U", dto.getUserType());
    }

    @Test
    void getUser_nonExistingUser_throwsNotFound() {
        when(userRepository.findById("NOUSER")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUser("NOUSER"));
    }

    @Test
    void createUser_newUser_returnsCreatedDto() {
        UserCreateRequest request = new UserCreateRequest(
                "user0004", "Alice", "Wonder", "PASS1234", "u");

        when(userRepository.existsById("USER0004")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto dto = userService.createUser(request);

        assertEquals("USER0004", dto.getUserId());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Wonder", dto.getLastName());
        assertEquals("U", dto.getUserType());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateId_throwsConflict() {
        UserCreateRequest request = new UserCreateRequest(
                "USER0001", "Alice", "Wonder", "PASS1234", "U");

        when(userRepository.existsById("USER0001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_convertsUserIdToUpperCase() {
        UserCreateRequest request = new UserCreateRequest(
                "admin002", "Bob", "Admin", "PASS1234", "a");

        when(userRepository.existsById("ADMIN002")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto dto = userService.createUser(request);

        assertEquals("ADMIN002", dto.getUserId());
        assertEquals("A", dto.getUserType());
    }

    @Test
    void updateUser_existingUser_updatesFields() {
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest("Jane", "Doe", "NEWPASS", "A");

        UserDto dto = userService.updateUser("USER0001", request);

        assertEquals("USER0001", dto.getUserId());
        assertEquals("Jane", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("A", dto.getUserType());
    }

    @Test
    void updateUser_partialUpdate_onlyUpdatesProvidedFields() {
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("UpdatedFirst");

        UserDto dto = userService.updateUser("USER0001", request);

        assertEquals("UpdatedFirst", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("U", dto.getUserType());
    }

    @Test
    void updateUser_nonExistingUser_throwsNotFound() {
        when(userRepository.findById("NOUSER")).thenReturn(Optional.empty());

        UserUpdateRequest request = new UserUpdateRequest("Jane", null, null, null);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser("NOUSER", request));
    }

    @Test
    void deleteUser_existingUser_deletes() {
        when(userRepository.existsById("USER0001")).thenReturn(true);

        userService.deleteUser("USER0001");

        verify(userRepository).deleteById("USER0001");
    }

    @Test
    void deleteUser_nonExistingUser_throwsNotFound() {
        when(userRepository.existsById("NOUSER")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser("NOUSER"));
        verify(userRepository, never()).deleteById(any());
    }
}
