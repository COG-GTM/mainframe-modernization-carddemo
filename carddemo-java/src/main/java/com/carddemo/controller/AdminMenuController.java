package com.carddemo.controller;

import com.carddemo.dto.MenuOption;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminMenuController {

    @GetMapping("/menu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MenuOption>> getAdminMenu() {
        List<MenuOption> options = List.of(
            new MenuOption(1, "CU00", "User List"),
            new MenuOption(2, "CU01", "Add User"),
            new MenuOption(3, "CU02", "Update User"),
            new MenuOption(4, "CU03", "Delete User"),
            new MenuOption(5, "CTTU", "Transaction Type Update (Admin Only)"),
            new MenuOption(6, "CTLI", "Transaction Type List (Admin Only)")
        );
        return ResponseEntity.ok(options);
    }
}
