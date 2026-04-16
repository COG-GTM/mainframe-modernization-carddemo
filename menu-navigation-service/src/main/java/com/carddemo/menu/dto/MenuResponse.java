package com.carddemo.menu.dto;

import java.util.List;

/**
 * Top-level response for GET /api/v1/menu.
 * Wraps the list of menu items with metadata about the menu context.
 */
public record MenuResponse(
        String menuType,
        int totalOptions,
        List<MenuItemResponse> menuItems
) {
}
