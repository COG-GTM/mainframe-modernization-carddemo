package com.carddemo.auth.controller;

import com.carddemo.auth.dto.CreateUserRequest;
import com.carddemo.auth.dto.UpdateUserRequest;
import com.carddemo.auth.dto.UserDto;
import com.carddemo.auth.exception.GlobalExceptionHandler;
import com.carddemo.auth.exception.UserAlreadyExistsException;
import com.carddemo.auth.exception.UserNotFoundException;
import com.carddemo.auth.security.JwtAuthenticationFilter;
import com.carddemo.auth.security.JwtTokenProvider;
import com.carddemo.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listUsers_shouldReturn200_withPagedResults() throws Exception {
        UserDto user1 = new UserDto("ADMIN001", "ADMIN", "USER", "A");
        UserDto user2 = new UserDto("USER0001", "REGULAR", "USER", "U");
        Page<UserDto> page = new PageImpl<>(List.of(user1, user2));
        when(userService.listUsers(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value("ADMIN001"))
                .andExpect(jsonPath("$.content[1].userId").value("USER0001"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getUser_shouldReturn200_whenUserExists() throws Exception {
        UserDto user = new UserDto("ADMIN001", "ADMIN", "USER", "A");
        when(userService.getUser("ADMIN001")).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/ADMIN001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("ADMIN001"))
                .andExpect(jsonPath("$.userType").value("A"));
    }

    @Test
    void getUser_shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.getUser("UNKNOWN1"))
                .thenThrow(new UserNotFoundException("User not found. Try again ..."));

        mockMvc.perform(get("/api/v1/users/UNKNOWN1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found. Try again ..."));
    }

    @Test
    void createUser_shouldReturn201_withValidRequest() throws Exception {
        UserDto created = new UserDto("NEWUSER1", "John", "Doe", "U");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(created);

        CreateUserRequest request = new CreateUserRequest("newuser1", "John", "Doe", "pass123", "U");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("NEWUSER1"));
    }

    @Test
    void createUser_shouldReturn409_whenUserAlreadyExists() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User ID 'ADMIN001' already exists"));

        CreateUserRequest request = new CreateUserRequest("admin001", "New", "Admin", "pass123", "A");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_shouldReturn200_withValidRequest() throws Exception {
        UserDto updated = new UserDto("ADMIN001", "NewFirst", "USER", "A");
        when(userService.updateUser(eq("ADMIN001"), any(UpdateUserRequest.class))).thenReturn(updated);

        UpdateUserRequest request = new UpdateUserRequest("NewFirst", null, null, null);

        mockMvc.perform(put("/api/v1/users/ADMIN001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("NewFirst"));
    }

    @Test
    void deleteUser_shouldReturn204_whenUserExists() throws Exception {
        doNothing().when(userService).deleteUser("USER0001");

        mockMvc.perform(delete("/api/v1/users/USER0001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturn404_whenUserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found. Try again ..."))
                .when(userService).deleteUser("UNKNOWN1");

        mockMvc.perform(delete("/api/v1/users/UNKNOWN1"))
                .andExpect(status().isNotFound());
    }
}
