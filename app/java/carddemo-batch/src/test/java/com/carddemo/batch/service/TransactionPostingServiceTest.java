package com.carddemo.batch.service;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.CardXref;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.model.TransactionCategoryBalance;
import com.carddemo.batch.model.TransactionCategoryBalanceId;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import com.carddemo.batch.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransactionPostingService using @DataJpaTest with H2.
 * Validates the posting logic from COBOL paragraphs 2000-2900.
 */
@DataJpaTest
@Import(TransactionPostingService.class)
class TransactionPostingServiceTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionCategoryBalanceRepository tcatbalRepository;

    @Autowired
    private TransactionPostingService postingService;

    private Account testAccount;
    private CardXref testXref;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        tcatbalRepository.deleteAll();
        accountRepository.deleteAll();

        testAccount = new Account();
        testAccount.setAccountId(12345678901L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrentBalance(new BigDecimal("5000.00"));
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testAccount.setCashCreditLimit(new BigDecimal("5000.00"));
        testAccount.setOpenDate(LocalDate.of(2020, 1, 1));
        testAccount.setExpirationDate(LocalDate.of(2030, 12, 31));
        testAccount.setReissueDate(LocalDate.of(2025, 1, 1));
        testAccount.setCurrentCycleCredit(new BigDecimal("1000.00"));
        testAccount.setCurrentCycleDebit(new BigDecimal("500.00"));
        testAccount.setAddressZip("12345");
        testAccount.setGroupId("GRP001");
        accountRepository.save(testAccount);

        testXref = new CardXref();
        testXref.setCardNumber("4111111111111111");
        testXref.setCustomerId(123456789L);
        testXref.setAccountId(12345678901L);
    }

    private DailyTransaction createTransaction(BigDecimal amount) {
        DailyTransaction txn = new DailyTransaction();
        txn.setTransactionId("TXN0000000000001");
        txn.setTypeCode("SA");
        txn.setCategoryCode(5001);
        txn.setSource("ONLINE");
        txn.setDescription("Test transaction");
        txn.setAmount(amount);
        txn.setMerchantId(123456789L);
        txn.setMerchantName("Test Merchant");
        txn.setMerchantCity("Test City");
        txn.setMerchantZip("12345");
        txn.setCardNumber("4111111111111111");
        txn.setOriginTimestamp(LocalDateTime.of(2025, 6, 15, 10, 30, 0));
        return txn;
    }

    @Test
    void testPostCreatesTransaction() {
        DailyTransaction txn = createTransaction(new BigDecimal("150.00"));

        postingService.post(txn, testXref, testAccount);

        Optional<Transaction> saved = transactionRepository.findById("TXN0000000000001");
        assertTrue(saved.isPresent(), "Transaction should be saved");

        Transaction result = saved.get();
        assertEquals("TXN0000000000001", result.getTransactionId());
        assertEquals("SA", result.getTypeCode());
        assertEquals(5001, result.getCategoryCode());
        assertEquals("ONLINE", result.getSource());
        assertEquals("Test transaction", result.getDescription());
        assertEquals(0, new BigDecimal("150.00").compareTo(result.getAmount()));
        assertEquals(123456789L, result.getMerchantId());
        assertEquals("Test Merchant", result.getMerchantName());
        assertEquals("Test City", result.getMerchantCity());
        assertEquals("12345", result.getMerchantZip());
        assertEquals("4111111111111111", result.getCardNumber());
        assertEquals(LocalDateTime.of(2025, 6, 15, 10, 30, 0), result.getOriginTimestamp());
        assertNotNull(result.getProcessedTimestamp(), "processedTimestamp should be set");
    }

    @Test
    void testPostUpdatesAccountBalancePositiveAmount() {
        BigDecimal amount = new BigDecimal("200.00");
        DailyTransaction txn = createTransaction(amount);

        postingService.post(txn, testXref, testAccount);

        Account updated = accountRepository.findById(12345678901L).orElseThrow();
        // currentBalance: 5000 + 200 = 5200
        assertEquals(0, new BigDecimal("5200.00").compareTo(updated.getCurrentBalance()));
        // currentCycleCredit: 1000 + 200 = 1200 (amount >= 0 goes to credit)
        assertEquals(0, new BigDecimal("1200.00").compareTo(updated.getCurrentCycleCredit()));
        // currentCycleDebit unchanged: 500
        assertEquals(0, new BigDecimal("500.00").compareTo(updated.getCurrentCycleDebit()));
    }

    @Test
    void testPostUpdatesAccountBalanceNegativeAmount() {
        BigDecimal amount = new BigDecimal("-300.00");
        DailyTransaction txn = createTransaction(amount);

        postingService.post(txn, testXref, testAccount);

        Account updated = accountRepository.findById(12345678901L).orElseThrow();
        // currentBalance: 5000 + (-300) = 4700
        assertEquals(0, new BigDecimal("4700.00").compareTo(updated.getCurrentBalance()));
        // currentCycleCredit unchanged: 1000 (amount < 0 goes to debit)
        assertEquals(0, new BigDecimal("1000.00").compareTo(updated.getCurrentCycleCredit()));
        // currentCycleDebit: 500 + (-300) = 200
        assertEquals(0, new BigDecimal("200.00").compareTo(updated.getCurrentCycleDebit()));
    }

    @Test
    void testPostCreatesTcatbalWhenNotExists() {
        DailyTransaction txn = createTransaction(new BigDecimal("250.00"));

        postingService.post(txn, testXref, testAccount);

        TransactionCategoryBalanceId key = new TransactionCategoryBalanceId(
                12345678901L, "SA", 5001);
        Optional<TransactionCategoryBalance> tcatbal = tcatbalRepository.findById(key);

        assertTrue(tcatbal.isPresent(), "TCATBAL record should be created");
        assertEquals(0, new BigDecimal("250.00").compareTo(tcatbal.get().getBalance()));
    }

    @Test
    void testPostUpdatesTcatbalWhenExists() {
        // Pre-populate TCATBAL record
        TransactionCategoryBalanceId key = new TransactionCategoryBalanceId(
                12345678901L, "SA", 5001);
        TransactionCategoryBalance existing = new TransactionCategoryBalance();
        existing.setId(key);
        existing.setBalance(new BigDecimal("1000.00"));
        tcatbalRepository.save(existing);

        DailyTransaction txn = createTransaction(new BigDecimal("250.00"));

        postingService.post(txn, testXref, testAccount);

        TransactionCategoryBalance updated = tcatbalRepository.findById(key).orElseThrow();
        // balance: 1000 + 250 = 1250
        assertEquals(0, new BigDecimal("1250.00").compareTo(updated.getBalance()));
    }
}
