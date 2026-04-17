package com.carddemo.account.service;

import com.carddemo.account.dto.AccountRequest;
import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.dto.BalanceUpdateRequest;
import com.carddemo.account.entity.Account;
import com.carddemo.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId("00000000001");
        testAccount.setAcctActiveStatus("Y");
        testAccount.setAcctCurrBal(new BigDecimal("1500.00"));
        testAccount.setAcctCreditLimit(new BigDecimal("5000.00"));
        testAccount.setAcctCashCreditLimit(new BigDecimal("1500.00"));
        testAccount.setAcctOpenDate("2020-01-15");
        testAccount.setAcctExpirationDate("2025-01-15");
        testAccount.setAcctReissueDate("2023-01-15");
        testAccount.setAcctCurrCycCredit(new BigDecimal("200.00"));
        testAccount.setAcctCurrCycDebit(new BigDecimal("350.00"));
        testAccount.setAcctAddrZip("60601");
        testAccount.setAcctGroupId("GROUP01");
    }

    @Test
    void getAccount_found() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));

        Optional<AccountResponse> result = accountService.getAccount("00000000001");

        assertTrue(result.isPresent());
        AccountResponse response = result.get();
        assertEquals("00000000001", response.getAcctId());
        assertEquals("Y", response.getAcctActiveStatus());
        assertEquals(new BigDecimal("1500.00"), response.getAcctCurrBal());
        assertEquals(new BigDecimal("5000.00"), response.getAcctCreditLimit());
        assertEquals("60601", response.getAcctAddrZip());
        assertEquals("GROUP01", response.getAcctGroupId());
        verify(accountRepository).findById("00000000001");
    }

    @Test
    void getAccount_notFound() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        Optional<AccountResponse> result = accountService.getAccount("99999999999");

        assertFalse(result.isPresent());
        verify(accountRepository).findById("99999999999");
    }

    @Test
    void updateAccount_success() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountRequest request = new AccountRequest();
        request.setAcctActiveStatus("N");
        request.setAcctCreditLimit(new BigDecimal("7500.00"));
        request.setAcctCashCreditLimit(new BigDecimal("2000.00"));
        request.setAcctGroupId("GROUP02");
        request.setAcctAddrZip("10001");

        AccountResponse response = accountService.updateAccount("00000000001", request);

        assertEquals("N", response.getAcctActiveStatus());
        assertEquals(new BigDecimal("7500.00"), response.getAcctCreditLimit());
        assertEquals(new BigDecimal("2000.00"), response.getAcctCashCreditLimit());
        assertEquals("GROUP02", response.getAcctGroupId());
        assertEquals("10001", response.getAcctAddrZip());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_invalidStatus() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));

        AccountRequest request = new AccountRequest();
        request.setAcctActiveStatus("X");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateAccount("00000000001", request));
    }

    @Test
    void updateAccount_notFound() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        AccountRequest request = new AccountRequest();
        request.setAcctActiveStatus("Y");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateAccount("99999999999", request));
    }

    @Test
    void updateBalance_credit() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceUpdateRequest request = new BalanceUpdateRequest(new BigDecimal("500.00"));

        AccountResponse response = accountService.updateBalance("00000000001", request);

        // 1500 + 500 = 2000
        assertEquals(new BigDecimal("2000.00"), response.getAcctCurrBal());
        // 200 + 500 = 700 (credit cycle)
        assertEquals(new BigDecimal("700.00"), response.getAcctCurrCycCredit());
        // Debit unchanged
        assertEquals(new BigDecimal("350.00"), response.getAcctCurrCycDebit());
    }

    @Test
    void updateBalance_debit() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceUpdateRequest request = new BalanceUpdateRequest(new BigDecimal("-200.00"));

        AccountResponse response = accountService.updateBalance("00000000001", request);

        // 1500 + (-200) = 1300
        assertEquals(new BigDecimal("1300.00"), response.getAcctCurrBal());
        // Credit unchanged
        assertEquals(new BigDecimal("200.00"), response.getAcctCurrCycCredit());
        // 350 + 200 = 550 (debit cycle)
        assertEquals(new BigDecimal("550.00"), response.getAcctCurrCycDebit());
    }

    @Test
    void updateBalance_notFound() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        BalanceUpdateRequest request = new BalanceUpdateRequest(new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateBalance("99999999999", request));
    }
}
