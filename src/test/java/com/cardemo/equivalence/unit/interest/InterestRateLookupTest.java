package com.cardemo.equivalence.unit.interest;

import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.DisclosureGroupRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import com.cardemo.batch.service.InterestCalculationService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for interest rate lookup including DEFAULT group fallback.
 * Business rule 6: When ACCT-GROUP-ID lookup fails (status '23'),
 * fall back to 'DEFAULT' group.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Interest Rate Lookup Tests")
class InterestRateLookupTest {

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

    @Test
    @DisplayName("Should return rate from specific group when found")
    void getInterestRate_specificGroupFound() {
        DisclosureGroup group = new DisclosureGroup("PREMIUM", "01", "05", new BigDecimal("15.99"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("PREMIUM", "01", "05"))
                .thenReturn(Optional.of(group));

        BigDecimal rate = service.getInterestRate("PREMIUM", "01", "05");

        assertEquals(new BigDecimal("15.99"), rate);
    }

    @Test
    @DisplayName("Should fall back to DEFAULT group when specific group not found")
    void getInterestRate_fallbackToDefault() {
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("UNKNOWN", "01", "05"))
                .thenReturn(Optional.empty());
        DisclosureGroup defaultGroup = new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("18.00"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                .thenReturn(Optional.of(defaultGroup));

        BigDecimal rate = service.getInterestRate("UNKNOWN", "01", "05");

        assertEquals(new BigDecimal("18.00"), rate);
    }

    @Test
    @DisplayName("Should return zero when neither specific nor DEFAULT group found")
    void getInterestRate_neitherFound_returnsZero() {
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("MISSING", "01", "05"))
                .thenReturn(Optional.empty());
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                .thenReturn(Optional.empty());

        BigDecimal rate = service.getInterestRate("MISSING", "01", "05");

        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    @DisplayName("Should not query DEFAULT when specific group found")
    void getInterestRate_specificFound_noDefaultQuery() {
        DisclosureGroup group = new DisclosureGroup("GOLD", "01", "05", new BigDecimal("12.00"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("GOLD", "01", "05"))
                .thenReturn(Optional.of(group));

        service.getInterestRate("GOLD", "01", "05");

        verify(disclosureGroupRepository, times(1))
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("DEFAULT fallback uses same type and category codes")
    void getInterestRate_defaultUsesSameTypeCat() {
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("CUSTOM", "02", "10"))
                .thenReturn(Optional.empty());
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "02", "10"))
                .thenReturn(Optional.empty());

        service.getInterestRate("CUSTOM", "02", "10");

        verify(disclosureGroupRepository).findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "02", "10");
    }

    @Test
    @DisplayName("Empty string group ID triggers DEFAULT fallback")
    void getInterestRate_emptyGroupId_fallback() {
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("", "01", "05"))
                .thenReturn(Optional.empty());
        DisclosureGroup defaultGroup = new DisclosureGroup("DEFAULT", "01", "05", new BigDecimal("20.00"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "05"))
                .thenReturn(Optional.of(defaultGroup));

        BigDecimal rate = service.getInterestRate("", "01", "05");

        assertEquals(new BigDecimal("20.00"), rate);
    }

    @Test
    @DisplayName("Different type/cat codes produce different rates")
    void getInterestRate_differentTypeCat() {
        DisclosureGroup group1 = new DisclosureGroup("STD", "01", "05", new BigDecimal("12.00"));
        DisclosureGroup group2 = new DisclosureGroup("STD", "02", "10", new BigDecimal("24.00"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("STD", "01", "05"))
                .thenReturn(Optional.of(group1));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("STD", "02", "10"))
                .thenReturn(Optional.of(group2));

        assertEquals(new BigDecimal("12.00"), service.getInterestRate("STD", "01", "05"));
        assertEquals(new BigDecimal("24.00"), service.getInterestRate("STD", "02", "10"));
    }
}
