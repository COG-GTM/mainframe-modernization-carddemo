package com.carddemo.transaction.validation;

import com.carddemo.transaction.dto.TransactionAddRequest;
import com.carddemo.transaction.exception.TransactionValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransactionValidator.
 * Verifies all COBOL error messages are preserved character-for-character.
 */
class TransactionValidatorTest {

    // --- Key Field Validation Tests (COTRN02C VALIDATE-INPUT-KEY-FIELDS) ---

    @Test
    void validateKeyFields_bothEmpty_throwsError() {
        TransactionAddRequest request = buildRequest(null, null);
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateKeyFields(request));
        assertEquals("Account or Card Number must be entered...", ex.getMessage());
    }

    @Test
    void validateKeyFields_accountIdNonNumeric_throwsError() {
        TransactionAddRequest request = buildRequest("ABC123", null);
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateKeyFields(request));
        assertEquals("Account ID must be Numeric...", ex.getMessage());
    }

    @Test
    void validateKeyFields_cardNumberNonNumeric_throwsError() {
        TransactionAddRequest request = buildRequest(null, "XXXX1234");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateKeyFields(request));
        assertEquals("Card Number must be Numeric...", ex.getMessage());
    }

    @Test
    void validateKeyFields_validAccountId_passes() {
        TransactionAddRequest request = buildRequest("12345678901", null);
        assertDoesNotThrow(() -> TransactionValidator.validateKeyFields(request));
    }

    @Test
    void validateKeyFields_validCardNumber_passes() {
        TransactionAddRequest request = buildRequest(null, "4567890123456789");
        assertDoesNotThrow(() -> TransactionValidator.validateKeyFields(request));
    }

    // --- Data Field Validation Tests (COTRN02C VALIDATE-INPUT-DATA-FIELDS) ---

    @Test
    void validateDataFields_emptyTypeCd_throwsError() {
        TransactionAddRequest request = buildFullRequest("", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Type CD can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyCategoryCd_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Category CD can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptySource_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Source can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyDescription_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Description can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyAmount_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Amount can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyOrigDate_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "+00000100.00", "", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Orig Date can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyProcDate_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Proc Date can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyMerchantId_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Merchant ID can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyMerchantName_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Merchant Name can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyMerchantCity_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Merchant City can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_emptyMerchantZip_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Merchant Zip can NOT be empty...", ex.getMessage());
    }

    @Test
    void validateDataFields_nonNumericTypeCd_throwsError() {
        TransactionAddRequest request = buildFullRequest("AB", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Type CD must be Numeric...", ex.getMessage());
    }

    @Test
    void validateDataFields_nonNumericCategoryCd_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "ABCD", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Category CD must be Numeric...", ex.getMessage());
    }

    @Test
    void validateDataFields_nonNumericMerchantId_throwsError() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "SRC", "DESC",
                "+00000100.00", "2024-01-15", "2024-01-15", "ABC456789",
                "Merchant", "City", "12345");
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDataFields(request));
        assertEquals("Merchant ID must be Numeric...", ex.getMessage());
    }

    // --- Amount Format Tests ---

    @Test
    void validateAmountFormat_cobolFormat_passes() {
        assertDoesNotThrow(() -> TransactionValidator.validateAmountFormat("+00001234.56"));
    }

    @Test
    void validateAmountFormat_negativeCobolFormat_passes() {
        assertDoesNotThrow(() -> TransactionValidator.validateAmountFormat("-99999999.99"));
    }

    @Test
    void validateAmountFormat_standardDecimal_passes() {
        assertDoesNotThrow(() -> TransactionValidator.validateAmountFormat("123.45"));
    }

    @Test
    void validateAmountFormat_invalid_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateAmountFormat("abc"));
        assertEquals("Amount should be in format -99999999.99", ex.getMessage());
    }

    @Test
    void validateAmountFormat_tooLarge_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateAmountFormat("100000000.00"));
        assertEquals("Amount should be in format -99999999.99", ex.getMessage());
    }

    // --- Date Format Tests ---

    @Test
    void validateDateFormat_validDate_passes() {
        assertDoesNotThrow(
                () -> TransactionValidator.validateDateFormat("2024-01-15",
                        "Orig Date should be in format YYYY-MM-DD"));
    }

    @Test
    void validateDateFormat_invalidFormat_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDateFormat("01/15/2024",
                        "Orig Date should be in format YYYY-MM-DD"));
        assertEquals("Orig Date should be in format YYYY-MM-DD", ex.getMessage());
    }

    @Test
    void validateDateFormat_nonNumericYear_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDateFormat("ABCD-01-15",
                        "Proc Date should be in format YYYY-MM-DD"));
        assertEquals("Proc Date should be in format YYYY-MM-DD", ex.getMessage());
    }

    @Test
    void validateDateFormat_wrongSeparator_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDateFormat("2024/01/15",
                        "Orig Date should be in format YYYY-MM-DD"));
        assertEquals("Orig Date should be in format YYYY-MM-DD", ex.getMessage());
    }

    @Test
    void validateDateFormat_tooShort_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateDateFormat("2024-1-5",
                        "Orig Date should be in format YYYY-MM-DD"));
        assertEquals("Orig Date should be in format YYYY-MM-DD", ex.getMessage());
    }

    // --- Confirmation Tests ---

    @Test
    void validateConfirmation_notConfirmed_throwsError() {
        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> TransactionValidator.validateConfirmation(false));
        assertEquals("Confirm to add this transaction...", ex.getMessage());
    }

    @Test
    void validateConfirmation_confirmed_passes() {
        assertDoesNotThrow(() -> TransactionValidator.validateConfirmation(true));
    }

    // --- All Valid Data Fields ---

    @Test
    void validateDataFields_allValid_passes() {
        TransactionAddRequest request = buildFullRequest("01", "1234", "ONLINE", "Test purchase",
                "+00000100.00", "2024-01-15", "2024-01-15", "123456789",
                "Test Merchant", "New York", "10001");
        assertDoesNotThrow(() -> TransactionValidator.validateDataFields(request));
    }

    // --- Helpers ---

    private TransactionAddRequest buildRequest(String accountId, String cardNumber) {
        return new TransactionAddRequest(accountId, cardNumber, "01", "1234",
                "SRC", "DESC", "+00000100.00", "2024-01-15", "2024-01-15",
                "123456789", "Merchant", "City", "12345", true);
    }

    private TransactionAddRequest buildFullRequest(String typeCd, String catCd, String source,
                                                    String desc, String amt, String origDate,
                                                    String procDate, String merchantId,
                                                    String merchantName, String merchantCity,
                                                    String merchantZip) {
        return new TransactionAddRequest("12345678901", null, typeCd, catCd, source, desc,
                amt, origDate, procDate, merchantId, merchantName, merchantCity, merchantZip, true);
    }
}
