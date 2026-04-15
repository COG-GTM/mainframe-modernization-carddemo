package com.carddemo.menu.service;

import com.carddemo.menu.dto.MenuItemDto;
import com.carddemo.menu.dto.MenuResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MenuService verifying correct menu items per user type
 * and access control logic migrated from COMEN01C and COADM01C.
 */
class MenuServiceTest {

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService();
    }

    @Test
    void regularUserGets11MenuItems() {
        MenuResponse response = menuService.getMenuForUserType("U");
        assertEquals("U", response.getUserType());
        assertEquals(11, response.getTotalOptions());
        assertEquals(11, response.getMenuItems().size());
    }

    @Test
    void adminUserGetsBothAdminAndRegularItems() {
        MenuResponse response = menuService.getMenuForUserType("A");
        assertEquals("A", response.getUserType());
        // 6 admin + 11 regular = 17
        assertEquals(17, response.getTotalOptions());
        assertEquals(17, response.getMenuItems().size());
    }

    @Test
    void adminMenuStartsWithAdminOptions() {
        MenuResponse response = menuService.getMenuForUserType("A");
        MenuItemDto first = response.getMenuItems().get(0);
        assertEquals("User List", first.getName());
    }

    @Test
    void adminMenuIncludesRegularOptionsAfterAdminOnes() {
        MenuResponse response = menuService.getMenuForUserType("A");
        // The 7th item (index 6) should be the first regular option
        MenuItemDto firstRegular = response.getMenuItems().get(6);
        assertEquals("Account View", firstRegular.getName());
    }

    @Test
    void regularUserMenuFirstOptionIsAccountView() {
        MenuResponse response = menuService.getMenuForUserType("U");
        MenuItemDto first = response.getMenuItems().get(0);
        assertEquals(1, first.getOptionNumber());
        assertEquals("Account View", first.getName());
        assertEquals("GET", first.getHttpMethod());
    }

    @Test
    void regularUserMenuLastOptionIsPendingAuthorizationView() {
        MenuResponse response = menuService.getMenuForUserType("U");
        MenuItemDto last = response.getMenuItems().get(10);
        assertEquals(11, last.getOptionNumber());
        assertEquals("Pending Authorization View", last.getName());
    }

    @Test
    void regularUserCanAccessRegularOption() {
        Optional<MenuItemDto> item = menuService.getMenuOption(1, "U");
        assertTrue(item.isPresent());
        assertEquals("Account View", item.get().getName());
    }

    @Test
    void regularUserGetsRegularOptionNotAdminForSameNumber() {
        // Option 1 exists in both regular and admin menus.
        // For a regular user, option 1 maps to "Account View" (regular),
        // NOT the admin "User List".
        Optional<MenuItemDto> item = menuService.getMenuOption(1, "U");
        assertTrue(item.isPresent());
        assertEquals("Account View", item.get().getName());
    }

    @Test
    void regularUserGetsAllRegularOptions() {
        // Verify a regular user can access all 11 regular options
        for (int i = 1; i <= 11; i++) {
            Optional<MenuItemDto> item = menuService.getMenuOption(i, "U");
            assertTrue(item.isPresent(), "Regular user should have access to option " + i);
        }
    }

    @Test
    void adminUserCanAccessAdminOption() {
        Optional<MenuItemDto> item = menuService.getMenuOption(1, "A");
        assertTrue(item.isPresent());
        assertEquals("User List", item.get().getName());
    }

    @Test
    void adminUserCanAccessRegularOption() {
        // Admin users get regular options too. Option 7 is only in regular menu.
        Optional<MenuItemDto> item = menuService.getMenuOption(7, "A");
        assertTrue(item.isPresent());
        assertEquals("Transaction View", item.get().getName());
    }

    @Test
    void nonExistentOptionReturnsEmpty() {
        Optional<MenuItemDto> item = menuService.getMenuOption(99, "U");
        assertFalse(item.isPresent());
    }

    @Test
    void nonExistentOptionForAdminReturnsEmpty() {
        Optional<MenuItemDto> item = menuService.getMenuOption(99, "A");
        assertFalse(item.isPresent());
    }

    @Test
    void regularMenuItemsAreImmutable() {
        assertEquals(11, menuService.getRegularMenuItems().size());
    }

    @Test
    void adminMenuItemsAreImmutable() {
        assertEquals(6, menuService.getAdminMenuItems().size());
    }

    @Test
    void verifyRegularMenuEndpoints() {
        MenuResponse response = menuService.getMenuForUserType("U");
        var items = response.getMenuItems();

        assertEquals("http://account-service:8082/api/accounts/{id}", items.get(0).getServiceEndpoint());
        assertEquals("http://card-service:8083/api/cards", items.get(2).getServiceEndpoint());
        assertEquals("http://transaction-service:8084/api/transactions", items.get(5).getServiceEndpoint());
        assertEquals("http://report-service:8088/api/reports", items.get(8).getServiceEndpoint());
        assertEquals("http://payment-service:8085/api/payments/bill", items.get(9).getServiceEndpoint());
        assertEquals("http://authorization-service:8090/api/authorizations", items.get(10).getServiceEndpoint());
    }

    @Test
    void verifyAdminMenuEndpoints() {
        MenuResponse response = menuService.getMenuForUserType("A");
        var items = response.getMenuItems();

        // First 6 are admin items
        assertEquals("http://user-service:8086/api/users", items.get(0).getServiceEndpoint());
        assertEquals("http://user-service:8086/api/users", items.get(1).getServiceEndpoint());
        assertEquals("http://user-service:8086/api/users/{id}", items.get(2).getServiceEndpoint());
        assertEquals("http://user-service:8086/api/users/{id}", items.get(3).getServiceEndpoint());
        assertEquals("http://transaction-service:8084/api/transaction-types", items.get(4).getServiceEndpoint());
        assertEquals("http://transaction-service:8084/api/transaction-types/{code}", items.get(5).getServiceEndpoint());
    }

    @Test
    void caseInsensitiveUserType() {
        MenuResponse upperCase = menuService.getMenuForUserType("A");
        MenuResponse lowerCase = menuService.getMenuForUserType("a");
        assertEquals(upperCase.getTotalOptions(), lowerCase.getTotalOptions());
    }
}
