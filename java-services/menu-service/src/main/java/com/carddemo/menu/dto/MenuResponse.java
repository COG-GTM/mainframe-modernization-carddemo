package com.carddemo.menu.dto;

import java.util.List;

/**
 * Response wrapper for menu endpoints.
 */
public class MenuResponse {

    private String userType;
    private int totalOptions;
    private List<MenuItemDto> menuItems;

    public MenuResponse() {
    }

    public MenuResponse(String userType, int totalOptions, List<MenuItemDto> menuItems) {
        this.userType = userType;
        this.totalOptions = totalOptions;
        this.menuItems = menuItems;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public int getTotalOptions() {
        return totalOptions;
    }

    public void setTotalOptions(int totalOptions) {
        this.totalOptions = totalOptions;
    }

    public List<MenuItemDto> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDto> menuItems) {
        this.menuItems = menuItems;
    }
}
