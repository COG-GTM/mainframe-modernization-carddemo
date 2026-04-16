package com.carddemo.accountservice.validation;

import com.carddemo.accountservice.dto.AccountUpdateRequest;
import com.carddemo.accountservice.exception.AccountValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountValidator — ported from COACTUPC.cbl validation logic.
 * Covers SSN, phone, date, status, and credit limit validations.
 */
class AccountValidatorTest {

    private AccountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccountValidator();
    }

    // ========================================================================
    // Account ID Validation (from COACTVWC.cbl)
    // ========================================================================

    @Nested
    @DisplayName("Account ID Validation")
    class AccountIdValidation {

        @Test
        @DisplayName("Valid 11-digit account ID is accepted")
        void validAccountId() {
            assertDoesNotThrow(() -> validator.validateAccountId(12345678901L));
        }

        @Test
        @DisplayName("Null account ID throws 'No input received'")
        void nullAccountId() {
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateAccountId(null));
            assertEquals("No input received", ex.getMessage());
        }

        @Test
        @DisplayName("Zero account ID throws validation error")
        void zeroAccountId() {
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateAccountId(0L));
            assertEquals("Account number must be a non zero 11 digit number", ex.getMessage());
        }

        @Test
        @DisplayName("Negative account ID throws validation error")
        void negativeAccountId() {
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateAccountId(-1L));
            assertEquals("Account number must be a non zero 11 digit number", ex.getMessage());
        }

        @Test
        @DisplayName("Account ID exceeding 11 digits throws validation error")
        void tooLargeAccountId() {
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateAccountId(100000000000L));
            assertEquals("Account number must be a non zero 11 digit number", ex.getMessage());
        }

        @Test
        @DisplayName("Maximum valid 11-digit account ID is accepted")
        void maxValidAccountId() {
            assertDoesNotThrow(() -> validator.validateAccountId(99999999999L));
        }

        @Test
        @DisplayName("Single digit account ID is accepted")
        void singleDigitAccountId() {
            assertDoesNotThrow(() -> validator.validateAccountId(1L));
        }
    }

    // ========================================================================
    // SSN Validation (from COACTUPC.cbl lines 117-123, 2447-2464)
    // ========================================================================

    @Nested
    @DisplayName("SSN Validation")
    class SsnValidation {

        @Test
        @DisplayName("Valid SSN 123456789 is accepted")
        void validSsn() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, "123456789",
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("SSN with first 3 digits = 000 is rejected")
        void ssnStartingWith000() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, "000456789",
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("should not be 000, 666, or between 900 and 999")));
        }

        @Test
        @DisplayName("SSN with first 3 digits = 666 is rejected")
        void ssnStartingWith666() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, "666456789",
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("should not be 000, 666, or between 900 and 999")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"900123456", "950123456", "999123456"})
        @DisplayName("SSN with first 3 digits 900-999 is rejected")
        void ssnStartingWith900to999(String ssn) {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, ssn,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("should not be 000, 666, or between 900 and 999")));
        }

        @Test
        @DisplayName("SSN with wrong digit count is rejected")
        void ssnWrongLength() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, "12345",
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("SSN must be 9 digits")));
        }

        @Test
        @DisplayName("SSN with first 3 digits = 001 is accepted")
        void ssnStartingWith001() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, "001456789",
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("SSN with first 3 digits = 899 (just below 900) is accepted")
        void ssnStartingWith899() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, "899456789",
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }
    }

    // ========================================================================
    // Phone Validation (from COACTUPC.cbl lines 82-99)
    // ========================================================================

    @Nested
    @DisplayName("Phone Validation")
    class PhoneValidation {

        @Test
        @DisplayName("Valid phone (555)123-4567 is accepted")
        void validPhone() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    "(555)123-4567", null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("Phone without parentheses is rejected")
        void phoneWithoutParentheses() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    "5551234567", null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Phone format must be (NNN)NNN-NNNN")));
        }

        @Test
        @DisplayName("Phone without dash is rejected")
        void phoneWithoutDash() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    "(555)1234567", null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Phone format must be (NNN)NNN-NNNN")));
        }

        @Test
        @DisplayName("Phone with letters is rejected")
        void phoneWithLetters() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    "(555)ABC-4567", null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Phone format must be (NNN)NNN-NNNN")));
        }
    }

    // ========================================================================
    // Date Validation (from CSUTLDWY.cpy)
    // ========================================================================

    @Nested
    @DisplayName("Date Validation")
    class DateValidation {

        @Test
        @DisplayName("Valid date 2024-01-15 is accepted")
        void validDate() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2024-01-15", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("Invalid date format is rejected")
        void invalidDateFormat() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "01/15/2024", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Date format must be YYYY-MM-DD")));
        }

        @Test
        @DisplayName("Month 13 is rejected")
        void invalidMonth() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2024-13-15", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Month must be between 01 and 12")));
        }

        @Test
        @DisplayName("Month 00 is rejected")
        void zeroMonth() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2024-00-15", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Month must be between 01 and 12")));
        }

        @Test
        @DisplayName("Day 32 is rejected")
        void dayTooHigh() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2024-01-32", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Day must be between 01 and 31")));
        }

        @Test
        @DisplayName("Feb 29 on leap year 2024 is accepted")
        void leapYearFeb29() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2024-02-29", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("Feb 29 on non-leap year 2023 is rejected")
        void nonLeapYearFeb29() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2023-02-29", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Day 29 is not valid for month 02")));
        }

        @Test
        @DisplayName("Feb 29 on century non-leap year 1900 is rejected")
        void centuryNonLeapYear() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "1900-02-29", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Day 29 is not valid for month 02")));
        }

        @Test
        @DisplayName("Feb 29 on century leap year 2000 is accepted")
        void centuryLeapYear() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2000-02-29", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("April 31 is rejected (30-day month)")
        void april31() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2024-04-31", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Day 31 is not valid for month 04")));
        }

        @Test
        @DisplayName("June 30 is accepted (30-day month)")
        void june30() {
            AccountUpdateRequest request = buildRequest(null, null, null,
                    null, null, "2024-06-30", null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }
    }

    // ========================================================================
    // Account Status Validation (from COACTUPC.cbl line 193)
    // ========================================================================

    @Nested
    @DisplayName("Account Status Validation")
    class StatusValidation {

        @Test
        @DisplayName("Status 'Y' is accepted")
        void statusY() {
            AccountUpdateRequest request = buildRequest("Y", null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("Status 'N' is accepted")
        void statusN() {
            AccountUpdateRequest request = buildRequest("N", null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("Status 'X' is rejected")
        void invalidStatus() {
            AccountUpdateRequest request = buildRequest("X", null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Account Active Status must be Y or N")));
        }
    }

    // ========================================================================
    // Credit Limit Validation (S9(10)V99)
    // ========================================================================

    @Nested
    @DisplayName("Credit Limit Validation")
    class CreditLimitValidation {

        @Test
        @DisplayName("Valid credit limit is accepted")
        void validCreditLimit() {
            AccountUpdateRequest request = buildRequest(null, null,
                    new BigDecimal("50000.00"), null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
        }

        @Test
        @DisplayName("Credit limit exceeding 10 integer digits is rejected")
        void creditLimitTooLarge() {
            AccountUpdateRequest request = buildRequest(null, null,
                    new BigDecimal("12345678901.00"), null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Value exceeds maximum of 10 integer digits")));
        }

        @Test
        @DisplayName("Credit limit with more than 2 decimal places is rejected")
        void creditLimitTooManyDecimals() {
            AccountUpdateRequest request = buildRequest(null, null,
                    new BigDecimal("100.123"), null, null, null, null,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null, null);
            AccountValidationException ex = assertThrows(
                    AccountValidationException.class,
                    () -> validator.validateUpdateRequest(request));
            assertTrue(ex.getErrors().stream().anyMatch(e ->
                    e.contains("Value cannot have more than 2 decimal places")));
        }
    }

    // ========================================================================
    // Leap Year Edge Cases
    // ========================================================================

    @Nested
    @DisplayName("Leap Year Calculation")
    class LeapYearTests {

        @Test
        @DisplayName("2024 is a leap year (divisible by 4)")
        void year2024() {
            assertTrue(validator.isLeapYear(2024));
        }

        @Test
        @DisplayName("1900 is NOT a leap year (century not divisible by 400)")
        void year1900() {
            assertFalse(validator.isLeapYear(1900));
        }

        @Test
        @DisplayName("2000 is a leap year (century divisible by 400)")
        void year2000() {
            assertTrue(validator.isLeapYear(2000));
        }

        @Test
        @DisplayName("2023 is NOT a leap year (not divisible by 4)")
        void year2023() {
            assertFalse(validator.isLeapYear(2023));
        }
    }

    // ========================================================================
    // Multiple Validation Errors
    // ========================================================================

    @Test
    @DisplayName("Multiple validation errors are collected and reported together")
    void multipleErrors() {
        AccountUpdateRequest request = buildRequest("X", null,
                new BigDecimal("12345678901.00"), null, null, "bad-date", null,
                null, null, null,
                null, null, null, null,
                "(ABC)DEF-GHIJ", null, null, "000123456",
                null, null, null, null, null, null, null, null, null);
        AccountValidationException ex = assertThrows(
                AccountValidationException.class,
                () -> validator.validateUpdateRequest(request));
        assertTrue(ex.getErrors().size() >= 3,
                "Expected at least 3 errors, got: " + ex.getErrors());
    }

    // ========================================================================
    // Helper to build AccountUpdateRequest
    // ========================================================================

    private AccountUpdateRequest buildRequest(
            String activeStatus, BigDecimal currentBalance, BigDecimal creditLimit,
            BigDecimal cashCreditLimit, String openDate, String expirationDate,
            String reissueDate, BigDecimal currentCycleCredit, BigDecimal currentCycleDebit,
            String groupId,
            String firstName, String middleName, String lastName,
            String addressLine1, String phoneNumber1, String phoneNumber2,
            String stateCode, String ssn,
            String govtIssuedId, String dateOfBirth, String eftAccountId,
            String primaryCardHolderIndicator, Integer ficoCreditScore,
            String addressLine2, String addressLine3, String countryCode, String zip) {
        return new AccountUpdateRequest(
                activeStatus, currentBalance, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currentCycleCredit, currentCycleDebit, groupId,
                firstName, middleName, lastName,
                addressLine1, addressLine2, addressLine3,
                stateCode, countryCode, zip,
                phoneNumber1, phoneNumber2, ssn,
                govtIssuedId, dateOfBirth, eftAccountId,
                primaryCardHolderIndicator, ficoCreditScore);
    }
}
