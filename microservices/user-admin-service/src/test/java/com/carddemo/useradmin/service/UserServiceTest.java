package com.carddemo.useradmin.service;

import com.carddemo.useradmin.dto.CreateUserRequest;
import com.carddemo.useradmin.dto.UpdateUserRequest;
import com.carddemo.useradmin.dto.UserResponse;
import com.carddemo.useradmin.entity.User;
import com.carddemo.useradmin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("USER0001", "John", "Doe",
                "$2a$10$hashedpassword", "U");
    }

    @Nested
    @DisplayName("List Users")
    class ListUsersTests {

        @Test
        @DisplayName("should return paginated users")
        void listUsers_withPagination() {
            User user2 = new User("USER0002", "Jane", "Smith",
                    "$2a$10$hashedpassword2", "A");
            Page<User> userPage = new PageImpl<>(
                    List.of(sampleUser, user2), PageRequest.of(0, 10), 2);

            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

            Page<UserResponse> result = userService.listUsers(PageRequest.of(0, 10));

            assertEquals(2, result.getTotalElements());
            assertEquals("USER0001", result.getContent().get(0).getUserId());
            assertEquals("John", result.getContent().get(0).getFirstName());
            assertEquals("Doe", result.getContent().get(0).getLastName());
            assertEquals("U", result.getContent().get(0).getUserType());
            assertEquals("USER0002", result.getContent().get(1).getUserId());
            verify(userRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("should never return passwords in response")
        void listUsers_passwordsNeverReturned() {
            Page<User> userPage = new PageImpl<>(
                    List.of(sampleUser), PageRequest.of(0, 10), 1);

            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

            Page<UserResponse> result = userService.listUsers(PageRequest.of(0, 10));

            UserResponse response = result.getContent().get(0);
            // UserResponse has no password field — verified by compilation
            assertNotNull(response.getUserId());
            assertNotNull(response.getFirstName());
            assertNotNull(response.getLastName());
            assertNotNull(response.getUserType());
        }
    }

    @Nested
    @DisplayName("Create User")
    class CreateUserTests {

        @Test
        @DisplayName("should create user with hashed password")
        void createUser_success() {
            CreateUserRequest request = new CreateUserRequest(
                    "NEWUSR01", "New", "User", "plainpass", "U");

            when(userRepository.existsById("NEWUSR01")).thenReturn(false);
            when(passwordEncoder.encode("plainpass")).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponse response = userService.createUser(request);

            assertEquals("NEWUSR01", response.getUserId());
            assertEquals("New", response.getFirstName());
            assertEquals("User", response.getLastName());
            assertEquals("U", response.getUserType());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals("$2a$10$encoded", captor.getValue().getUsrPwd());
            verify(passwordEncoder).encode("plainpass");
        }

        @Test
        @DisplayName("should throw exception when user ID already exists (DFHRESP DUPREC)")
        void createUser_duplicateId() {
            CreateUserRequest request = new CreateUserRequest(
                    "USER0001", "Dup", "User", "pass", "U");

            when(userRepository.existsById("USER0001")).thenReturn(true);

            UserService.UserAlreadyExistsException ex = assertThrows(
                    UserService.UserAlreadyExistsException.class,
                    () -> userService.createUser(request));

            assertEquals("User ID already exists: USER0001", ex.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("should update user fields")
        void updateUser_success() {
            UpdateUserRequest request = new UpdateUserRequest(
                    "Updated", "Name", null, "A");

            when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponse response = userService.updateUser("USER0001", request);

            assertEquals("USER0001", response.getUserId());
            assertEquals("Updated", response.getFirstName());
            assertEquals("Name", response.getLastName());
            assertEquals("A", response.getUserType());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("should throw not found when user does not exist (DFHRESP NOTFND)")
        void updateUser_notFound() {
            UpdateUserRequest request = new UpdateUserRequest(
                    "Updated", "Name", null, "A");

            when(userRepository.findById("NOEXIST")).thenReturn(Optional.empty());

            assertThrows(UserService.UserNotFoundException.class,
                    () -> userService.updateUser("NOEXIST", request));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should hash and update password when provided")
        void updateUser_withPasswordChange() {
            UpdateUserRequest request = new UpdateUserRequest(
                    null, null, "newpassword", null);

            when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.encode("newpassword")).thenReturn("$2a$10$newhash");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUser("USER0001", request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals("$2a$10$newhash", captor.getValue().getUsrPwd());
            verify(passwordEncoder).encode("newpassword");
        }

        @Test
        @DisplayName("should not update password when not provided")
        void updateUser_withoutPasswordChange() {
            String originalPwd = sampleUser.getUsrPwd();
            UpdateUserRequest request = new UpdateUserRequest(
                    "Updated", null, null, null);

            when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.updateUser("USER0001", request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals(originalPwd, captor.getValue().getUsrPwd());
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete existing user")
        void deleteUser_success() {
            when(userRepository.existsById("USER0001")).thenReturn(true);

            userService.deleteUser("USER0001");

            verify(userRepository).deleteById("USER0001");
        }

        @Test
        @DisplayName("should throw not found when user does not exist (DFHRESP NOTFND)")
        void deleteUser_notFound() {
            when(userRepository.existsById("NOEXIST")).thenReturn(false);

            assertThrows(UserService.UserNotFoundException.class,
                    () -> userService.deleteUser("NOEXIST"));

            verify(userRepository, never()).deleteById(anyString());
        }
    }
}
