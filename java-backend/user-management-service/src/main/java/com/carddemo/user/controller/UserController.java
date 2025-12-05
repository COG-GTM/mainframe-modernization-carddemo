package com.carddemo.user.controller;

import com.carddemo.user.dto.CreateUserRequest;
import com.carddemo.user.dto.UpdateUserRequest;
import com.carddemo.user.dto.UserDto;
import com.carddemo.user.dto.UserListResponse;
import com.carddemo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Management Controller
 * 
 * REST API endpoints replacing the COUSR COBOL programs:
 * - GET /api/users         -> COUSR00C (List users, Transaction CU00)
 * - GET /api/users/{id}    -> Part of COUSR02C/COUSR03C (Read user)
 * - POST /api/users        -> COUSR01C (Add user, Transaction CU01)
 * - PUT /api/users/{id}    -> COUSR02C (Update user, Transaction CU02)
 * - DELETE /api/users/{id} -> COUSR03C (Delete user, Transaction CU03)
 * 
 * Access Control:
 * - In mainframe, these functions are only accessible from Admin menu (COADM01C)
 * - In modernized system, endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User CRUD operations (replaces COUSR00C-COUSR03C)")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * List all users with pagination
     * 
     * Replaces COUSR00C (Transaction CU00) - List all users from USRSEC file
     * 
     * Original mainframe behavior:
     * - Displays 10 users per page (USER-REC OCCURS 10 TIMES)
     * - PF7 for previous page, PF8 for next page
     * - Selection options: U (Update), D (Delete)
     * 
     * @param page page number (0-based, default 0)
     * @param size page size (default 10 to match mainframe)
     * @return paginated list of users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List Users",
            description = "List all users with pagination. Replaces COUSR00C transaction CU00."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserListResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<UserListResponse> listUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10)")
            @RequestParam(defaultValue = "10") int size) {
        UserListResponse response = userService.listUsers(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID
     * 
     * Retrieves a single user's details.
     * Used before update (COUSR02C) or delete (COUSR03C) operations.
     * 
     * @param userId user ID to retrieve
     * @return user details
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get User",
            description = "Get user details by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "User ID (max 8 characters)")
            @PathVariable String userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Create new user
     * 
     * Replaces COUSR01C (Transaction CU01) - Add a new Regular/Admin user
     * 
     * Original mainframe behavior:
     * - Validates all fields are not empty
     * - Writes to USRSEC VSAM file
     * - Checks for duplicate user ID (DFHRESP(DUPKEY))
     * - Success message: 'User {id} has been added ...'
     * 
     * @param request user creation request
     * @return created user
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create User",
            description = "Create a new user. Replaces COUSR01C transaction CU01."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "User ID already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Update existing user
     * 
     * Replaces COUSR02C (Transaction CU02) - Update a user in USRSEC file
     * 
     * Original mainframe behavior:
     * - Reads user with UPDATE option
     * - Compares each field to detect changes
     * - Rewrites record if modified
     * - Success message: 'User {id} has been updated ...'
     * - Error if no changes: 'Please modify to update ...'
     * 
     * @param userId user ID to update
     * @param request update request
     * @return updated user
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update User",
            description = "Update an existing user. Replaces COUSR02C transaction CU02."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or no modifications"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID to update")
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user
     * 
     * Replaces COUSR03C (Transaction CU03) - Delete a user from USRSEC file
     * 
     * Original mainframe behavior:
     * - Reads user with UPDATE option
     * - Deletes record from VSAM file
     * - Success message: 'User {id} has been deleted ...'
     * 
     * @param userId user ID to delete
     * @return no content on success
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete User",
            description = "Delete a user. Replaces COUSR03C transaction CU03."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID to delete")
            @PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
