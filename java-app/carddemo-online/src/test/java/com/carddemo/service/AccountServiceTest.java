package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService — validates parity with COACTVWC and COACTUPC.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(1L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrentBalance(new BigDecimal("1500.00"));
        testAccount.setCreditLimit(new BigDecimal("5000.00"));
        testAccount.setCashCreditLimit(new BigDecimal("1000.00"));
        testAccount.setOpenDate(LocalDate.of(2020, 1, 15));
        testAccount.setExpirationDate(LocalDate.of(2025, 12, 31));
        testAccount.setCurrentCycleCredit(new BigDecimal("200.00"));
        testAccount.setCurrentCycleDebit(new BigDecimal("100.00"));
        testAccount.setGroupId("DEFAULT");
    }

    @Test
    void getAccount_found() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findByAcctId(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> result = accountService.getAccount(1L);

        assertNotNull(result);
        Account acct = (Account) result.get("account");
        assertEquals(1L, acct.getAcctId());
        assertEquals(new BigDecimal("1500.00"), acct.getCurrentBalance());
        assertEquals(new BigDecimal("5000.00"), acct.getCreditLimit());
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.getAccount(999L));
    }
}
