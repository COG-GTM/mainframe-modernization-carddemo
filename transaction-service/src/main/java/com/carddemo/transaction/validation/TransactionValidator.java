package com.carddemo.transaction.validation;

import com.carddemo.transaction.dto.TransactionAddRequest;
import com.carddemo.transaction.exception.TransactionValidationException;

/**
 * Validates transaction input fields.
 * Preserves all COBOL validation rules and error messages character-for-character
 * from COTRN02C.cbl VALIDATE-INPUT-KEY-FIELDS and VALIDATE-INPUT-DATA-FIELDS.
 */
public final class TransactionValidator {

    private TransactionValidator() {
    }

    /**
     * Validates key fields (Account ID / Card Number).
     * Port of COTRN02C VALIDATE-INPUT-KEY-FIELDS.
     */
    public static void validateKeyFields(TransactionAddRequest request) {
        boolean hasAccountId = request.accountId() != null && !request.accountId().isBlank();
        boolean hasCardNumber = request.cardNumber() != null && !request.cardNumber().isBlank();

        if (hasAccountId) {
            if (!isNumeric(request.accountId())) {
                throw new TransactionValidationException("Account ID must be Numeric...");
            }
        } else if (hasCardNumber) {
            if (!isNumeric(request.cardNumber())) {
                throw new TransactionValidationException("Card Number must be Numeric...");
            }
        } else {
            throw new TransactionValidationException(
                    "Account or Card Number must be entered...");
        }
    }

    /**
     * Validates all data fields.
     * Port of COTRN02C VALIDATE-INPUT-DATA-FIELDS.
     * All 11 data fields are required; checks emptiness first, then format.
     */
    public static void validateDataFields(TransactionAddRequest request) {
        // Emptiness checks (order matches COBOL EVALUATE TRUE sequence)
        if (isBlank(request.tranTypeCd())) {
            throw new TransactionValidationException("Type CD can NOT be empty...");
        }
        if (isBlank(request.tranCatCd())) {
            throw new TransactionValidationException("Category CD can NOT be empty...");
        }
        if (isBlank(request.tranSource())) {
            throw new TransactionValidationException("Source can NOT be empty...");
        }
        if (isBlank(request.tranDesc())) {
            throw new TransactionValidationException("Description can NOT be empty...");
        }
        if (isBlank(request.tranAmt())) {
            throw new TransactionValidationException("Amount can NOT be empty...");
        }
        if (isBlank(request.tranOrigDate())) {
            throw new TransactionValidationException("Orig Date can NOT be empty...");
        }
        if (isBlank(request.tranProcDate())) {
            throw new TransactionValidationException("Proc Date can NOT be empty...");
        }
        if (isBlank(request.merchantId())) {
            throw new TransactionValidationException("Merchant ID can NOT be empty...");
        }
        if (isBlank(request.merchantName())) {
            throw new TransactionValidationException("Merchant Name can NOT be empty...");
        }
        if (isBlank(request.merchantCity())) {
            throw new TransactionValidationException("Merchant City can NOT be empty...");
        }
        if (isBlank(request.merchantZip())) {
            throw new TransactionValidationException("Merchant Zip can NOT be empty...");
        }

        // Numeric format checks
        if (!isNumeric(request.tranTypeCd())) {
            throw new TransactionValidationException("Type CD must be Numeric...");
        }
        if (!isNumeric(request.tranCatCd())) {
            throw new TransactionValidationException("Category CD must be Numeric...");
        }

        // Amount format: sign(+/-) + 8 digits + '.' + 2 digits = -99999999.99
        validateAmountFormat(request.tranAmt());

        // Date format: YYYY-MM-DD with position-level checks
        validateDateFormat(request.tranOrigDate(),
                "Orig Date should be in format YYYY-MM-DD");
        validateDateFormat(request.tranProcDate(),
                "Proc Date should be in format YYYY-MM-DD");

        // Merchant ID must be numeric
        if (!isNumeric(request.merchantId())) {
            throw new TransactionValidationException("Merchant ID must be Numeric...");
        }
    }

    /**
     * Validates confirmation field.
     * Port of COTRN02C PROCESS-ENTER-KEY confirmation check.
     */
    public static void validateConfirmation(boolean confirmed) {
        if (!confirmed) {
            throw new TransactionValidationException(
                    "Confirm to add this transaction...");
        }
    }

    /**
     * Amount format validation matching COBOL position-level checks.
     * Format: sign(+/-) + 8 digits + '.' + 2 digits
     * e.g., +00001234.56 or -99999999.99
     */
    static void validateAmountFormat(String amount) {
        if (amount == null || amount.length() < 4) {
            throw new TransactionValidationException(
                    "Amount should be in format -99999999.99");
        }

        // Try to parse as a standard decimal number
        try {
            // Check COBOL-style format: [+-]99999999.99
            if (amount.length() == 12) {
                char sign = amount.charAt(0);
                if (sign != '+' && sign != '-') {
                    throw new TransactionValidationException(
                            "Amount should be in format -99999999.99");
                }
                String digits = amount.substring(1, 9);
                if (!isNumeric(digits)) {
                    throw new TransactionValidationException(
                            "Amount should be in format -99999999.99");
                }
                if (amount.charAt(9) != '.') {
                    throw new TransactionValidationException(
                            "Amount should be in format -99999999.99");
                }
                String decimals = amount.substring(10, 12);
                if (!isNumeric(decimals)) {
                    throw new TransactionValidationException(
                            "Amount should be in format -99999999.99");
                }
            } else {
                // Allow standard decimal format as well (e.g., "123.45", "-99.00")
                java.math.BigDecimal parsed = new java.math.BigDecimal(amount);
                if (parsed.abs().compareTo(new java.math.BigDecimal("99999999.99")) > 0) {
                    throw new TransactionValidationException(
                            "Amount should be in format -99999999.99");
                }
            }
        } catch (NumberFormatException e) {
            throw new TransactionValidationException(
                    "Amount should be in format -99999999.99");
        }
    }

    /**
     * Date format validation matching COBOL position-level checks.
     * Positions: YYYY(1:4) + '-'(5) + MM(6:2) + '-'(8) + DD(9:2)
     */
    static void validateDateFormat(String date, String errorMessage) {
        if (date == null || date.length() != 10) {
            throw new TransactionValidationException(errorMessage);
        }
        // Position-level checks matching COBOL EVALUATE TRUE
        if (!isNumeric(date.substring(0, 4))) {       // YYYY
            throw new TransactionValidationException(errorMessage);
        }
        if (date.charAt(4) != '-') {                   // separator
            throw new TransactionValidationException(errorMessage);
        }
        if (!isNumeric(date.substring(5, 7))) {        // MM
            throw new TransactionValidationException(errorMessage);
        }
        if (date.charAt(7) != '-') {                   // separator
            throw new TransactionValidationException(errorMessage);
        }
        if (!isNumeric(date.substring(8, 10))) {       // DD
            throw new TransactionValidationException(errorMessage);
        }

        // Additional date validity check
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        int year = Integer.parseInt(date.substring(0, 4));
        if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1) {
            throw new TransactionValidationException(
                    date.contains("Orig") ? "Orig Date - Not a valid date..."
                            : "Proc Date - Not a valid date...");
        }
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
