package com.carddemo.user.controller;

import com.carddemo.common.dto.ApiResponse;
import com.carddemo.common.dto.PagedResponse;
import com.carddemo.user.dto.*;
import com.carddemo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management operations.
 * 
 * Replaces COBOL programs:
 * - GET  /api/users       -> COUSR00C (CU00) - List users
 * - GET  /api/users/{id}  -> COUSR00C (CU00) - Get single user
 * - POST /api/users       -> COUSR01C (CU01) - Add user
 * - PUT  /api/users/{id}  -> COUSR02C (CU02) - Update user
 * - DELETE /api/users/{id} -> COUSR03C (CU03) - Delete user
 * 
 * Original COBOL transaction flow:
 *   CU00 -> COUSR00C -> displays user list with selection
 *   User selects 'U' -> COUSR02C (update)
 *   User selects 'D' -> COUSR03C (delete)
 *   Admin menu -> COUSR01C (add new user)
 * 
 * New REST API provides direct CRUD operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User Management API - replaces COUSR00C/01C/02C/03C")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "List all users",
            description = "Returns paginated list of users. " +
                    "Replaces COUSR00C (CU00) which displays 10 users per page."
    )
    public ResponseEntity<ApiResponse<PagedResponse<UserListItem>>> listUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, matching COBOL)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Search term for user ID, first name, or last name")
            @RequestParam(required = false) String search) {

        log.info("List users request - page: {}, size: {}, search: {}", page, size, search);
        PagedResponse<UserListItem> response = userService.listUsers(page, size, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Returns user details by user ID. " +
                    "Replaces READ-USER-SEC-FILE in COUSR02C/03C."
    )
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "User ID (8 characters max)")
            @PathVariable String userId) {

        log.info("Get user request - userId: {}", userId);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @Operation(
            summary = "Create new user",
            description = "Creates a new user. " +
                    "Replaces COUSR01C (CU01) WRITE-USER-SEC-FILE operation."
    )
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("Create user request - userId: {}", request.getUserId());
        UserDto user = userService.createUser(request);
        String message = String.format("User %s has been added", user.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, message));
    }

    @PutMapping("/{userId}")
    @Operation(
            summary = "Update user",
            description = "Updates an existing user. " +
                    "Replaces COUSR02C (CU02) UPDATE-USER-SEC-FILE operation."
    )
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Parameter(description = "User ID to update")
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Update user request - userId: {}", userId);
        UserDto user = userService.updateUser(userId, request);
        String message = String.format("User %s has been updated", user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(user, message));
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete user",
            description = "Deletes a user (soft delete). " +
                    "Replaces COUSR03C (CU03) DELETE-USER-SEC-FILE operation."
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID to delete")
            @PathVariable String userId) {

        log.info("Delete user request - userId: {}", userId);
        userService.deleteUser(userId);
        String message = String.format("User %s has been deleted", userId.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }
}
