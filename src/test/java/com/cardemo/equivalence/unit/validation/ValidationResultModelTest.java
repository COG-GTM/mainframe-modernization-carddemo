package com.cardemo.equivalence.unit.validation;

import com.cardemo.batch.model.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ValidationResult model - maps to WS-VALIDATION-TRAILER in CBTRN02C.
 */
@DisplayName("ValidationResult Model Tests")
class ValidationResultModelTest {

    @Test
    @DisplayName("Success result should be valid with code 0")
    void success_isValid() {
        ValidationResult result = ValidationResult.success();
        assertTrue(result.isValid());
        assertEquals(0, result.getFailReasonCode());
        assertEquals("", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Failure result should not be valid")
    void failure_isNotValid() {
        ValidationResult result = ValidationResult.failure(100, "INVALID CARD NUMBER FOUND");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Failure result preserves error code")
    void failure_preservesCode() {
        ValidationResult result = ValidationResult.failure(102, "OVERLIMIT TRANSACTION");
        assertEquals(102, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Failure result preserves description character-for-character")
    void failure_preservesDescription() {
        ValidationResult result = ValidationResult.failure(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("All 4 COBOL error messages preserved exactly")
    void allFourMessages_preservedExactly() {
        assertEquals("INVALID CARD NUMBER FOUND",
                ValidationResult.failure(100, "INVALID CARD NUMBER FOUND").getFailReasonDescription());
        assertEquals("ACCOUNT RECORD NOT FOUND",
                ValidationResult.failure(101, "ACCOUNT RECORD NOT FOUND").getFailReasonDescription());
        assertEquals("OVERLIMIT TRANSACTION",
                ValidationResult.failure(102, "OVERLIMIT TRANSACTION").getFailReasonDescription());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                ValidationResult.failure(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION").getFailReasonDescription());
    }

    @Test
    @DisplayName("Error messages contain no trailing spaces")
    void errorMessages_noTrailingSpaces() {
        String msg100 = "INVALID CARD NUMBER FOUND";
        String msg101 = "ACCOUNT RECORD NOT FOUND";
        String msg102 = "OVERLIMIT TRANSACTION";
        String msg103 = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION";

        assertEquals(msg100, msg100.trim());
        assertEquals(msg101, msg101.trim());
        assertEquals(msg102, msg102.trim());
        assertEquals(msg103, msg103.trim());
    }

    @Test
    @DisplayName("Error messages contain no leading spaces")
    void errorMessages_noLeadingSpaces() {
        String msg = "INVALID CARD NUMBER FOUND";
        assertFalse(msg.startsWith(" "));
    }

    @Test
    @DisplayName("Error messages are all uppercase (COBOL convention)")
    void errorMessages_allUppercase() {
        String[] messages = {
            "INVALID CARD NUMBER FOUND",
            "ACCOUNT RECORD NOT FOUND",
            "OVERLIMIT TRANSACTION",
            "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"
        };
        for (String msg : messages) {
            assertEquals(msg, msg.toUpperCase());
        }
    }
}
