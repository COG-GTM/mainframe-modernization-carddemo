package com.carddemo.menu.controller;

import com.carddemo.menu.dto.MenuItemResponse;
import com.carddemo.menu.dto.MenuResponse;
import com.carddemo.menu.dto.NavigationContextResponse;
import com.carddemo.menu.dto.NavigationRequest;
import com.carddemo.menu.dto.NavigationResponse;
import com.carddemo.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the CardDemo Menu Navigation Service.
 * Exposes endpoints for menu retrieval and navigation, porting the
 * CICS transaction interactions from COMEN01C.cbl and COADM01C.cbl.
 */
@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * GET /api/v1/menu?userType={U|A}
     *
     * Returns menu items filtered by user role.
     * - userType=U returns the regular user menu (10 options from COMEN02Y.cpy)
     * - userType=A returns the admin menu (4 options from COADM02Y.cpy)
     */
    @GetMapping
    public ResponseEntity<MenuResponse> getMenu(
            @RequestParam(name = "userType", defaultValue = "U") String userType) {
        MenuResponse response = menuService.getMenuForUser(userType);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/menu/items
     *
     * Returns all active menu items across both admin and regular menus.
     */
    @GetMapping("/items")
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        return ResponseEntity.ok(menuService.getAllMenuItems());
    }

    /**
     * POST /api/v1/menu/navigate
     *
     * Processes a menu option selection, equivalent to PROCESS-ENTER-KEY
     * in the COBOL programs. Validates the option, checks access rights,
     * updates navigation context, and returns the target program info.
     */
    @PostMapping("/navigate")
    public ResponseEntity<NavigationResponse> navigate(
            @Valid @RequestBody NavigationRequest request) {
        NavigationResponse response = menuService.navigate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/menu/context/{sessionId}
     *
     * Retrieves the current navigation context for a session.
     * Equivalent to reading the CARDDEMO-COMMAREA.
     */
    @GetMapping("/context/{sessionId}")
    public ResponseEntity<NavigationContextResponse> getNavigationContext(
            @PathVariable String sessionId) {
        NavigationContextResponse response = menuService.getNavigationContext(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/menu/back/{sessionId}
     *
     * Handles back navigation (PF3 key equivalent).
     * Routes back to the sign-on screen (COSGN00C), matching
     * the RETURN-TO-SIGNON-SCREEN paragraph.
     */
    @PostMapping("/back/{sessionId}")
    public ResponseEntity<NavigationResponse> navigateBack(
            @PathVariable String sessionId) {
        NavigationResponse response = menuService.navigateBack(sessionId);
        return ResponseEntity.ok(response);
    }
}
