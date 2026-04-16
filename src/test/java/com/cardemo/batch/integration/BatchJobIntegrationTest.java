package com.cardemo.batch.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransactionEntity;
import com.cardemo.batch.model.RejectedTransaction;
import com.cardemo.batch.model.Transaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionCategoryBalanceId;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.DailyTransactionRepository;
import com.cardemo.batch.repository.RejectedTransactionRepository;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.repository.TransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete batch transaction posting job.
 * Uses H2 in-memory database with Spring Batch test utilities.
 * NOTE: Do NOT use @Transactional on test methods - it conflicts with
 * Spring Batch's own transaction management in the JobRepository.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@SpringBatchTest
@ActiveProfiles("test")
class BatchJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job transactionPostingJob;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RejectedTransactionRepository rejectedTransactionRepository;

    @Autowired
    private TransactionCategoryBalanceRepository categoryBalanceRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(transactionPostingJob);
        // Clean up test data
        rejectedTransactionRepository.deleteAll();
        transactionRepository.deleteAll();
        categoryBalanceRepository.deleteAll();
        dailyTransactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private void insertDailyTransaction(String id, String cardNum, BigDecimal amount,
                                         String typeCd, int catCd, String origTs) {
        DailyTransactionEntity dte = new DailyTransactionEntity();
        dte.setId(id);
        dte.setCardNum(cardNum);
        dte.setAmount(amount);
        dte.setTypeCd(typeCd);
        dte.setCatCd(catCd);
        dte.setSource("ONLINE");
        dte.setDescription("Test transaction");
        dte.setMerchantId(123456789L);
        dte.setMerchantName("TEST STORE");
        dte.setMerchantCity("NEW YORK");
        dte.setMerchantZip("10001");
        dte.setOrigTimestamp(origTs);
        dailyTransactionRepository.save(dte);
    }

    private void insertAccount(long acctId, BigDecimal creditLimit,
                                BigDecimal cycCredit, BigDecimal cycDebit,
                                String expDate) {
        Account acct = new Account();
        acct.setAcctId(acctId);
        acct.setCreditLimit(creditLimit);
        acct.setCurrentCycleCredit(cycCredit);
        acct.setCurrentCycleDebit(cycDebit);
        acct.setCurrentBalance(BigDecimal.ZERO);
        acct.setCashCreditLimit(BigDecimal.ZERO);
        acct.setExpirationDate(expDate);
        acct.setActiveStatus("Y");
        acct.setOpenDate("2020-01-01");
        acct.setReissueDate("2024-01-01");
        accountRepository.save(acct);
    }

    private void insertCardXref(String cardNum, long acctId) {
        CardXref xref = new CardXref();
        xref.setCardNum(cardNum);
        xref.setAcctId(acctId);
        xref.setCustId(999999999L);
        cardXrefRepository.save(xref);
    }

    private JobExecution launchJob() throws Exception {
        return jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters());
    }

    // --- Integration Test 1: Full successful transaction posting ---

    @Test
    @DisplayName("IT1: Valid transaction posts successfully with account and category balance updates")
    void fullJob_validTransaction_postsSuccessfully() throws Exception {
        insertAccount(12345678901L, new BigDecimal("10000.00"),
                new BigDecimal("500.00"), new BigDecimal("200.00"), "2030-12-31");
        insertCardXref("1234567890123456", 12345678901L);
        insertDailyTransaction("TRAN0000000001", "1234567890123456",
                new BigDecimal("100.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(1, transactionRepository.count());
        assertEquals(0, rejectedTransactionRepository.count());

        Transaction posted = transactionRepository.findAll().get(0);
        assertEquals("TRAN0000000001", posted.getId());
        assertNotNull(posted.getProcTimestamp());

        Account updatedAcct = accountRepository.findById(12345678901L).orElseThrow();
        assertEquals(new BigDecimal("100.00"), updatedAcct.getCurrentBalance());
        assertEquals(new BigDecimal("600.00"), updatedAcct.getCurrentCycleCredit());
    }

    // --- Integration Test 2: Rejected transaction (invalid card) ---

    @Test
    @DisplayName("IT2: Invalid card number creates rejected transaction in dead-letter queue")
    void fullJob_invalidCard_writesToDeadLetterQueue() throws Exception {
        insertDailyTransaction("TRAN0000000002", "9999999999999999",
                new BigDecimal("50.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(0, transactionRepository.count());
        assertEquals(1, rejectedTransactionRepository.count());

        RejectedTransaction rejected = rejectedTransactionRepository.findAll().get(0);
        assertEquals("TRAN0000000002", rejected.getTransactionId());
        assertEquals(100, rejected.getFailReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", rejected.getFailReasonDescription());
    }

    // --- Integration Test 3: Exit code when rejections exist ---

    @Test
    @DisplayName("IT3: Job exit status is COMPLETED_WITH_REJECTS when rejections exist")
    void fullJob_withRejections_exitCodeCompletedWithRejects() throws Exception {
        insertDailyTransaction("TRAN0000000003", "0000000000000000",
                new BigDecimal("25.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals("COMPLETED_WITH_REJECTS", execution.getExitStatus().getExitCode());
    }

    // --- Integration Test 4: Overlimit transaction rejection ---

    @Test
    @DisplayName("IT4: Overlimit transaction rejected with code 102")
    void fullJob_overlimit_rejected() throws Exception {
        insertAccount(22222222222L, new BigDecimal("1000.00"),
                new BigDecimal("900.00"), new BigDecimal("0.00"), "2030-12-31");
        insertCardXref("2222222222222222", 22222222222L);
        insertDailyTransaction("TRAN0000000004", "2222222222222222",
                new BigDecimal("200.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(1, rejectedTransactionRepository.count());
        RejectedTransaction rejected = rejectedTransactionRepository.findAll().get(0);
        assertEquals(102, rejected.getFailReasonCode());
        assertEquals("OVERLIMIT TRANSACTION", rejected.getFailReasonDescription());
    }

    // --- Integration Test 5: Expired account rejection ---

    @Test
    @DisplayName("IT5: Expired account transaction rejected with code 103")
    void fullJob_expired_rejected() throws Exception {
        insertAccount(33333333333L, new BigDecimal("10000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2020-01-01");
        insertCardXref("3333333333333333", 33333333333L);
        insertDailyTransaction("TRAN0000000005", "3333333333333333",
                new BigDecimal("50.00"), "SA", 5001, "2025-06-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(1, rejectedTransactionRepository.count());
        RejectedTransaction rejected = rejectedTransactionRepository.findAll().get(0);
        assertEquals(103, rejected.getFailReasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", rejected.getFailReasonDescription());
    }

    // --- Integration Test 6: Transaction category balance creation ---

    @Test
    @DisplayName("IT6: New transaction category balance record created for unknown composite key")
    void fullJob_newCategoryBalance_created() throws Exception {
        insertAccount(44444444444L, new BigDecimal("10000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2030-12-31");
        insertCardXref("4444444444444444", 44444444444L);
        insertDailyTransaction("TRAN0000000006", "4444444444444444",
                new BigDecimal("75.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        TransactionCategoryBalanceId catBalId = new TransactionCategoryBalanceId(44444444444L, "SA", 5001);
        assertTrue(categoryBalanceRepository.findById(catBalId).isPresent());
        assertEquals(new BigDecimal("75.00"), categoryBalanceRepository.findById(catBalId).get().getBalance());
    }

    // --- Integration Test 7: Transaction category balance update ---

    @Test
    @DisplayName("IT7: Existing transaction category balance record updated by adding amount")
    void fullJob_existingCategoryBalance_updated() throws Exception {
        insertAccount(55555555555L, new BigDecimal("10000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2030-12-31");
        insertCardXref("5555555555555555", 55555555555L);

        // Pre-insert category balance
        TransactionCategoryBalance existingCatBal = new TransactionCategoryBalance();
        existingCatBal.setAcctId(55555555555L);
        existingCatBal.setTypeCd("SA");
        existingCatBal.setCatCd(5001);
        existingCatBal.setBalance(new BigDecimal("200.00"));
        categoryBalanceRepository.save(existingCatBal);

        insertDailyTransaction("TRAN0000000007", "5555555555555555",
                new BigDecimal("50.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        TransactionCategoryBalanceId catBalId = new TransactionCategoryBalanceId(55555555555L, "SA", 5001);
        assertEquals(new BigDecimal("250.00"), categoryBalanceRepository.findById(catBalId).get().getBalance());
    }

    // --- Integration Test 8: Credit/debit separation with negative amount ---

    @Test
    @DisplayName("IT8: Negative amount updates debit cycle, not credit cycle")
    void fullJob_negativeAmount_updatesDebitCycle() throws Exception {
        insertAccount(66666666666L, new BigDecimal("10000.00"),
                new BigDecimal("1000.00"), new BigDecimal("500.00"), "2030-12-31");
        insertCardXref("6666666666666666", 66666666666L);
        insertDailyTransaction("TRAN0000000008", "6666666666666666",
                new BigDecimal("-75.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        Account updatedAcct = accountRepository.findById(66666666666L).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), updatedAcct.getCurrentCycleCredit()); // unchanged
        assertEquals(new BigDecimal("425.00"), updatedAcct.getCurrentCycleDebit()); // 500 + (-75)
        assertEquals(new BigDecimal("-75.00"), updatedAcct.getCurrentBalance()); // 0 + (-75)
    }

    // --- Integration Test 9: Mixed valid and invalid transactions ---

    @Test
    @DisplayName("IT9: Mixed batch with both valid and invalid transactions processed correctly")
    void fullJob_mixedTransactions_bothProcessed() throws Exception {
        // Valid transaction setup
        insertAccount(77777777777L, new BigDecimal("10000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "2030-12-31");
        insertCardXref("7777777777777777", 77777777777L);
        insertDailyTransaction("TRAN0000000009", "7777777777777777",
                new BigDecimal("100.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        // Invalid transaction (no card xref)
        insertDailyTransaction("TRAN0000000010", "8888888888888888",
                new BigDecimal("50.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals("COMPLETED_WITH_REJECTS", execution.getExitStatus().getExitCode());
        assertEquals(1, transactionRepository.count());
        assertEquals(1, rejectedTransactionRepository.count());
    }

    // --- Integration Test 10: Prometheus metrics integration ---

    @Test
    @DisplayName("IT10: Prometheus metrics counters are registered and updated")
    void fullJob_metricsRegistered() throws Exception {
        insertDailyTransaction("TRAN0000000011", "0000000000000000",
                new BigDecimal("10.00"), "SA", 5001, "2025-01-15-10.30.00.000000");

        JobExecution execution = launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertNotNull(meterRegistry.find("cardemo.batch.transactions.total").counter());
        assertNotNull(meterRegistry.find("cardemo.batch.transactions.rejected").counter());
    }
}
