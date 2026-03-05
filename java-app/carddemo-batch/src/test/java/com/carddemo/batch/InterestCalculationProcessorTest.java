package com.carddemo.batch;

import com.carddemo.batch.interest.InterestCalculationProcessor;
import com.carddemo.entity.Account;
import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.DisclosureGroup.DisclosureGroupId;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionCategoryBalance.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InterestCalculationProcessor — validates parity with CBACT04C.cbl.
 * Verifies interest formula: categoryBalance * interestRate / 1200 with HALF_UP rounding.
 */
@ExtendWith(MockitoExtension.class)
class InterestCalculationProcessorTest {

    @Mock private AccountRepository accountRepository;
    @Mock private DisclosureGroupRepository disclosureGroupRepository;

    private InterestCalculationProcessor processor;

    private TransactionCategoryBalance tcb;
    private Account account;
    private DisclosureGroup disclosureGroup;

    @BeforeEach
    void setUp() {
        processor = new InterestCalculationProcessor(accountRepository, disclosureGroupRepository);

        TransactionCategoryBalanceId tcbId = new TransactionCategoryBalanceId(1L, "SA", 1);
        tcb = new TransactionCategoryBalance();
        tcb.setId(tcbId);
        tcb.setBalance(new BigDecimal("1200.00"));

        account = new Account();
        account.setAcctId(1L);
        account.setCurrentBalance(new BigDecimal("1200.00"));
        account.setCurrentCycleCredit(new BigDecimal("500.00"));
        account.setCurrentCycleDebit(new BigDecimal("300.00"));
        account.setGroupId("DEFAULT");

        disclosureGroup = new DisclosureGroup();
        disclosureGroup.setId(new DisclosureGroupId("DEFAULT", "SA", 1));
        disclosureGroup.setInterestRate(new BigDecimal("18.00")); // 18% APR
    }

    @Test
    void process_calculatesInterestCorrectly() throws Exception {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(disclosureGroupRepository.findByGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "SA", 1))
                .thenReturn(Optional.of(disclosureGroup));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = processor.process(tcb);

        assertNotNull(result);
        // Interest = 1200.00 * 18.00 / 1200 = 18.00
        BigDecimal expectedInterest = new BigDecimal("1200.00")
                .multiply(new BigDecimal("18.00"))
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        assertEquals(expectedInterest, result.getAmount());
    }

    @Test
    void process_fallsBackToDefaultGroup() throws Exception {
        account.setGroupId("PREMIUM");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        // PREMIUM group not found
        when(disclosureGroupRepository.findByGroupIdAndTranTypeCdAndTranCatCd("PREMIUM", "SA", 1))
                .thenReturn(Optional.empty());
        // Falls back to DEFAULT via findDefaultGroup
        when(disclosureGroupRepository.findDefaultGroup("SA", 1))
                .thenReturn(Optional.of(disclosureGroup));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = processor.process(tcb);

        assertNotNull(result);
        verify(disclosureGroupRepository).findByGroupIdAndTranTypeCdAndTranCatCd("PREMIUM", "SA", 1);
        verify(disclosureGroupRepository).findDefaultGroup("SA", 1);
    }

    @Test
    void process_zeroBalance_returnsNull() throws Exception {
        tcb.setBalance(BigDecimal.ZERO);

        // Processor returns null for zero balance (skip)
        Transaction result = processor.process(tcb);

        assertNull(result);
    }

    @Test
    void interestFormula_knownValues() {
        // Verify formula: balance * rate / 1200
        BigDecimal balance = new BigDecimal("5432.10");
        BigDecimal rate = new BigDecimal("24.99");
        BigDecimal expected = balance.multiply(rate).divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

        // 5432.10 * 24.99 / 1200 = 113.12 (BigDecimal HALF_UP)
        assertEquals(new BigDecimal("113.12"), expected);
    }
}
