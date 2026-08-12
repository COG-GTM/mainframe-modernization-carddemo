package com.carddemo.online.menu;

import java.util.List;

/**
 * COBOL copybooks: COMEN02Y (CARDDEMO-MAIN-MENU-OPTIONS, CDEMO-MENU-OPT-COUNT = 10) and
 * COADM02Y (CARDDEMO-ADMIN-MENU-OPTIONS, CDEMO-ADMIN-OPT-COUNT = 4).
 *
 * <p>The option names keep the wording of the copybook literals; their PIC X(35) padding is
 * dropped. Only {@code CDEMO-*-OPT-COUNT} entries are ever addressed, so the unused table slots
 * (the tables OCCURS 12 and 9 times) are not reproduced.
 */
public final class MenuOptions {

    /** CDEMO-MENU-OPTIONS-DATA of COMEN02Y. */
    public static final List<MenuOption> MAIN = List.of(
            new MenuOption(1, "Account View", "COACTVWC", "U"),
            new MenuOption(2, "Account Update", "COACTUPC", "U"),
            new MenuOption(3, "Credit Card List", "COCRDLIC", "U"),
            new MenuOption(4, "Credit Card View", "COCRDSLC", "U"),
            new MenuOption(5, "Credit Card Update", "COCRDUPC", "U"),
            new MenuOption(6, "Transaction List", "COTRN00C", "U"),
            new MenuOption(7, "Transaction View", "COTRN01C", "U"),
            new MenuOption(8, "Transaction Add", "COTRN02C", "U"),
            new MenuOption(9, "Transaction Reports", "CORPT00C", "U"),
            new MenuOption(10, "Bill Payment", "COBIL00C", "U"));

    /** CDEMO-ADMIN-OPTIONS-DATA of COADM02Y. */
    public static final List<MenuOption> ADMIN = List.of(
            new MenuOption(1, "User List (Security)", "COUSR00C", null),
            new MenuOption(2, "User Add (Security)", "COUSR01C", null),
            new MenuOption(3, "User Update (Security)", "COUSR02C", null),
            new MenuOption(4, "User Delete (Security)", "COUSR03C", null));

    private MenuOptions() {
    }
}
