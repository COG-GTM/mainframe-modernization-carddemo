package com.carddemo.useradmin.controller;

import com.carddemo.useradmin.dto.CreateUserRequest;
import com.carddemo.useradmin.dto.UpdateUserRequest;
import com.carddemo.useradmin.dto.UserResponse;
import com.carddemo.useradmin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for User Administration.
 * Modernized from COBOL CICS transactions:
 *   CU00 (COUSR00C) - List users   -> GET  /users
 *   CU01 (COUSR01C) - Add user     -> POST /users
 *   CU02 (COUSR02C) - Update user  -> PUT  /users/{id}
 *   CU03 (COUSR03C) - Delete user  -> DELETE /users/{id}
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    /**
     * List all users with pagination.
     * Replaces COUSR00C (CU00) STARTBR/READNEXT browse on USRSEC.
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> users = userService.listUsers(PageRequest.of(page, size));
        return ResponseEntity.ok(users);
    }

    /**
     * Create a new user.
     * Replaces COUSR01C (CU01) WRITE to USRSEC.
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing user.
     * Replaces COUSR02C (CU02) READ + REWRITE on USRSEC.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a user.
     * Replaces COUSR03C (CU03) READ + DELETE on USRSEC.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(UserService.UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            UserService.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserService.UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleConflict(
            UserService.UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
