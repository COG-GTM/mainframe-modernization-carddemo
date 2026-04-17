package com.carddemo.useradmin.controller;

import com.carddemo.useradmin.dto.CreateUserRequest;
import com.carddemo.useradmin.dto.UpdateUserRequest;
import com.carddemo.useradmin.dto.UserResponse;
import com.carddemo.useradmin.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(com.carddemo.useradmin.SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /users - List Users")
    class ListUsersTests {

        @Test
        @DisplayName("should return paginated users")
        void listUsers_success() throws Exception {
            UserResponse user1 = new UserResponse("USER0001", "John", "Doe", "U");
            UserResponse user2 = new UserResponse("ADMIN001", "Admin", "One", "A");
            Page<UserResponse> page = new PageImpl<>(
                    List.of(user1, user2), PageRequest.of(0, 10), 2);

            when(userService.listUsers(any())).thenReturn(page);

            mockMvc.perform(get("/users")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].userId", is("USER0001")))
                    .andExpect(jsonPath("$.content[0].firstName", is("John")))
                    .andExpect(jsonPath("$.content[0].lastName", is("Doe")))
                    .andExpect(jsonPath("$.content[0].userType", is("U")))
                    .andExpect(jsonPath("$.content[1].userId", is("ADMIN001")))
                    .andExpect(jsonPath("$.totalElements", is(2)));
        }

        @Test
        @DisplayName("should never return passwords in list response")
        void listUsers_noPasswords() throws Exception {
            UserResponse user = new UserResponse("USER0001", "John", "Doe", "U");
            Page<UserResponse> page = new PageImpl<>(
                    List.of(user), PageRequest.of(0, 10), 1);

            when(userService.listUsers(any())).thenReturn(page);

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0]", not(hasKey("password"))))
                    .andExpect(jsonPath("$.content[0]", not(hasKey("usrPwd"))));
        }
    }

    @Nested
    @DisplayName("POST /users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("should create user and return 201")
        void createUser_success() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "NEWUSR01", "New", "User", "password", "U");
            UserResponse response = new UserResponse(
                    "NEWUSR01", "New", "User", "U");

            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId", is("NEWUSR01")))
                    .andExpect(jsonPath("$.firstName", is("New")))
                    .andExpect(jsonPath("$.lastName", is("User")))
                    .andExpect(jsonPath("$.userType", is("U")))
                    .andExpect(jsonPath("$", not(hasKey("password"))));
        }

        @Test
        @DisplayName("should return 409 for duplicate user ID")
        void createUser_duplicate() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "USER0001", "Dup", "User", "pass", "U");

            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new UserService.UserAlreadyExistsException(
                            "User ID already exists: USER0001"));

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error", is("User ID already exists: USER0001")));
        }
    }

    @Nested
    @DisplayName("PUT /users/{id} - Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("should update user and return 200")
        void updateUser_success() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(
                    "Updated", "Name", null, "A");
            UserResponse response = new UserResponse(
                    "USER0001", "Updated", "Name", "A");

            when(userService.updateUser(eq("USER0001"), any(UpdateUserRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/users/USER0001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is("USER0001")))
                    .andExpect(jsonPath("$.firstName", is("Updated")))
                    .andExpect(jsonPath("$.userType", is("A")))
                    .andExpect(jsonPath("$", not(hasKey("password"))));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void updateUser_notFound() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(
                    "Updated", "Name", null, "A");

            when(userService.updateUser(eq("NOEXIST"), any(UpdateUserRequest.class)))
                    .thenThrow(new UserService.UserNotFoundException(
                            "User ID not found: NOEXIST"));

            mockMvc.perform(put("/users/NOEXIST")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("User ID not found: NOEXIST")));
        }

        @Test
        @DisplayName("should update user with password change")
        void updateUser_withPasswordChange() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(
                    "Updated", "Name", "newpass", "U");
            UserResponse response = new UserResponse(
                    "USER0001", "Updated", "Name", "U");

            when(userService.updateUser(eq("USER0001"), any(UpdateUserRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/users/USER0001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is("USER0001")))
                    .andExpect(jsonPath("$", not(hasKey("password"))));
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user and return 204")
        void deleteUser_success() throws Exception {
            doNothing().when(userService).deleteUser("USER0001");

            mockMvc.perform(delete("/users/USER0001"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void deleteUser_notFound() throws Exception {
            doThrow(new UserService.UserNotFoundException(
                    "User ID not found: NOEXIST"))
                    .when(userService).deleteUser("NOEXIST");

            mockMvc.perform(delete("/users/NOEXIST"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("User ID not found: NOEXIST")));
        }
    }

    @Nested
    @DisplayName("GET /users/health - Health Check")
    class HealthCheckTests {

        @Test
        @DisplayName("should return UP status")
        void healthCheck() throws Exception {
            mockMvc.perform(get("/users/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("UP")));
        }
    }
}
