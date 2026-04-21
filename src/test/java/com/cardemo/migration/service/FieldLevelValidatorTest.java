package com.cardemo.migration.service;

import com.cardemo.migration.model.CopybookField;
import com.cardemo.migration.model.CopybookLayout;
import com.cardemo.migration.model.ValidationResult;
import com.cardemo.migration.reader.CopybookLayouts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FieldLevelValidator - validates field-level spot checks
 * and value comparison logic.
 */
class FieldLevelValidatorTest {

    @Test
    @DisplayName("Numeric values match with different formatting")
    void numericValuesMatch() {
        CopybookField field = CopybookField.numericDisplay("ACCT-ID", 0, 11, "acct_id");
        assertTrue(FieldLevelValidator.valuesMatch("00000012345", "12345", field));
    }

    @Test
    @DisplayName("Numeric values with decimal match")
    void numericDecimalMatch() {
        CopybookField field = CopybookField.signedNumericDisplay("AMOUNT", 0, 12, 2, "amount");
        assertTrue(FieldLevelValidator.valuesMatch("1234.56", "1234.56", field));
    }

    @Test
    @DisplayName("Numeric values do not match")
    void numericValuesMismatch() {
        CopybookField field = CopybookField.numericDisplay("ACCT-ID", 0, 11, "acct_id");
        assertFalse(FieldLevelValidator.valuesMatch("12345", "12346", field));
    }

    @Test
    @DisplayName("Alphanumeric values match after trimming")
    void alphanumericValuesMatch() {
        CopybookField field = CopybookField.alphanumeric("NAME", 0, 25, "name");
        assertTrue(FieldLevelValidator.valuesMatch("JOHN DOE", "JOHN DOE", field));
    }

    @Test
    @DisplayName("Alphanumeric values with leading/trailing whitespace match")
    void alphanumericWhitespaceMatch() {
        CopybookField field = CopybookField.alphanumeric("NAME", 0, 25, "name");
        assertTrue(FieldLevelValidator.valuesMatch("  JOHN DOE  ", "JOHN DOE", field));
    }

    @Test
    @DisplayName("Null value comparison handles null ebcdic")
    void nullEbcdicValue() {
        CopybookField field = CopybookField.alphanumeric("NAME", 0, 25, "name");
        assertFalse(FieldLevelValidator.valuesMatch(null, "value", field));
    }

    @Test
    @DisplayName("Null value comparison handles null db value")
    void nullDbValue() {
        CopybookField field = CopybookField.alphanumeric("NAME", 0, 25, "name");
        assertFalse(FieldLevelValidator.valuesMatch("value", null, field));
    }

    @Test
    @DisplayName("Both null values match")
    void bothNullMatch() {
        CopybookField field = CopybookField.alphanumeric("NAME", 0, 25, "name");
        assertTrue(FieldLevelValidator.valuesMatch(null, null, field));
    }

    @Test
    @DisplayName("Validate parsed record with matching fields passes")
    void validateParsedRecordMatching() {
        FieldLevelValidator validator = new FieldLevelValidator(null, null, 3);
        CopybookLayout layout = CopybookLayouts.CVACT03Y;

        Map<String, String> parsedRecord = Map.of(
                "XREF-CARD-NUM", "4000123456789012",
                "XREF-CUST-ID", "000000001",
                "XREF-ACCT-ID", "00000000001"
        );
        Map<String, String> expectedValues = Map.of(
                "card_num", "4000123456789012",
                "cust_id", "1",
                "acct_id", "1"
        );

        ValidationResult result = validator.validateParsedRecord(parsedRecord, expectedValues, layout);
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Validate parsed record with mismatching field fails")
    void validateParsedRecordMismatch() {
        FieldLevelValidator validator = new FieldLevelValidator(null, null, 3);
        CopybookLayout layout = CopybookLayouts.CVACT03Y;

        Map<String, String> parsedRecord = Map.of(
                "XREF-CARD-NUM", "4000123456789012",
                "XREF-CUST-ID", "000000001",
                "XREF-ACCT-ID", "00000000001"
        );
        Map<String, String> expectedValues = Map.of(
                "card_num", "DIFFERENT_CARD",
                "cust_id", "1",
                "acct_id", "1"
        );

        ValidationResult result = validator.validateParsedRecord(parsedRecord, expectedValues, layout);
        assertFalse(result.isPassed());
    }
}
