package com.carddemo.menu.service;

import com.carddemo.menu.dto.MenuItemDto;
import com.carddemo.menu.dto.MenuResponse;
import com.carddemo.menu.model.MenuItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service that provides menu configuration migrated from COBOL programs
 * COMEN01C/COADM01C and copybooks COMEN02Y/COADM02Y.
 *
 * Regular user menu (from COMEN02Y.cpy): 11 options mapping CICS programs
 * to microservice REST endpoints.
 *
 * Admin user menu (from COADM02Y.cpy): 6 additional admin-only options.
 * Admin users receive both admin and regular options.
 */
@Service
public class MenuService {

    private final List<MenuItem> regularMenuItems;
    private final List<MenuItem> adminMenuItems;

    public MenuService() {
        this.regularMenuItems = initRegularMenuItems();
        this.adminMenuItems = initAdminMenuItems();
    }

    /**
     * Regular user menu options migrated from COMEN02Y.cpy.
     * Each entry maps the original COBOL option to its replacement REST endpoint.
     */
    private List<MenuItem> initRegularMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(1, "Account View", "http://account-service:8082/api/accounts/{id}", "GET", "U"));
        items.add(new MenuItem(2, "Account Update", "http://account-service:8082/api/accounts/{id}", "PUT", "U"));
        items.add(new MenuItem(3, "Credit Card List", "http://card-service:8083/api/cards", "GET", "U"));
        items.add(new MenuItem(4, "Credit Card View", "http://card-service:8083/api/cards/{num}", "GET", "U"));
        items.add(new MenuItem(5, "Credit Card Update", "http://card-service:8083/api/cards/{num}", "PUT", "U"));
        items.add(new MenuItem(6, "Transaction List", "http://transaction-service:8084/api/transactions", "GET", "U"));
        items.add(new MenuItem(7, "Transaction View", "http://transaction-service:8084/api/transactions/{id}", "GET", "U"));
        items.add(new MenuItem(8, "Transaction Add", "http://transaction-service:8084/api/transactions", "POST", "U"));
        items.add(new MenuItem(9, "Transaction Reports", "http://report-service:8088/api/reports", "GET", "U"));
        items.add(new MenuItem(10, "Bill Payment", "http://payment-service:8085/api/payments/bill", "POST", "U"));
        items.add(new MenuItem(11, "Pending Authorization View", "http://authorization-service:8090/api/authorizations", "GET", "U"));
        return Collections.unmodifiableList(items);
    }

    /**
     * Admin menu options migrated from COADM02Y.cpy.
     * These are only accessible to admin users (userType 'A').
     */
    private List<MenuItem> initAdminMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(1, "User List", "http://user-service:8086/api/users", "GET", "A"));
        items.add(new MenuItem(2, "User Add", "http://user-service:8086/api/users", "POST", "A"));
        items.add(new MenuItem(3, "User Update", "http://user-service:8086/api/users/{id}", "PUT", "A"));
        items.add(new MenuItem(4, "User Delete", "http://user-service:8086/api/users/{id}", "DELETE", "A"));
        items.add(new MenuItem(5, "Transaction Type List", "http://transaction-service:8084/api/transaction-types", "GET", "A"));
        items.add(new MenuItem(6, "Transaction Type Maintenance", "http://transaction-service:8084/api/transaction-types/{code}", "PUT", "A"));
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns menu items for the given user type.
     * Regular users ('U') see only regular options.
     * Admin users ('A') see admin options followed by regular options.
     *
     * This mirrors the COBOL logic where COMEN01C serves regular users
     * and COADM01C serves admin users.
     */
    public MenuResponse getMenuForUserType(String userType) {
        List<MenuItemDto> dtos = new ArrayList<>();

        if ("A".equalsIgnoreCase(userType)) {
            for (MenuItem item : adminMenuItems) {
                dtos.add(toDto(item));
            }
            for (MenuItem item : regularMenuItems) {
                dtos.add(toDto(item));
            }
            return new MenuResponse("A", dtos.size(), dtos);
        }

        for (MenuItem item : regularMenuItems) {
            dtos.add(toDto(item));
        }
        return new MenuResponse("U", dtos.size(), dtos);
    }

    /**
     * Returns a specific menu option by number.
     * Mirrors the PROCESS-ENTER-KEY logic from COMEN01C/COADM01C:
     * - Validates the option number is in range
     * - Checks user type access (regular users cannot access admin-only options)
     *
     * @return the matching menu item, or empty if not found
     * @throws SecurityException if a regular user tries to access an admin option
     */
    public Optional<MenuItemDto> getMenuOption(int optionNumber, String userType) {
        if ("A".equalsIgnoreCase(userType)) {
            // Admin users: first check admin options, then regular options
            Optional<MenuItem> adminItem = adminMenuItems.stream()
                    .filter(item -> item.getOptionNumber() == optionNumber)
                    .findFirst();
            if (adminItem.isPresent()) {
                return adminItem.map(this::toDto);
            }
            return regularMenuItems.stream()
                    .filter(item -> item.getOptionNumber() == optionNumber)
                    .findFirst()
                    .map(this::toDto);
        }

        // Regular users: only check regular menu items
        Optional<MenuItem> regularItem = regularMenuItems.stream()
                .filter(item -> item.getOptionNumber() == optionNumber)
                .findFirst();
        if (regularItem.isPresent()) {
            return regularItem.map(this::toDto);
        }

        // Check if it's an admin-only option — mirrors COMEN01C access check
        Optional<MenuItem> adminOnly = adminMenuItems.stream()
                .filter(item -> item.getOptionNumber() == optionNumber)
                .findFirst();
        if (adminOnly.isPresent()) {
            throw new SecurityException("No access - Admin Only option");
        }

        return Optional.empty();
    }

    public List<MenuItem> getRegularMenuItems() {
        return regularMenuItems;
    }

    public List<MenuItem> getAdminMenuItems() {
        return adminMenuItems;
    }

    private MenuItemDto toDto(MenuItem item) {
        return new MenuItemDto(
                item.getOptionNumber(),
                item.getName(),
                item.getServiceEndpoint(),
                item.getHttpMethod()
        );
    }
}
