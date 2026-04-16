package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.service.DateValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for date validation with real Spring context.
 * Tests leap year handling and date boundary validation.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@DisplayName("Date Validation Integration Tests")
class DateValidationIntegrationTest {

    @Autowired
    private DateValidationService dateService;

    @Test
    @DisplayName("IT: Service is autowired from Spring context")
    void serviceIsAutowired() {
        assertNotNull(dateService);
    }

    @Test
    @DisplayName("IT: 2024-02-29 is valid (leap year)")
    void leapYear2024_valid() {
        assertTrue(dateService.isValidDate(2024, 2, 29));
    }

    @Test
    @DisplayName("IT: 2023-02-29 is invalid (not leap year)")
    void nonLeapYear2023_invalid() {
        assertFalse(dateService.isValidDate(2023, 2, 29));
    }

    @Test
    @DisplayName("IT: 2000-02-29 is valid (divisible by 400)")
    void centuryLeapYear2000_valid() {
        assertTrue(dateService.isValidDate(2000, 2, 29));
    }

    @Test
    @DisplayName("IT: 1900-02-29 is invalid (divisible by 100 but not 400)")
    void centuryNonLeapYear1900_invalid() {
        assertFalse(dateService.isValidDate(1900, 2, 29));
    }

    @Test
    @DisplayName("IT: Valid date string parsing")
    void validDateString() {
        assertTrue(dateService.isValidDateString("2024-02-29"));
        assertFalse(dateService.isValidDateString("2023-02-29"));
    }

    @Test
    @DisplayName("IT: Date of birth validation")
    void dateOfBirth_validation() {
        assertTrue(dateService.isValidDateOfBirth(1990, 5, 15, 2024, 1, 15));
        assertFalse(dateService.isValidDateOfBirth(2025, 1, 1, 2024, 1, 15));
    }

    @Test
    @DisplayName("IT: Month boundary validation")
    void monthBoundary() {
        assertTrue(dateService.isValidDate(2024, 1, 31));
        assertFalse(dateService.isValidDate(2024, 4, 31));
        assertTrue(dateService.isValidDate(2024, 4, 30));
    }
}
