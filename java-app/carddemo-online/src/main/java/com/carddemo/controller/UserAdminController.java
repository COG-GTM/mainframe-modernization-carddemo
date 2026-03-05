package com.carddemo.controller;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.dto.UserUpdateRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User administration controller — replaces COUSR00C, COUSR01C, COUSR02C, COUSR03C CICS transactions.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ResponseEntity<Page<UserSecurity>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userAdminService.listUsers(pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSecurity> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userAdminService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserSecurity> addUser(@Valid @RequestBody UserCreateRequest dto) {
        return ResponseEntity.ok(userAdminService.addUser(dto));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserSecurity> updateUser(@PathVariable String userId,
                                                    @Valid @RequestBody UserUpdateRequest dto) {
        return ResponseEntity.ok(userAdminService.updateUser(userId, dto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userAdminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
