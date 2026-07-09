package com.carddemo.session;

/**
 * Logical action a screen turn represents, independent of the physical key. The CardDemo
 * BMS screens follow a consistent convention (see the screen headers in {@code app/bms/}
 * and the {@code EVALUATE EIBAID} blocks in the online programs): ENTER submits the screen
 * and PF3 returns to the previous screen.
 */
public enum CommonAction {

    /** ENTER — submit / process the current screen. */
    SUBMIT,
    /** PF3 — return to the previous (calling) screen. */
    BACK,
    /** PF4 — clear the current screen. */
    CLEAR,
    /** PF7 — page up / backward in a list. */
    PAGE_UP,
    /** PF8 — page down / forward in a list. */
    PAGE_DOWN,
    /** PF12 — cancel. */
    CANCEL,
    /** Any key with no framework-level meaning; the screen decides. */
    OTHER
}
