package com.carddemo.model;

import com.carddemo.context.UserType;

/** One entry of {@code CDEMO-MENU-OPT} / {@code CDEMO-ADMIN-OPT}. */
public record MenuOption(int number, String name, String programName, UserType userType) {

    public MenuOption {
        if (name.length() > 35) {
            throw new IllegalArgumentException("Menu option name exceeds PIC X(35): " + name);
        }
    }
}
