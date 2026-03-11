package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock private AccountRepository accountRepository;
    @InjectMocks private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(10000000001L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrBal(new BigDecimal("1500.00"));
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testAccount.setCashCreditLimit(new BigDecimal("2000.00"));
        testAccount.setOpenDate("2020-01-15");
        testAccount.setGroupId("GROUP001");
    }

    @Test
    void getAccount_success() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        Account result = accountService.getAccount(10000000001L);
        assertEquals(10000000001L, result.getAcctId());
        assertEquals("Y", result.getActiveStatus());
        verify(accountRepository).findById(10000000001L);
    }

    @Test
    void getAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccount(999L));
    }

    @Test
    void getAllAccounts() {
        when(accountRepository.findAll()).thenReturn(List.of(testAccount));
        List<Account> result = accountService.getAllAccounts();
        assertEquals(1, result.size());
    }

    @Test
    void updateAccount_success() {
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        Account update = new Account();
        update.setActiveStatus("N");
        update.setCreditLimit(new BigDecimal("15000.00"));
        Account result = accountService.updateAccount(10000000001L, update);
        assertNotNull(result);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        Account update = new Account();
        assertThrows(ResourceNotFoundException.class, () -> accountService.updateAccount(999L, update));
    }
}
