package com.cardemo.equivalence.unit.disclosure;

import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.DisclosureGroupRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import com.cardemo.batch.service.InterestCalculationService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for disclosure group DEFAULT fallback logic.
 * Business rule 6: When ACCT-GROUP-ID lookup fails (status '23'),
 * fall back to 'DEFAULT' group.
 * COBOL: IF WS-FILE-STATUS OF WS-FD-DISCGRP-STATUS = '23'
 *        MOVE 'DEFAULT' TO DIS-ACCT-GROUP-ID
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Disclosure Group DEFAULT Fallback Tests")
class DisclosureGroupFallbackTest {

    @Mock private DisclosureGroupRepository disclosureGroupRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private TransactionRecordRepository transactionRecordRepository;

    private InterestCalculationService service;

    @BeforeEach
    void setUp() {
        service = new InterestCalculationService(
                disclosureGroupRepository, accountRepository,
                cardXrefRepository, transactionRecordRepository,
                new SimpleMeterRegistry());
    }

    @Nested
    @DisplayName("Specific Group Found (Happy Path)")
    class SpecificGroupFound {

        @Test
        @DisplayName("Returns rate from specific group")
        void specificGroup_returnsRate() {
            DisclosureGroup group = new DisclosureGroup("GOLD", "01", "05", new BigDecimal("12.50"));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("GOLD", "01", "05"))
                    .thenReturn(Optional.of(group));

            BigDecimal rate = service.getInterestRate("GOLD", "01", "05");

            assertEquals(new BigDecimal("12.50"), rate);
        }

        @Test
        @DisplayName("Does not query DEFAULT when specific group exists")
        void specificGroup_noDefaultQuery() {
            DisclosureGroup group = new DisclosureGroup("SILVER", "01", "05", new BigDecimal("15.00"));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("SILVER", "01", "05"))
                    .thenReturn(Optional.of(group));

            service.getInterestRate("SILVER", "01", "05");

            verify(disclosureGroupRepository, never())
                    .findByAcctGroupIdAndTranTypeCdAndTranCatCd(eq("DEFAULT"), anyString(), anyString());
        }

        @Test
        @DisplayName("Handles multiple specific groups correctly")
        void multipleGroups() {
            DisclosureGroup gold = new DisclosureGroup("GOLD", "01", "05", new BigDecimal("10.00"));
            DisclosureGroup silver = new DisclosureGroup("SILVER", "01", "05", new BigDecimal("15.00"));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("GOLD", "01", "05"))
                    .thenReturn(Optional.of(gold));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("SILVER", "01", "05"))
                    .thenReturn(Optional.of(silver));

            assertEquals(new BigDecimal("10.00"), service.getInterestRate("GOLD", "01", "05"));
            assertEquals(new BigDecimal("15.00"), service.getInterestRate("SILVER", "01", "05"));
        }
    }

    @Nested
    @DisplayName("DEFAULT Fallback Path")
    class DefaultFallback {

        @Test
        @DisplayName("Falls back to DEFAULT when specific group not found")
        void fallback_whenNotFound() {
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("PREMIUM", "01", "05"))
                    .thenReturn(Optional.empty());
            DisclosureGroup defaultGroup = new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("18.99"));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                    .thenReturn(Optional.of(defaultGroup));

            BigDecimal rate = service.getInterestRate("PREMIUM", "01", "05");

            assertEquals(new BigDecimal("18.99"), rate);
        }

        @Test
        @DisplayName("DEFAULT fallback preserves type and category codes")
        void fallback_preservesTypeCat() {
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("MISSING", "02", "10"))
                    .thenReturn(Optional.empty());
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "02", "10"))
                    .thenReturn(Optional.empty());

            service.getInterestRate("MISSING", "02", "10");

            verify(disclosureGroupRepository).findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "02", "10");
        }

        @Test
        @DisplayName("DEFAULT group name is exactly 'DEFAULT' (uppercase)")
        void fallback_defaultIsUppercase() {
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("UNKNOWN", "01", "05"))
                    .thenReturn(Optional.empty());
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                    .thenReturn(Optional.empty());

            service.getInterestRate("UNKNOWN", "01", "05");

            // Verify it queries "DEFAULT" not "default" or "Default"
            verify(disclosureGroupRepository).findByAcctGroupIdAndTranTypeCdAndTranCatCd(
                    eq("DEFAULT"), anyString(), anyString());
        }

        @Test
        @DisplayName("Returns zero when neither specific nor DEFAULT found")
        void fallback_neitherFound_returnsZero() {
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("ORPHAN", "01", "05"))
                    .thenReturn(Optional.empty());
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                    .thenReturn(Optional.empty());

            BigDecimal rate = service.getInterestRate("ORPHAN", "01", "05");

            assertEquals(BigDecimal.ZERO, rate);
        }

        @Test
        @DisplayName("Exactly two repository queries when falling back to DEFAULT")
        void fallback_exactlyTwoQueries() {
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("MISSING2", "01", "05"))
                    .thenReturn(Optional.empty());
            DisclosureGroup defaultGroup = new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("20.00"));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                    .thenReturn(Optional.of(defaultGroup));

            service.getInterestRate("MISSING2", "01", "05");

            verify(disclosureGroupRepository, times(2))
                    .findByAcctGroupIdAndTranTypeCdAndTranCatCd(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Group ID 'DEFAULT' itself is not looked up twice")
        void defaultGroupId_notLookedUpTwice() {
            DisclosureGroup defaultGroup = new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("18.00"));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                    .thenReturn(Optional.of(defaultGroup));

            BigDecimal rate = service.getInterestRate("DEFAULT", "01", "05");

            assertEquals(new BigDecimal("18.00"), rate);
            verify(disclosureGroupRepository, times(1))
                    .findByAcctGroupIdAndTranTypeCdAndTranCatCd(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Different DEFAULT rates for different type/cat combinations")
        void defaultRates_differByTypeCat() {
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("X", "01", "05"))
                    .thenReturn(Optional.empty());
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("X", "02", "10"))
                    .thenReturn(Optional.empty());

            DisclosureGroup default1 = new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("15.00"));
            DisclosureGroup default2 = new DisclosureGroup("DEFAULT", "02", "10", new BigDecimal("22.00"));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                    .thenReturn(Optional.of(default1));
            when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "02", "10"))
                    .thenReturn(Optional.of(default2));

            assertEquals(new BigDecimal("15.00"), service.getInterestRate("X", "01", "05"));
            assertEquals(new BigDecimal("22.00"), service.getInterestRate("X", "02", "10"));
        }
    }
}
