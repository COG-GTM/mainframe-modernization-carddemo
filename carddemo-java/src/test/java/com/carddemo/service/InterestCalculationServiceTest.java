package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestCalculationServiceTest {
    @Mock private TransactionCategoryBalanceRepository tcbRepository;
    @Mock private DisclosureGroupRepository disclosureGroupRepository;
    @Mock private AccountRepository accountRepository;
    @InjectMocks private InterestCalculationService service;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(10000000001L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrBal(new BigDecimal("1500.00"));
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testAccount.setGroupId("GROUP001");
    }

    @Test
    void calculateInterest_withBalance() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        TransactionCategoryBalance tcb = new TransactionCategoryBalance();
        tcb.setAcctId(10000000001L);
        tcb.setTranTypeCd("PR");
        tcb.setTranCatCd(5000);
        tcb.setTranCatBal(new BigDecimal("1000.00"));
        when(tcbRepository.findByAcctId(10000000001L)).thenReturn(List.of(tcb));
        DisclosureGroup dg = new DisclosureGroup();
        dg.setIntRate(new BigDecimal("18.99"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("GROUP001", "PR", 5000))
            .thenReturn(Optional.of(dg));

        BigDecimal interest = service.calculateInterestForAccount(10000000001L);
        assertTrue(interest.compareTo(BigDecimal.ZERO) > 0);
        // Monthly rate = 18.99/12/100 = 0.015825, interest = 1000 * 0.015825 = 15.83
        assertEquals(new BigDecimal("15.83"), interest);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void calculateInterest_noBalance() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        when(tcbRepository.findByAcctId(10000000001L)).thenReturn(Collections.emptyList());
        BigDecimal interest = service.calculateInterestForAccount(10000000001L);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateInterest_accountNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        BigDecimal interest = service.calculateInterestForAccount(999L);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateInterest_zeroBalance() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        TransactionCategoryBalance tcb = new TransactionCategoryBalance();
        tcb.setAcctId(10000000001L);
        tcb.setTranTypeCd("PR");
        tcb.setTranCatCd(5000);
        tcb.setTranCatBal(BigDecimal.ZERO);
        when(tcbRepository.findByAcctId(10000000001L)).thenReturn(List.of(tcb));
        BigDecimal interest = service.calculateInterestForAccount(10000000001L);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateInterest_noDisclosureGroup() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        TransactionCategoryBalance tcb = new TransactionCategoryBalance();
        tcb.setAcctId(10000000001L);
        tcb.setTranTypeCd("PR");
        tcb.setTranCatCd(5000);
        tcb.setTranCatBal(new BigDecimal("1000.00"));
        when(tcbRepository.findByAcctId(10000000001L)).thenReturn(List.of(tcb));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("GROUP001", "PR", 5000))
            .thenReturn(Optional.empty());
        BigDecimal interest = service.calculateInterestForAccount(10000000001L);
        assertEquals(BigDecimal.ZERO, interest);
    }
}
