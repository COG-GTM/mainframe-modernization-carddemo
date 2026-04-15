package com.carddemo.user.controller;

import com.carddemo.user.dto.UserCreateRequest;
import com.carddemo.user.dto.UserDto;
import com.carddemo.user.dto.UserListResponse;
import com.carddemo.user.dto.UserUpdateRequest;
import com.carddemo.user.exception.DuplicateResourceException;
import com.carddemo.user.exception.GlobalExceptionHandler;
import com.carddemo.user.exception.ResourceNotFoundException;
import com.carddemo.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listUsers_returnsOkWithPagedResults() throws Exception {
        UserDto user = new UserDto("USER0001", "John", "Smith", "U");
        UserListResponse response = new UserListResponse(List.of(user), 0, 1, 1);

        when(userService.listUsers(0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].userId").value("USER0001"))
                .andExpect(jsonPath("$.users[0].firstName").value("John"))
                .andExpect(jsonPath("$.users[0].lastName").value("Smith"))
                .andExpect(jsonPath("$.users[0].userType").value("U"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getUser_existingUser_returnsOk() throws Exception {
        UserDto user = new UserDto("USER0001", "John", "Smith", "U");
        when(userService.getUser("USER0001")).thenReturn(user);

        mockMvc.perform(get("/api/users/USER0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("USER0001"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getUser_nonExistingUser_returns404() throws Exception {
        when(userService.getUser("NOUSER"))
                .thenThrow(new ResourceNotFoundException("User ID NOT found: NOUSER"));

        mockMvc.perform(get("/api/users/NOUSER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User ID NOT found: NOUSER"));
    }

    @Test
    void createUser_validRequest_returns201() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "USER0004", "Alice", "Wonder", "PASS1234", "U");
        UserDto created = new UserDto("USER0004", "Alice", "Wonder", "U");

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("USER0004"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void createUser_duplicateId_returns409() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "USER0001", "Alice", "Wonder", "PASS1234", "U");

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("User ID already exists: USER0001"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User ID already exists: USER0001"));
    }

    @Test
    void createUser_emptyUserId_returns400() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "", "Alice", "Wonder", "PASS1234", "U");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_invalidUserType_returns400() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "USER0004", "Alice", "Wonder", "PASS1234", "X");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_validRequest_returnsOk() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Jane", "Doe", "NEWPASS", "A");
        UserDto updated = new UserDto("USER0001", "Jane", "Doe", "A");

        when(userService.updateUser(eq("USER0001"), any(UserUpdateRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/users/USER0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.userType").value("A"));
    }

    @Test
    void updateUser_nonExistingUser_returns404() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Jane", null, null, null);

        when(userService.updateUser(eq("NOUSER"), any(UserUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("User ID NOT found: NOUSER"));

        mockMvc.perform(put("/api/users/NOUSER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_existingUser_returns204() throws Exception {
        doNothing().when(userService).deleteUser("USER0001");

        mockMvc.perform(delete("/api/users/USER0001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_nonExistingUser_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("User ID NOT found: NOUSER"))
                .when(userService).deleteUser("NOUSER");

        mockMvc.perform(delete("/api/users/NOUSER"))
                .andExpect(status().isNotFound());
    }
}
