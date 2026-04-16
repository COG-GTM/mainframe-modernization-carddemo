package com.carddemo.accountservice.validation;

import com.carddemo.accountservice.dto.AccountUpdateRequest;
import com.carddemo.accountservice.exception.AccountValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation logic ported from COACTUPC.cbl (4237 lines).
 * Preserves all business rules and error messages character-for-character.
 *
 * Key validations:
 * - SSN: First 3 digits cannot be 000, 666, or 900-999 (lines 117-123, 2447-2464)
 * - Phone: Must match (NNN)NNN-NNNN format (lines 82-99)
 * - Date: Full leap year validation, month/day range checks (CSUTLDWY.cpy)
 * - Account status: Y/N only (line 193)
 * - Credit limits: Must be valid decimal values (S9(10)V99)
 */
@Component
public class AccountValidator {

    /** Phone format: (NNN)NNN-NNNN — from WS-EDIT-US-PHONE-NUM-X REDEFINES */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\(\\d{3}\\)\\d{3}-\\d{4}$");

    /** Date format: YYYY-MM-DD */
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /**
     * Validates an account ID per COACTVWC.cbl rules.
     * Must be a non-zero 11-digit number.
     */
    public void validateAccountId(Long accountId) {
        if (accountId == null) {
            throw new AccountValidationException("No input received");
        }
        if (accountId <= 0 || accountId > 99999999999L) {
            throw new AccountValidationException(
                    "Account number must be a non zero 11 digit number");
        }
    }

    /**
     * Validates all fields in an account update request.
     * Collects all errors before throwing, mirroring COACTUPC.cbl behavior.
     */
    public void validateUpdateRequest(AccountUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        validateActiveStatus(request.activeStatus(), errors);
        validateCreditLimit(request.creditLimit(), "Credit Limit", errors);
        validateCreditLimit(request.cashCreditLimit(), "Cash Credit Limit", errors);
        validateCreditLimit(request.currentBalance(), "Current Balance", errors);
        validateCreditLimit(request.currentCycleCredit(), "Current Cycle Credit", errors);
        validateCreditLimit(request.currentCycleDebit(), "Current Cycle Debit", errors);

        if (request.openDate() != null && !request.openDate().isBlank()) {
            validateDate(request.openDate(), "Open Date", errors);
        }
        if (request.expirationDate() != null && !request.expirationDate().isBlank()) {
            validateDate(request.expirationDate(), "Expiration Date", errors);
        }
        if (request.reissueDate() != null && !request.reissueDate().isBlank()) {
            validateDate(request.reissueDate(), "Reissue Date", errors);
        }
        if (request.dateOfBirth() != null && !request.dateOfBirth().isBlank()) {
            validateDate(request.dateOfBirth(), "Date of Birth", errors);
        }

        if (request.ssn() != null && !request.ssn().isBlank()) {
            validateSsn(request.ssn(), "SSN", errors);
        }

        if (request.phoneNumber1() != null && !request.phoneNumber1().isBlank()) {
            validatePhone(request.phoneNumber1(), "Phone Number 1", errors);
        }
        if (request.phoneNumber2() != null && !request.phoneNumber2().isBlank()) {
            validatePhone(request.phoneNumber2(), "Phone Number 2", errors);
        }

        if (!errors.isEmpty()) {
            throw new AccountValidationException(errors);
        }
    }

    /**
     * Validates account active status.
     * From COACTUPC.cbl line 193: FLG-ACCT-STATUS-ISVALID VALUES 'Y', 'N'.
     */
    void validateActiveStatus(String status, List<String> errors) {
        if (status != null && !status.equals("Y") && !status.equals("N")) {
            errors.add("Account Active Status must be Y or N");
        }
    }

    /**
     * Validates SSN per COACTUPC.cbl lines 117-123, 2447-2464.
     * Part 1 (first 3 digits): should not be 000, 666, or between 900 and 999.
     */
    void validateSsn(String ssn, String fieldName, List<String> errors) {
        if (ssn == null || ssn.isBlank()) {
            return;
        }

        String digitsOnly = ssn.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 9) {
            errors.add(fieldName + ": SSN must be 9 digits");
            return;
        }

        int part1 = Integer.parseInt(digitsOnly.substring(0, 3));
        if (part1 == 0 || part1 == 666 || (part1 >= 900 && part1 <= 999)) {
            errors.add(fieldName + ": should not be 000, 666, or between 900 and 999");
        }
    }

    /**
     * Validates phone number format per COACTUPC.cbl lines 82-99.
     * Must match (NNN)NNN-NNNN pattern.
     */
    void validatePhone(String phone, String fieldName, List<String> errors) {
        if (phone == null || phone.isBlank()) {
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.add(fieldName + ": Phone format must be (NNN)NNN-NNNN");
        }
    }

    /**
     * Validates date per CSUTLDWY.cpy rules.
     * Full validation: format, month range, day range with leap year support.
     */
    void validateDate(String date, String fieldName, List<String> errors) {
        if (date == null || date.isBlank()) {
            return;
        }

        if (!DATE_PATTERN.matcher(date).matches()) {
            errors.add(fieldName + ": Date format must be YYYY-MM-DD");
            return;
        }

        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        // WS-VALID-MONTH VALUES 1 THROUGH 12
        if (month < 1 || month > 12) {
            errors.add(fieldName + ": Month must be between 01 and 12");
            return;
        }

        // WS-VALID-DAY VALUES 1 THROUGH 31
        if (day < 1 || day > 31) {
            errors.add(fieldName + ": Day must be between 01 and 31");
            return;
        }

        // Year must be positive
        if (year < 1) {
            errors.add(fieldName + ": Year must be positive");
            return;
        }

        // WS-31-DAY-MONTH VALUES 1, 3, 5, 7, 8, 10, 12
        int maxDay = maxDayForMonth(month, year);
        if (day > maxDay) {
            errors.add(fieldName + ": Day " + String.format("%02d", day)
                    + " is not valid for month " + String.format("%02d", month));
        }
    }

    /**
     * Returns the maximum valid day for a given month and year.
     * Implements leap year logic from CSUTLDWY.cpy.
     */
    int maxDayForMonth(int month, int year) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;   // WS-31-DAY-MONTH
            case 4, 6, 9, 11 -> 30;               // 30-day months
            case 2 -> isLeapYear(year) ? 29 : 28;  // WS-FEBRUARY with leap year
            default -> 31;
        };
    }

    /**
     * Leap year calculation matching CSUTLDWY.cpy logic.
     * Divisible by 4, except centuries unless divisible by 400.
     */
    boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true;
        }
        if (year % 100 == 0) {
            return false;
        }
        return year % 4 == 0;
    }

    /**
     * Validates credit limit fields (S9(10)V99 format).
     */
    void validateCreditLimit(java.math.BigDecimal value, String fieldName, List<String> errors) {
        if (value == null) {
            return;
        }
        // S9(10)V99: max 10 integer digits + 2 decimal places
        if (value.precision() - value.scale() > 10) {
            errors.add(fieldName + ": Value exceeds maximum of 10 integer digits");
        }
        if (value.scale() > 2) {
            errors.add(fieldName + ": Value cannot have more than 2 decimal places");
        }
    }
}
