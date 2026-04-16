package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.service.SsnValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SSN validation with real Spring context.
 * Verifies the service is properly wired and validates SSNs correctly.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@DisplayName("SSN Validation Integration Tests")
class SSNValidationIntegrationTest {

    @Autowired
    private SsnValidationService ssnService;

    @Test
    @DisplayName("IT: Service is autowired from Spring context")
    void serviceIsAutowired() {
        assertNotNull(ssnService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"000-12-3456", "666-12-3456", "900-12-3456", "999-12-3456"})
    @DisplayName("IT: Rejects invalid area numbers from Spring-managed service")
    void rejectsInvalidAreas(String ssn) {
        assertFalse(ssnService.isValid(ssn));
    }

    @ParameterizedTest
    @ValueSource(strings = {"001-01-0001", "123-45-6789", "665-01-0001", "899-99-9999"})
    @DisplayName("IT: Accepts valid SSNs from Spring-managed service")
    void acceptsValidSSNs(String ssn) {
        assertTrue(ssnService.isValid(ssn));
    }

    @Test
    @DisplayName("IT: Boundary value 899 is valid (just below 900)")
    void boundary899_valid() {
        assertTrue(ssnService.isValid("899-01-0001"));
    }

    @Test
    @DisplayName("IT: Boundary value 900 is invalid")
    void boundary900_invalid() {
        assertFalse(ssnService.isValid("900-01-0001"));
    }

    @Test
    @DisplayName("IT: Boundary value 667 is valid (just above 666)")
    void boundary667_valid() {
        assertTrue(ssnService.isValid("667-01-0001"));
    }

    @Test
    @DisplayName("IT: Group 00 rejected")
    void group00_rejected() {
        assertFalse(ssnService.isValid("123-00-4567"));
    }

    @Test
    @DisplayName("IT: Serial 0000 rejected")
    void serial0000_rejected() {
        assertFalse(ssnService.isValid("123-45-0000"));
    }
}
