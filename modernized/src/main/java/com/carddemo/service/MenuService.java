package com.carddemo.service;

import com.carddemo.exception.BusinessRuleException;
import com.carddemo.web.dto.MenuOption;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * COMEN01C / COADM01C - menu navigation.
 *
 * <p>The option tables come from the COMEN02Y and COADM02Y copybooks. Each option carries the
 * REST resource that replaces the CICS program the menu used to XCTL to.
 */
@Service
public class MenuService {

    private static final List<MenuOption> MAIN_MENU = List.of(
            new MenuOption(1, "Account View", "COACTVWC", "GET /api/v1/accounts/{accountId}"),
            new MenuOption(2, "Account Update", "COACTUPC", "PUT /api/v1/accounts/{accountId}"),
            new MenuOption(3, "Credit Card List", "COCRDLIC", "GET /api/v1/cards"),
            new MenuOption(4, "Credit Card View", "COCRDSLC", "GET /api/v1/cards/{cardNumber}"),
            new MenuOption(5, "Credit Card Update", "COCRDUPC", "PUT /api/v1/cards/{cardNumber}"),
            new MenuOption(6, "Transaction List", "COTRN00C", "GET /api/v1/transactions"),
            new MenuOption(7, "Transaction View", "COTRN01C", "GET /api/v1/transactions/{transactionId}"),
            new MenuOption(8, "Transaction Add", "COTRN02C", "POST /api/v1/transactions"),
            new MenuOption(9, "Transaction Reports", "CORPT00C", "POST /api/v1/reports/transactions"),
            new MenuOption(10, "Bill Payment", "COBIL00C", "POST /api/v1/bill-payments"));

    private static final List<MenuOption> ADMIN_MENU = List.of(
            new MenuOption(1, "User List (Security)", "COUSR00C", "GET /api/v1/admin/users"),
            new MenuOption(2, "User Add (Security)", "COUSR01C", "POST /api/v1/admin/users"),
            new MenuOption(3, "User Update (Security)", "COUSR02C", "PUT /api/v1/admin/users/{userId}"),
            new MenuOption(4, "User Delete (Security)", "COUSR03C", "DELETE /api/v1/admin/users/{userId}"));

    public List<MenuOption> mainMenu() {
        return MAIN_MENU;
    }

    public List<MenuOption> adminMenu() {
        return ADMIN_MENU;
    }

    /** Mirrors the option number edit performed by COMEN01C and COADM01C. */
    public MenuOption select(List<MenuOption> menu, Integer option) {
        if (option == null || option < 1 || option > menu.size()) {
            throw new BusinessRuleException("Please enter a valid option number...");
        }
        return menu.get(option - 1);
    }
}
