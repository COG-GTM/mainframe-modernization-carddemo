package com.cardemo.batch.service;

import org.springframework.stereotype.Service;

/**
 * Date validation service ported from COBOL program CSUTLDTC.
 * Validates dates including leap year handling.
 * Replicates COBOL's WS-DIV-BY approach for leap year calculation.
 */
@Service
public class DateValidationService {

    private static final int[] DAYS_IN_MONTH = {
            0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    /**
     * Validates a date given as year, month, day components.
     *
     * @param year  the 4-digit year (CCYY)
     * @param month the month (1-12)
     * @param day   the day (1-31)
     * @return true if the date is valid
     */
    public boolean isValidDate(int year, int month, int day) {
        if (year < 1) {
            return false;
        }
        if (month < 1 || month > 12) {
            return false;
        }
        if (day < 1) {
            return false;
        }

        int maxDay = DAYS_IN_MONTH[month];
        if (month == 2 && isLeapYear(year)) {
            maxDay = 29;
        }

        return day <= maxDay;
    }

    /**
     * Validates a date string in YYYY-MM-DD format.
     *
     * @param dateStr the date string
     * @return true if the date is valid
     */
    public boolean isValidDateString(String dateStr) {
        if (dateStr == null || dateStr.length() != 10) {
            return false;
        }
        try {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(5, 7));
            int day = Integer.parseInt(dateStr.substring(8, 10));
            return isValidDate(year, month, day);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Determines if a year is a leap year.
     * Uses the COBOL WS-DIV-BY approach:
     * - Century years (divisible by 100) must also be divisible by 400
     * - Other years need only be divisible by 4
     *
     * @param year the 4-digit year
     * @return true if leap year
     */
    public boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            return year % 400 == 0;
        }
        return year % 4 == 0;
    }

    /**
     * Validates a date of birth - must be a valid date and in the past.
     *
     * @param year  the 4-digit year
     * @param month the month
     * @param day   the day
     * @param currentYear  current year
     * @param currentMonth current month
     * @param currentDay   current day
     * @return true if the date of birth is valid and in the past
     */
    public boolean isValidDateOfBirth(int year, int month, int day,
                                       int currentYear, int currentMonth, int currentDay) {
        if (!isValidDate(year, month, day)) {
            return false;
        }

        // Date must be strictly before current date (COBOL uses strictly greater)
        long dobValue = (long) year * 10000 + month * 100 + day;
        long currentValue = (long) currentYear * 10000 + currentMonth * 100 + currentDay;

        return dobValue < currentValue;
    }
}
