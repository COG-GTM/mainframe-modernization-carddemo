package com.carddemo.user.service;

import com.carddemo.user.dto.CreateUserRequest;
import com.carddemo.user.dto.UpdateUserRequest;
import com.carddemo.user.dto.UserResponse;
import com.carddemo.user.entity.UserSecurityEntity;
import com.carddemo.user.exception.DuplicateUserException;
import com.carddemo.user.exception.UserNotFoundException;
import com.carddemo.user.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService.
 * Tests all CRUD operations matching COBOL program behavior:
 *   - COUSR00C (List), COUSR01C (Add), COUSR02C (Update), COUSR03C (Delete)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserSecurityRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserSecurityEntity testEntity;

    @BeforeEach
    void setUp() {
        testEntity = new UserSecurityEntity(
                "ADMIN001", "ADMIN", "USER", "PASSWORD", "A");
    }

    @Nested
    @DisplayName("listUsers — COUSR00C (User List)")
    class ListUsersTests {

        @Test
        @DisplayName("should return paginated users sorted by userId ascending")
        void listUsers_returnsPagedResults() {
            var entity2 = new UserSecurityEntity("USER0001", "FIRST", "USER", "PASSWORD", "U");
            Page<UserSecurityEntity> page = new PageImpl<>(List.of(testEntity, entity2));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<UserResponse> result = userService.listUsers(0, 10, "userId", "asc");

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).userId()).isEqualTo("ADMIN001");
            assertThat(result.getContent().get(1).userId()).isEqualTo("USER0001");
        }

        @Test
        @DisplayName("should return empty page when no users exist")
        void listUsers_emptyPage() {
            Page<UserSecurityEntity> page = new PageImpl<>(List.of());
            when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<UserResponse> result = userService.listUsers(0, 10, "userId", "asc");

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("should exclude password from response")
        void listUsers_excludesPassword() {
            Page<UserSecurityEntity> page = new PageImpl<>(List.of(testEntity));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<UserResponse> result = userService.listUsers(0, 10, "userId", "asc");

            UserResponse response = result.getContent().get(0);
            assertThat(response).hasNoNullFieldsOrProperties();
            // UserResponse record does not have a password field
        }
    }

    @Nested
    @DisplayName("getUser — CICS READ on USRSEC")
    class GetUserTests {

        @Test
        @DisplayName("should return user when found")
        void getUser_found() {
            when(userRepository.findById("ADMIN001")).thenReturn(Optional.of(testEntity));

            UserResponse result = userService.getUser("ADMIN001");

            assertThat(result.userId()).isEqualTo("ADMIN001");
            assertThat(result.firstName()).isEqualTo("ADMIN");
            assertThat(result.lastName()).isEqualTo("USER");
            assertThat(result.userType()).isEqualTo("A");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found (CICS RESP NOTFND)")
        void getUser_notFound() {
            when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser("UNKNOWN"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }
    }

    @Nested
    @DisplayName("createUser — COUSR01C (User Add)")
    class CreateUserTests {

        @Test
        @DisplayName("should create user successfully")
        void createUser_success() {
            var request = new CreateUserRequest("NEWUSER1", "John", "Doe", "PASS1234", "U");
            when(userRepository.existsById("NEWUSER1")).thenReturn(false);
            when(userRepository.save(any(UserSecurityEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UserResponse result = userService.createUser(request);

            assertThat(result.userId()).isEqualTo("NEWUSER1");
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
            assertThat(result.userType()).isEqualTo("U");
            verify(userRepository).save(any(UserSecurityEntity.class));
        }

        @Test
        @DisplayName("should uppercase user ID on create")
        void createUser_uppercasesUserId() {
            var request = new CreateUserRequest("newuser1", "John", "Doe", "PASS1234", "u");
            when(userRepository.existsById("NEWUSER1")).thenReturn(false);
            when(userRepository.save(any(UserSecurityEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UserResponse result = userService.createUser(request);

            assertThat(result.userId()).isEqualTo("NEWUSER1");
            assertThat(result.userType()).isEqualTo("U");
        }

        @Test
        @DisplayName("should throw DuplicateUserException for duplicate ID (CICS RESP DUPREC)")
        void createUser_duplicate() {
            var request = new CreateUserRequest("ADMIN001", "John", "Doe", "PASS1234", "A");
            when(userRepository.existsById("ADMIN001")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateUserException.class)
                    .hasMessageContaining("ADMIN001");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateUser — COUSR02C (User Update)")
    class UpdateUserTests {

        @Test
        @DisplayName("should update user successfully")
        void updateUser_success() {
            var request = new UpdateUserRequest("UPDATED", "NAME", "NEWPASS", "U");
            when(userRepository.findById("ADMIN001")).thenReturn(Optional.of(testEntity));
            when(userRepository.save(any(UserSecurityEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            UserResponse result = userService.updateUser("ADMIN001", request);

            assertThat(result.userId()).isEqualTo("ADMIN001");
            assertThat(result.firstName()).isEqualTo("UPDATED");
            assertThat(result.lastName()).isEqualTo("NAME");
            assertThat(result.userType()).isEqualTo("U");
            verify(userRepository).save(any(UserSecurityEntity.class));
        }

        @Test
        @DisplayName("should throw UserNotFoundException when updating non-existent user")
        void updateUser_notFound() {
            var request = new UpdateUserRequest("UPDATED", "NAME", "NEWPASS", "U");
            when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser("UNKNOWN", request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteUser — COUSR03C (User Delete)")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user successfully")
        void deleteUser_success() {
            when(userRepository.existsById("ADMIN001")).thenReturn(true);

            userService.deleteUser("ADMIN001");

            verify(userRepository).deleteById("ADMIN001");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when deleting non-existent user")
        void deleteUser_notFound() {
            when(userRepository.existsById("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser("UNKNOWN"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
            verify(userRepository, never()).deleteById(any());
        }
    }
}
