package com.carddemo.menu.controller;

import com.carddemo.menu.dto.MenuItemDto;
import com.carddemo.menu.dto.MenuResponse;
import com.carddemo.menu.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller replacing CICS screen navigation from COMEN01C (regular menu)
 * and COADM01C (admin menu).
 *
 * Provides endpoints for a frontend to retrieve menu/navigation configuration
 * instead of the original 3270 terminal-based CICS menus.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * GET /api/menu?userType=U — regular user menu (11 options)
     * GET /api/menu?userType=A — admin user menu (admin + regular options)
     */
    @GetMapping
    public ResponseEntity<MenuResponse> getMenu(
            @RequestParam(defaultValue = "U") String userType) {
        MenuResponse response = menuService.getMenuForUserType(userType);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/menu/{optionNumber}?userType=U — get details for a specific option.
     *
     * Returns 403 if a regular user requests an admin-only option,
     * mirroring the COMEN01C PROCESS-ENTER-KEY access check.
     */
    @GetMapping("/{optionNumber}")
    public ResponseEntity<?> getMenuOption(
            @PathVariable int optionNumber,
            @RequestParam(defaultValue = "U") String userType) {
        try {
            Optional<MenuItemDto> item = menuService.getMenuOption(optionNumber, userType);
            if (item.isPresent()) {
                return ResponseEntity.ok(item.get());
            }
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
