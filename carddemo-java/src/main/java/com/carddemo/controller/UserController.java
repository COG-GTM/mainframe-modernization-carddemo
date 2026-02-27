package com.carddemo.controller;

import com.carddemo.dto.UserSecurityRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserSecurity>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @PostMapping
    public ResponseEntity<UserSecurity> createUser(@Valid @RequestBody UserSecurityRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserSecurity> updateUser(@PathVariable String id, @Valid @RequestBody UserSecurityRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
