package com.cardemo.batch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationResult model.
 */
class ValidationResultTest {

    @Test
    @DisplayName("Success result is valid with code 0")
    void success_isValid() {
        ValidationResult result = ValidationResult.success();
        assertTrue(result.isValid());
        assertEquals(0, result.getFailReasonCode());
        assertEquals("", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Failure result is invalid with given code and description")
    void failure_isInvalid() {
        ValidationResult result = ValidationResult.failure(102, "OVERLIMIT TRANSACTION");
        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.getFailReasonDescription());
    }
}
