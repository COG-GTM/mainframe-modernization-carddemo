package com.carddemo;

import com.carddemo.entity.Account;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.TranCatBalance;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionReject;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRejectRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.TransactionPostingService;
import com.carddemo.service.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransactionValidationTest {

    @Autowired
    private TransactionPostingService service;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TranCatBalanceRepository tranCatBalanceRepository;
    @Autowired
    private TransactionRejectRepository transactionRejectRepository;

    @BeforeEach
    void cleanUp() {
        transactionRejectRepository.deleteAll();
        transactionRepository.deleteAll();
        tranCatBalanceRepository.deleteAll();
        accountRepository.deleteAll();
        cardXrefRepository.deleteAll();
    }

    @Test
    void testValidTransaction() {
        cardXrefRepository.save(TestDataFactory.xref());
        accountRepository.save(TestDataFactory.account());
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000001", new BigDecimal("250.00"));

        ValidationResult result = service.processSingleTransaction(txn);

        assertTrue(result.isValid());
        assertEquals(0, result.getFailReasonCode());

        // Transaction written
        Optional<Transaction> written = transactionRepository.findById("TXN0000000000001");
        assertTrue(written.isPresent());
        assertEquals(new BigDecimal("250.00"), written.get().getAmount());

        // Account balances updated
        Account account = accountRepository.findByAcctId(TestDataFactory.ACCT_ID).orElseThrow();
        assertEquals(new BigDecimal("350.00"), account.getCurrBal());
        assertEquals(new BigDecimal("250.00"), account.getCurrCycCredit());

        // TCATBAL updated
        TranCatBalance bal = tranCatBalanceRepository
                .findByAcctIdAndTypeCdAndCatCd(TestDataFactory.ACCT_ID, "01", 5).orElseThrow();
        assertEquals(new BigDecimal("250.00"), bal.getBalance());

        assertEquals(0, transactionRejectRepository.count());
    }

    @Test
    void testInvalidCardNumber() {
        accountRepository.save(TestDataFactory.account());
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000002", new BigDecimal("10.00"));
        txn.setCardNum("0000000000000000");

        ValidationResult result = service.processSingleTransaction(txn);

        assertFalse(result.isValid());
        assertEquals(100, result.getFailReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.getFailReasonDesc());

        List<TransactionReject> rejects = transactionRejectRepository.findAll();
        assertEquals(1, rejects.size());
        assertEquals(100, rejects.get(0).getFailReasonCode());
    }

    @Test
    void testAccountNotFound() {
        cardXrefRepository.save(TestDataFactory.xref());
        // No account saved.
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000003", new BigDecimal("10.00"));

        ValidationResult result = service.processSingleTransaction(txn);

        assertFalse(result.isValid());
        assertEquals(101, result.getFailReasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailReasonDesc());
        assertEquals(1, transactionRejectRepository.count());
    }

    @Test
    void testOverlimitTransaction() {
        cardXrefRepository.save(TestDataFactory.xref());
        Account account = TestDataFactory.account();
        account.setCreditLimit(new BigDecimal("100.00"));
        account.setCurrCycCredit(new BigDecimal("90.00"));
        account.setCurrCycDebit(new BigDecimal("0.00"));
        accountRepository.save(account);
        // tempBal = 90 - 0 + 50 = 140 > creditLimit 100 -> overlimit
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000004", new BigDecimal("50.00"));

        ValidationResult result = service.processSingleTransaction(txn);

        assertFalse(result.isValid());
        assertEquals(102, result.getFailReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.getFailReasonDesc());
    }

    @Test
    void testExpiredAccount() {
        cardXrefRepository.save(TestDataFactory.xref());
        Account account = TestDataFactory.account();
        account.setExpirationDate(LocalDate.of(2020, 1, 1)); // before origTs 2023-06-01
        accountRepository.save(account);
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000005", new BigDecimal("10.00"));

        ValidationResult result = service.processSingleTransaction(txn);

        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.getFailReasonDesc());
    }

    @Test
    void testBothOverlimitAndExpired() {
        cardXrefRepository.save(TestDataFactory.xref());
        Account account = TestDataFactory.account();
        account.setCreditLimit(new BigDecimal("100.00"));
        account.setCurrCycCredit(new BigDecimal("90.00"));
        account.setCurrCycDebit(new BigDecimal("0.00"));
        account.setExpirationDate(LocalDate.of(2020, 1, 1));
        accountRepository.save(account);
        DailyTransaction txn = TestDataFactory.transaction("TXN0000000000006", new BigDecimal("50.00"));

        ValidationResult result = service.processSingleTransaction(txn);

        // Last-writer-wins per COBOL: expiration (103) overwrites overlimit (102).
        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
    }
}
