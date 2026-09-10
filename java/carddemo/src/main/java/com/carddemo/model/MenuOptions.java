package com.carddemo.model;

import com.carddemo.context.UserType;
import java.util.List;

/**
 * Menu tables of copybooks {@code COMEN02Y} (main menu) and {@code COADM02Y} (admin menu). The
 * COBOL {@code OCCURS} tables are exposed as immutable lists.
 */
public final class MenuOptions {

    /** {@code CARDDEMO-MAIN-MENU-OPTIONS} */
    public static final List<MenuOption> MAIN_MENU = List.of(
            new MenuOption(1, "Account View", "COACTVWC", UserType.USER),
            new MenuOption(2, "Account Update", "COACTUPC", UserType.USER),
            new MenuOption(3, "Credit Card List", "COCRDLIC", UserType.USER),
            new MenuOption(4, "Credit Card View", "COCRDSLC", UserType.USER),
            new MenuOption(5, "Credit Card Update", "COCRDUPC", UserType.USER),
            new MenuOption(6, "Transaction List", "COTRN00C", UserType.USER),
            new MenuOption(7, "Transaction View", "COTRN01C", UserType.USER),
            new MenuOption(8, "Transaction Add", "COTRN02C", UserType.USER),
            new MenuOption(9, "Transaction Reports", "CORPT00C", UserType.USER),
            new MenuOption(10, "Bill Payment", "COBIL00C", UserType.USER));

    /** {@code CARDDEMO-ADMIN-MENU-OPTIONS} */
    public static final List<MenuOption> ADMIN_MENU = List.of(
            new MenuOption(1, "User List (Security)", "COUSR00C", UserType.ADMIN),
            new MenuOption(2, "User Add (Security)", "COUSR01C", UserType.ADMIN),
            new MenuOption(3, "User Update (Security)", "COUSR02C", UserType.ADMIN),
            new MenuOption(4, "User Delete (Security)", "COUSR03C", UserType.ADMIN));

    private MenuOptions() {
    }

    public static MenuOption find(List<MenuOption> menu, int number) {
        return menu.stream()
                .filter(option -> option.number() == number)
                .findFirst()
                .orElse(null);
    }
}
