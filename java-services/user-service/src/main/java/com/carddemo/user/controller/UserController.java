package com.carddemo.user.controller;

import com.carddemo.user.dto.UserCreateRequest;
import com.carddemo.user.dto.UserDto;
import com.carddemo.user.dto.UserListResponse;
import com.carddemo.user.dto.UserUpdateRequest;
import com.carddemo.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * REST controller for user administration.
 * Exposes CRUD endpoints migrated from CICS transactions CU00-CU03:
 *   GET  /api/users          - List users (COUSR00C / CU00)
 *   GET  /api/users/{userId} - Get user   (COUSR02C read)
 *   POST /api/users          - Add user   (COUSR01C / CU01)
 *   PUT  /api/users/{userId} - Update     (COUSR02C / CU02)
 *   DELETE /api/users/{userId} - Delete   (COUSR03C / CU03)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserListResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.listUsers(page, size));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDto created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
