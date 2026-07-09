package com.carddemo.service.account;

import java.time.LocalDate;

/**
 * Port of the reusable {@code CCYYMMDD} date edits in copybook {@code CSUTLDPY.cpy}
 * ({@code EDIT-DATE-CCYYMMDD} and the year/month/day/combination paragraphs) plus the
 * {@code EDIT-DATE-OF-BIRTH} future-date reasonableness check.
 *
 * <p>Each method returns the first {@code WS-RETURN-MSG} the COBOL edits would produce, or
 * {@code null} when the date passes. Dates arrive as the three BMS component fields
 * (year / month / day).</p>
 */
final class DateEditor {

    private DateEditor() {
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isDigits(String value, int expectedLen) {
        String v = value.trim();
        if (v.length() != expectedLen) {
            return false;
        }
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** {@code EDIT-DATE-CCYYMMDD}: full year/month/day/combination edit. */
    static String editDate(String name, String year, String month, String day) {
        // --- EDIT-YEAR-CCYY ---
        if (isBlank(year)) {
            return name + " : Year must be supplied.";
        }
        if (!isDigits(year, 4)) {
            return name + " must be 4 digit number.";
        }
        String century = year.trim().substring(0, 2);
        if (!century.equals("19") && !century.equals("20")) {
            return name + " : Century is not valid.";
        }

        // --- EDIT-MONTH ---
        if (isBlank(month)) {
            return name + " : Month must be supplied.";
        }
        if (!isDigits(month, 2) || monthValue(month) < 1 || monthValue(month) > 12) {
            return name + ": Month must be a number between 1 and 12.";
        }

        // --- EDIT-DAY ---
        if (isBlank(day)) {
            return name + " : Day must be supplied.";
        }
        if (!isDigits(day, 2)) {
            return name + ":day must be a number between 1 and 31.";
        }
        int dd = Integer.parseInt(day.trim());
        if (dd < 1 || dd > 31) {
            return name + ":day must be a number between 1 and 31.";
        }

        // --- EDIT-DAY-MONTH-YEAR combinations ---
        int mm = monthValue(month);
        int ccyy = Integer.parseInt(year.trim());
        if (dd == 31 && !is31DayMonth(mm)) {
            return name + ":Cannot have 31 days in this month.";
        }
        if (mm == 2 && dd == 30) {
            return name + ":Cannot have 30 days in this month.";
        }
        if (mm == 2 && dd == 29 && !isLeapYear(ccyy)) {
            return name + ":Not a leap year.Cannot have 29 days in this month.";
        }
        return null;
    }

    /** {@code EDIT-DATE-OF-BIRTH}: the date must not be today or in the future. */
    static String editDateOfBirth(String name, String year, String month, String day) {
        LocalDate dob = LocalDate.of(
            Integer.parseInt(year.trim()),
            Integer.parseInt(month.trim()),
            Integer.parseInt(day.trim()));
        if (!LocalDate.now().isAfter(dob)) {
            return name + ":cannot be in the future ";
        }
        return null;
    }

    private static int monthValue(String month) {
        try {
            return Integer.parseInt(month.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean is31DayMonth(int mm) {
        return mm == 1 || mm == 3 || mm == 5 || mm == 7 || mm == 8 || mm == 10 || mm == 12;
    }

    /**
     * Leap-year test matching {@code EDIT-DAY-MONTH-YEAR}: century years (yy = 00) must be
     * divisible by 400, all other years by 4.
     */
    private static boolean isLeapYear(int ccyy) {
        int divisor = (ccyy % 100 == 0) ? 400 : 4;
        return ccyy % divisor == 0;
    }
}
