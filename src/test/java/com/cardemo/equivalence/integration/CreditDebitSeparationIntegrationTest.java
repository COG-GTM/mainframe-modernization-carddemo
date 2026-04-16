package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.service.AccountUpdateService;
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
 * Integration tests for credit/debit separation with real Spring context.
 * Business rule 8: AMT >= 0 → credit, AMT < 0 → debit, always update balance.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Credit/Debit Separation Integration Tests")
class CreditDebitSeparationIntegrationTest {

    @Autowired
    private AccountUpdateService accountUpdateService;
    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        testAccount = new Account();
        testAccount.setAcctId(40001L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrentBalance(new BigDecimal("1000.00"));
        testAccount.setCreditLimit(new BigDecimal("5000.00"));
        testAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        testAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        testAccount.setExpirationDate("2025-12-31");
        testAccount.setGroupId("STANDARD");
        accountRepository.save(testAccount);
    }

    private DailyTransaction createTxn(BigDecimal amount) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("IT_CD");
        dt.setAmount(amount);
        return dt;
    }

    @Test
    @DisplayName("IT: Positive amount updates cycle credit and balance")
    void positiveAmount_updatesCreditAndBalance() {
        accountUpdateService.updateAccountBalances(testAccount, createTxn(new BigDecimal("250.00")));
        accountRepository.save(testAccount);

        Account updated = accountRepository.findById(40001L).orElseThrow();
        assertEquals(new BigDecimal("250.00"), updated.getCurrentCycleCredit());
        assertEquals(new BigDecimal("1250.00"), updated.getCurrentBalance());
    }

    @Test
    @DisplayName("IT: Negative amount updates cycle debit and balance")
    void negativeAmount_updatesDebitAndBalance() {
        accountUpdateService.updateAccountBalances(testAccount, createTxn(new BigDecimal("-300.00")));
        accountRepository.save(testAccount);

        Account updated = accountRepository.findById(40001L).orElseThrow();
        assertEquals(new BigDecimal("-300.00"), updated.getCurrentCycleDebit());
        assertEquals(new BigDecimal("700.00"), updated.getCurrentBalance());
    }

    @Test
    @DisplayName("IT: Zero amount goes to credit path")
    void zeroAmount_goesToCredit() {
        accountUpdateService.updateAccountBalances(testAccount, createTxn(BigDecimal.ZERO));
        accountRepository.save(testAccount);

        Account updated = accountRepository.findById(40001L).orElseThrow();
        assertEquals(BigDecimal.ZERO, updated.getCurrentCycleCredit());
        assertEquals(new BigDecimal("1000.00"), updated.getCurrentBalance());
    }

    @Test
    @DisplayName("IT: Multiple transactions accumulate correctly in database")
    void multipleTransactions_accumulateInDb() {
        accountUpdateService.updateAccountBalances(testAccount, createTxn(new BigDecimal("200.00")));
        accountUpdateService.updateAccountBalances(testAccount, createTxn(new BigDecimal("-100.00")));
        accountUpdateService.updateAccountBalances(testAccount, createTxn(new BigDecimal("50.00")));
        accountRepository.save(testAccount);

        Account updated = accountRepository.findById(40001L).orElseThrow();
        assertEquals(new BigDecimal("1150.00"), updated.getCurrentBalance());
        assertEquals(new BigDecimal("250.00"), updated.getCurrentCycleCredit());
        assertEquals(new BigDecimal("-100.00"), updated.getCurrentCycleDebit());
    }
}
