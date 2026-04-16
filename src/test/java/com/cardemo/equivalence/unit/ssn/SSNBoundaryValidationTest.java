package com.cardemo.equivalence.unit.ssn;

import com.cardemo.batch.service.SsnValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SSN boundary validation per COBOL business rules.
 * Business rule 4: Reject SSNs starting with 000, 666, or 900-999.
 * Also reject group=00 and serial=0000.
 */
@DisplayName("SSN Boundary Validation Tests")
class SSNBoundaryValidationTest {

    private SsnValidationService ssnService;

    @BeforeEach
    void setUp() {
        ssnService = new SsnValidationService();
    }

    @Nested
    @DisplayName("Area Number 000 Rejection")
    class AreaZero {

        @Test
        @DisplayName("Should reject SSN starting with 000")
        void reject_000() {
            assertFalse(ssnService.isValid("000-12-3456"));
        }

        @Test
        @DisplayName("Should reject 000 without dashes")
        void reject_000_noDashes() {
            assertFalse(ssnService.isValid("000123456"));
        }

        @Test
        @DisplayName("Should accept SSN starting with 001 (boundary)")
        void accept_001() {
            assertTrue(ssnService.isValid("001-01-0001"));
        }
    }

    @Nested
    @DisplayName("Area Number 666 Rejection")
    class AreaSixSixSix {

        @Test
        @DisplayName("Should reject SSN starting with 666")
        void reject_666() {
            assertFalse(ssnService.isValid("666-12-3456"));
        }

        @Test
        @DisplayName("Should reject 666 without dashes")
        void reject_666_noDashes() {
            assertFalse(ssnService.isValid("666123456"));
        }

        @Test
        @DisplayName("Should accept SSN starting with 665 (just below)")
        void accept_665() {
            assertTrue(ssnService.isValid("665-01-0001"));
        }

        @Test
        @DisplayName("Should accept SSN starting with 667 (just above)")
        void accept_667() {
            assertTrue(ssnService.isValid("667-01-0001"));
        }
    }

    @Nested
    @DisplayName("Area Number 900-999 Rejection")
    class Area900To999 {

        @Test
        @DisplayName("Should reject SSN starting with 900 (boundary)")
        void reject_900() {
            assertFalse(ssnService.isValid("900-12-3456"));
        }

        @Test
        @DisplayName("Should reject SSN starting with 999")
        void reject_999() {
            assertFalse(ssnService.isValid("999-12-3456"));
        }

        @Test
        @DisplayName("Should reject SSN starting with 950 (mid-range)")
        void reject_950() {
            assertFalse(ssnService.isValid("950-12-3456"));
        }

        @Test
        @DisplayName("Should accept SSN starting with 899 (just below boundary)")
        void accept_899() {
            assertTrue(ssnService.isValid("899-01-0001"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"900-11-1111", "901-22-2222", "950-33-3333", "975-44-4444", "999-55-5555"})
        @DisplayName("Should reject all SSNs in 900-999 range")
        void reject_entire900Range(String ssn) {
            assertFalse(ssnService.isValid(ssn));
        }
    }

    @Nested
    @DisplayName("Group Number 00 Rejection")
    class GroupZero {

        @Test
        @DisplayName("Should reject SSN with group 00")
        void reject_group00() {
            assertFalse(ssnService.isValid("123-00-4567"));
        }

        @Test
        @DisplayName("Should accept SSN with group 01")
        void accept_group01() {
            assertTrue(ssnService.isValid("123-01-4567"));
        }
    }

    @Nested
    @DisplayName("Serial Number 0000 Rejection")
    class SerialZero {

        @Test
        @DisplayName("Should reject SSN with serial 0000")
        void reject_serial0000() {
            assertFalse(ssnService.isValid("123-45-0000"));
        }

        @Test
        @DisplayName("Should accept SSN with serial 0001")
        void accept_serial0001() {
            assertTrue(ssnService.isValid("123-45-0001"));
        }
    }

    @Nested
    @DisplayName("Valid SSN Acceptance")
    class ValidSSNs {

        @ParameterizedTest
        @ValueSource(strings = {
            "001-01-0001", "123-45-6789", "499-99-9999",
            "665-01-0001", "667-01-0001", "899-99-9999"
        })
        @DisplayName("Should accept valid SSNs across ranges")
        void accept_validSSNs(String ssn) {
            assertTrue(ssnService.isValid(ssn));
        }

        @Test
        @DisplayName("Should accept valid SSN without dashes")
        void accept_noDashes() {
            assertTrue(ssnService.isValid("123456789"));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Invalid Formats")
    class EdgeCases {

        @Test
        @DisplayName("Should reject null SSN")
        void reject_null() {
            assertFalse(ssnService.isValid(null));
        }

        @Test
        @DisplayName("Should reject empty SSN")
        void reject_empty() {
            assertFalse(ssnService.isValid(""));
        }

        @Test
        @DisplayName("Should reject SSN with letters")
        void reject_letters() {
            assertFalse(ssnService.isValid("ABC-DE-FGHI"));
        }

        @Test
        @DisplayName("Should reject SSN with too few digits")
        void reject_tooFewDigits() {
            assertFalse(ssnService.isValid("12-34-567"));
        }

        @Test
        @DisplayName("Should reject SSN with too many digits")
        void reject_tooManyDigits() {
            assertFalse(ssnService.isValid("1234-56-78901"));
        }

        @Test
        @DisplayName("Should reject blank SSN")
        void reject_blank() {
            assertFalse(ssnService.isValid("   "));
        }
    }
}
