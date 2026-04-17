package com.carddemo.user.controller;

import com.carddemo.user.dto.CreateUserRequest;
import com.carddemo.user.dto.UpdateUserRequest;
import com.carddemo.user.dto.UserResponse;
import com.carddemo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
 * REST controller for User Administration CRUD operations.
 * <p>
 * Migrated from: COUSR00C.cbl (User List — CU00), COUSR01C.cbl (User Add — CU01),
 *                COUSR02C.cbl (User Update — CU02), COUSR03C.cbl (User Delete — CU03)
 * <p>
 * These programs are accessed only by admin users via the admin menu (COADM01C / CA00).
 * All endpoints require ADMIN role, matching the COBOL authorization check
 * where SEC-USR-TYPE = 'A' grants access to the admin menu.
 * <p>
 * Navigation context:
 *   - XCTL from COADM01C (admin menu) to COUSR00C/COUSR01C
 *   - XCTL from COUSR00C to COUSR02C (update) or COUSR03C (delete)
 *   - PF3 returns to COADM01C or calling program
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Administration", description = "Admin-only CRUD operations for user management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Paginated user list.
     * <p>
     * Migrated from: COUSR00C.cbl (User List — CU00)
     * CICS operations: STARTBR/READNEXT/READPREV on USRSEC
     * Original uses PF7 (page up) and PF8 (page down) for navigation.
     */
    @GetMapping
    @Operation(summary = "List users with pagination",
            description = "Replaces COUSR00C browse of USRSEC VSAM file")
    @ApiResponse(responseCode = "200", description = "Page of users returned")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "userId") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(userService.listUsers(page, size, sortBy, direction));
    }

    /**
     * Get a single user by ID.
     * <p>
     * Migrated from: COUSR02C.cbl / COUSR03C.cbl — initial READ by key
     * CICS operation: EXEC CICS READ FILE('USRSEC') RIDFLD(SEC-USR-ID)
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID",
            description = "Replaces CICS READ on USRSEC by primary key")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found (CICS RESP NOTFND)")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /**
     * Create a new user.
     * <p>
     * Migrated from: COUSR01C.cbl (User Add — CU01)
     * CICS operation: EXEC CICS WRITE FILE('USRSEC')
     * Navigation: Accessible from COADM01C admin menu
     */
    @PostMapping
    @Operation(summary = "Create new user",
            description = "Replaces COUSR01C WRITE to USRSEC VSAM file")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "409", description = "Duplicate user ID (CICS RESP DUPREC)")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing user.
     * <p>
     * Migrated from: COUSR02C.cbl (User Update — CU02)
     * CICS operations: READ + REWRITE on USRSEC
     * Navigation: XCTL from COUSR00C (selection flag 'U') or COADM01C
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update existing user",
            description = "Replaces COUSR02C REWRITE on USRSEC VSAM file")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "404", description = "User not found (CICS RESP NOTFND)")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    /**
     * Delete a user.
     * <p>
     * Migrated from: COUSR03C.cbl (User Delete — CU03)
     * CICS operations: READ (verify) + DELETE on USRSEC
     * Navigation: XCTL from COUSR00C (selection flag 'D') or COADM01C
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user",
            description = "Replaces COUSR03C DELETE on USRSEC VSAM file")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "User not found (CICS RESP NOTFND)")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
