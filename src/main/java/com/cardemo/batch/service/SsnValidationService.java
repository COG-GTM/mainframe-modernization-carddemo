package com.cardemo.batch.service;

import org.springframework.stereotype.Service;

/**
 * SSN validation service ported from COBOL business rules.
 * Validates Social Security Numbers according to SSA rules:
 * - Area number (first 3 digits) must not be 000, 666, or 900-999
 * - Group number (middle 2 digits) must not be 00
 * - Serial number (last 4 digits) must not be 0000
 */
@Service
public class SsnValidationService {

    /**
     * Validates an SSN string in the format "XXX-XX-XXXX" or "XXXXXXXXX".
     *
     * @param ssn the SSN to validate
     * @return true if the SSN is valid, false otherwise
     */
    public boolean isValid(String ssn) {
        if (ssn == null || ssn.isBlank()) {
            return false;
        }

        // Remove dashes for uniform processing
        String digits = ssn.replace("-", "");

        if (digits.length() != 9) {
            return false;
        }

        // Must be all digits
        if (!digits.matches("\\d{9}")) {
            return false;
        }

        int area = Integer.parseInt(digits.substring(0, 3));
        int group = Integer.parseInt(digits.substring(3, 5));
        int serial = Integer.parseInt(digits.substring(5, 9));

        // Area number must not be 000
        if (area == 0) {
            return false;
        }

        // Area number must not be 666
        if (area == 666) {
            return false;
        }

        // Area number must not be 900-999
        if (area >= 900) {
            return false;
        }

        // Group number must not be 00
        if (group == 0) {
            return false;
        }

        // Serial number must not be 0000
        if (serial == 0) {
            return false;
        }

        return true;
    }
}
