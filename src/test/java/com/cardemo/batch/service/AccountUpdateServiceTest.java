package com.cardemo.batch.service;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.DailyTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountUpdateService.
 * Tests credit/debit separation logic from CBTRN02C paragraph 2800-UPDATE-ACCOUNT-REC.
 */
class AccountUpdateServiceTest {

    private AccountUpdateService accountUpdateService;

    @BeforeEach
    void setUp() {
        accountUpdateService = new AccountUpdateService();
    }

    private Account createAccount(BigDecimal currentBal, BigDecimal cycCredit, BigDecimal cycDebit) {
        Account acct = new Account();
        acct.setAcctId(12345678901L);
        acct.setCurrentBalance(currentBal);
        acct.setCurrentCycleCredit(cycCredit);
        acct.setCurrentCycleDebit(cycDebit);
        return acct;
    }

    private DailyTransaction createDailyTransaction(BigDecimal amount) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TRAN0000000001");
        dt.setAmount(amount);
        return dt;
    }

    @Test
    @DisplayName("Positive amount adds to ACCT-CURR-CYC-CREDIT (credit path)")
    void updateAccountBalances_positiveAmount_addsToCredit() {
        Account acct = createAccount(new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("200.00"));
        DailyTransaction dt = createDailyTransaction(new BigDecimal("150.00"));

        accountUpdateService.updateAccountBalances(acct, dt);

        assertEquals(new BigDecimal("650.00"), acct.getCurrentCycleCredit());
        assertEquals(new BigDecimal("200.00"), acct.getCurrentCycleDebit()); // unchanged
        assertEquals(new BigDecimal("1150.00"), acct.getCurrentBalance());
    }

    @Test
    @DisplayName("Negative amount adds to ACCT-CURR-CYC-DEBIT (debit path)")
    void updateAccountBalances_negativeAmount_addsToDebit() {
        Account acct = createAccount(new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("200.00"));
        DailyTransaction dt = createDailyTransaction(new BigDecimal("-75.00"));

        accountUpdateService.updateAccountBalances(acct, dt);

        assertEquals(new BigDecimal("500.00"), acct.getCurrentCycleCredit()); // unchanged
        assertEquals(new BigDecimal("125.00"), acct.getCurrentCycleDebit()); // 200 + (-75) = 125
        assertEquals(new BigDecimal("925.00"), acct.getCurrentBalance());
    }

    @Test
    @DisplayName("Zero amount adds to ACCT-CURR-CYC-CREDIT (>= 0 path)")
    void updateAccountBalances_zeroAmount_addsToCredit() {
        Account acct = createAccount(new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("200.00"));
        DailyTransaction dt = createDailyTransaction(BigDecimal.ZERO);

        accountUpdateService.updateAccountBalances(acct, dt);

        assertEquals(new BigDecimal("500.00"), acct.getCurrentCycleCredit());
        assertEquals(new BigDecimal("200.00"), acct.getCurrentCycleDebit());
        assertEquals(new BigDecimal("1000.00"), acct.getCurrentBalance());
    }

    @Test
    @DisplayName("Current balance always updated regardless of credit/debit path")
    void updateAccountBalances_alwaysUpdatesCurrentBalance() {
        Account acct = createAccount(new BigDecimal("5000.00"), new BigDecimal("0.00"), new BigDecimal("0.00"));
        DailyTransaction dt = createDailyTransaction(new BigDecimal("-250.50"));

        accountUpdateService.updateAccountBalances(acct, dt);

        assertEquals(new BigDecimal("4749.50"), acct.getCurrentBalance());
    }

    @Test
    @DisplayName("Large positive amount correctly updates credit and balance")
    void updateAccountBalances_largePositiveAmount() {
        Account acct = createAccount(new BigDecimal("999999999.99"), new BigDecimal("0.00"), new BigDecimal("0.00"));
        DailyTransaction dt = createDailyTransaction(new BigDecimal("0.01"));

        accountUpdateService.updateAccountBalances(acct, dt);

        assertEquals(new BigDecimal("1000000000.00"), acct.getCurrentBalance());
        assertEquals(new BigDecimal("0.01"), acct.getCurrentCycleCredit());
    }
}
