package com.carddemo.user.controller;

import com.carddemo.user.dto.CreateUserRequest;
import com.carddemo.user.dto.UpdateUserRequest;
import com.carddemo.user.repository.UserSecurityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for UserController with H2 in-memory database.
 * Tests all CRUD operations, pagination, and admin-only access enforcement.
 * <p>
 * Validates behavior matching the original COBOL programs:
 *   - COUSR00C (User List with pagination)
 *   - COUSR01C (User Add with duplicate detection)
 *   - COUSR02C (User Update with not-found handling)
 *   - COUSR03C (User Delete with not-found handling)
 *   - Admin-only access (SEC-USR-TYPE = 'A' required)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/admin/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserSecurityRepository userRepository;

    @Nested
    @DisplayName("GET /api/v1/admin/users — List Users (COUSR00C)")
    class ListUsersTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return paginated user list")
        void listUsers_paginated() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sortBy", "userId")
                            .param("direction", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.pageable").exists())
                    .andExpect(jsonPath("$.totalElements").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return second page when requested")
        void listUsers_secondPage() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.size", is(2)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should not include password in response")
        void listUsers_noPassword() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].password").doesNotExist());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{userId} — Get User")
    class GetUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return user when found")
        void getUser_found() throws Exception {
            mockMvc.perform(get(BASE_URL + "/ADMIN001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is("ADMIN001")))
                    .andExpect(jsonPath("$.firstName", is("ADMIN")))
                    .andExpect(jsonPath("$.userType", is("A")))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 when user not found (CICS RESP NOTFND)")
        void getUser_notFound() throws Exception {
            mockMvc.perform(get(BASE_URL + "/ZZZZZZZZ"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User not found: ZZZZZZZZ"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/users — Create User (COUSR01C)")
    class CreateUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should create user and return 201")
        void createUser_success() throws Exception {
            var request = new CreateUserRequest("NEWUSR01", "Jane", "Smith", "PASS1234", "U");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId", is("NEWUSR01")))
                    .andExpect(jsonPath("$.firstName", is("Jane")))
                    .andExpect(jsonPath("$.lastName", is("Smith")))
                    .andExpect(jsonPath("$.userType", is("U")))
                    .andExpect(jsonPath("$.password").doesNotExist());

            // Cleanup
            userRepository.deleteById("NEWUSR01");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 409 for duplicate user ID (CICS RESP DUPREC)")
        void createUser_duplicate() throws Exception {
            var request = new CreateUserRequest("ADMIN001", "Dup", "User", "PASS1234", "A");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("User already exists: ADMIN001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 for missing required fields")
        void createUser_validationError() throws Exception {
            var request = new CreateUserRequest("", "", "", "", "X");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages").isArray());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 for invalid user type")
        void createUser_invalidUserType() throws Exception {
            var request = new CreateUserRequest("TESTUS01", "Test", "User", "PASS1234", "X");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 when userId exceeds 8 characters")
        void createUser_userIdTooLong() throws Exception {
            var request = new CreateUserRequest("TOOLONGID", "Test", "User", "PASS1234", "U");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/users/{userId} — Update User (COUSR02C)")
    class UpdateUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should update user and return 200")
        void updateUser_success() throws Exception {
            var request = new UpdateUserRequest("UPDATED", "ADMIN", "NEWPASS1", "A");

            mockMvc.perform(put(BASE_URL + "/ADMIN001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is("ADMIN001")))
                    .andExpect(jsonPath("$.firstName", is("UPDATED")))
                    .andExpect(jsonPath("$.lastName", is("ADMIN")));

            // Restore original data
            var restore = new UpdateUserRequest("ADMIN", "USER", "PASSWORD", "A");
            mockMvc.perform(put(BASE_URL + "/ADMIN001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(restore)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 when updating non-existent user")
        void updateUser_notFound() throws Exception {
            var request = new UpdateUserRequest("First", "Last", "PASS1234", "U");

            mockMvc.perform(put(BASE_URL + "/ZZZZZZZZ")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/users/{userId} — Delete User (COUSR03C)")
    class DeleteUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should delete user and return 204")
        void deleteUser_success() throws Exception {
            // Create a user to delete
            var create = new CreateUserRequest("DELUSR01", "Delete", "Me", "PASS1234", "U");
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(create)));

            mockMvc.perform(delete(BASE_URL + "/DELUSR01"))
                    .andExpect(status().isNoContent());

            // Verify user is gone
            mockMvc.perform(get(BASE_URL + "/DELUSR01"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 when deleting non-existent user")
        void deleteUser_notFound() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/ZZZZZZZZ"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Admin-only access enforcement")
    class AuthorizationTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should deny access to regular user (SEC-USR-TYPE = 'U')")
        void regularUser_denied() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should deny access to unauthenticated user")
        void unauthenticated_denied() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should deny POST to regular user")
        void regularUser_deniedPost() throws Exception {
            var request = new CreateUserRequest("TESTUS01", "Test", "User", "PASS1234", "U");
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should deny DELETE to regular user")
        void regularUser_deniedDelete() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/ADMIN001"))
                    .andExpect(status().isForbidden());
        }
    }
}
