package com.carddemo.menu.dto;

import com.carddemo.menu.entity.MenuItem;

/**
 * DTO for menu item API responses. Mirrors the COBOL menu option fields
 * from COMEN02Y.cpy / COADM02Y.cpy.
 */
public record MenuItemResponse(
        Long id,
        int optionNumber,
        String optionName,
        String programName,
        String userType,
        String menuGroup,
        int displayOrder
) {

    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getOptionNumber(),
                item.getOptionName(),
                item.getProgramName(),
                item.getUserType(),
                item.getMenuGroup().name(),
                item.getDisplayOrder()
        );
    }
}
