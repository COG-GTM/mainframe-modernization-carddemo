package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.repository.DisclosureGroupRepository;
import com.cardemo.batch.service.InterestCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for interest calculation with real Spring context.
 * Verifies formula precision and disclosure group fallback with real database.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Interest Calculation Integration Tests")
class InterestCalculationIntegrationTest {

    @Autowired
    private InterestCalculationService interestService;
    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;

    @BeforeEach
    void setUp() {
        disclosureGroupRepository.deleteAll();
    }

    @Test
    @DisplayName("IT: Interest calculation with BigDecimal precision")
    void interestCalc_bigDecimalPrecision() {
        BigDecimal result = interestService.computeMonthlyInterest(
                new BigDecimal("5000.00"), new BigDecimal("18.99"));
        assertEquals(new BigDecimal("79.13"), result);
    }

    @Test
    @DisplayName("IT: Interest calculation for $0 balance")
    void interestCalc_zeroBalance() {
        BigDecimal result = interestService.computeMonthlyInterest(
                BigDecimal.ZERO, new BigDecimal("18.99"));
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("IT: Disclosure group lookup from database")
    void disclosureGroup_lookupFromDb() {
        DisclosureGroup group = new DisclosureGroup("GOLD", "01", "05", new BigDecimal("12.50"));
        disclosureGroupRepository.save(group);

        BigDecimal rate = interestService.getInterestRate("GOLD", "01", "05");
        assertEquals(new BigDecimal("12.50"), rate);
    }

    @Test
    @DisplayName("IT: DEFAULT fallback when specific group not in database")
    void disclosureGroup_defaultFallback() {
        DisclosureGroup defaultGroup = new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("18.00"));
        disclosureGroupRepository.save(defaultGroup);

        BigDecimal rate = interestService.getInterestRate("NONEXISTENT", "01", "05");
        assertEquals(new BigDecimal("18.00"), rate);
    }

    @Test
    @DisplayName("IT: Returns zero when no group found at all")
    void disclosureGroup_noneFound_returnsZero() {
        BigDecimal rate = interestService.getInterestRate("MISSING", "01", "05");
        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    @DisplayName("IT: Multiple disclosure groups in database")
    void disclosureGroup_multipleGroups() {
        disclosureGroupRepository.save(new DisclosureGroup("GOLD", "01", "05", new BigDecimal("12.00")));
        disclosureGroupRepository.save(new DisclosureGroup("SILVER", "01", "05", new BigDecimal("15.00")));
        disclosureGroupRepository.save(new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("18.00")));

        assertEquals(new BigDecimal("12.00"), interestService.getInterestRate("GOLD", "01", "05"));
        assertEquals(new BigDecimal("15.00"), interestService.getInterestRate("SILVER", "01", "05"));
        assertEquals(new BigDecimal("18.00"), interestService.getInterestRate("BRONZE", "01", "05")); // falls back to DEFAULT
    }

    @Test
    @DisplayName("IT: Interest precision for known COBOL test case $2500.50 at 15.75%")
    void interestCalc_knownCobolTestCase() {
        BigDecimal result = interestService.computeMonthlyInterest(
                new BigDecimal("2500.50"), new BigDecimal("15.75"));
        assertEquals(new BigDecimal("32.82"), result);
    }

    @Test
    @DisplayName("IT: Large balance interest calculation")
    void interestCalc_largeBalance() {
        BigDecimal result = interestService.computeMonthlyInterest(
                new BigDecimal("99999999.99"), new BigDecimal("0.01"));
        assertEquals(new BigDecimal("833.33"), result);
    }
}
