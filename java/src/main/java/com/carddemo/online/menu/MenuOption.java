package com.carddemo.online.menu;

/**
 * One entry of the menu tables in copybooks COMEN02Y (CDEMO-MENU-OPT) and COADM02Y
 * (CDEMO-ADMIN-OPT).
 *
 * @param number CDEMO-MENU-OPT-NUM / CDEMO-ADMIN-OPT-NUM PIC 9(02)
 * @param name CDEMO-MENU-OPT-NAME / CDEMO-ADMIN-OPT-NAME PIC X(35), trailing blanks removed
 * @param programName CDEMO-MENU-OPT-PGMNAME / CDEMO-ADMIN-OPT-PGMNAME PIC X(08)
 * @param userType CDEMO-MENU-OPT-USRTYPE PIC X(01) ('A' = admin only, 'U' = any user);
 *     the admin table of COADM02Y has no such column, so it is {@code null} there
 */
public record MenuOption(int number, String name, String programName, String userType) {

    /** BUILD-MENU-OPTIONS: {@code STRING num '. ' name INTO WS-MENU-OPT-TXT PIC X(40)}. */
    public String displayText() {
        return "%02d. %s".formatted(number, name);
    }

    /**
     * {@code CDEMO-MENU-OPT-NAME(WS-IDX) DELIMITED BY SPACE} of the "coming soon" message: only
     * the characters up to the first blank of the PIC X(35) name are moved.
     */
    public String nameDelimitedBySpace() {
        int space = name.indexOf(' ');
        return space < 0 ? name : name.substring(0, space);
    }

    /** {@code IF CDEMO-MENU-OPT-PGMNAME(WS-OPTION)(1:5) NOT = 'DUMMY'}. */
    public boolean isImplemented() {
        return !programName.startsWith("DUMMY");
    }
}
