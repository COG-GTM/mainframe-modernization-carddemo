package com.cardemo.batch.job;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.DisclosureGroupRepository;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import com.cardemo.batch.service.InterestCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the interest calculation batch job.
 * Uses H2 in-memory database and verifies end-to-end processing.
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class InterestCalculationIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job interestCalculationJob;

    @Autowired
    private TransactionCategoryBalanceRepository categoryBalanceRepository;

    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private InterestCalculationService interestCalculationService;

    @BeforeEach
    void setUp() {
        transactionRecordRepository.deleteAll();
        categoryBalanceRepository.deleteAll();
        disclosureGroupRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();

        jobLauncherTestUtils.setJob(interestCalculationJob);
    }

    private void seedTestData() {
        // Account 1
        Account acct1 = new Account();
        acct1.setAcctId("00000012345");
        acct1.setActiveStatus("Y");
        acct1.setCurrBal(new BigDecimal("5000.00"));
        acct1.setCreditLimit(new BigDecimal("10000.00"));
        acct1.setCashCreditLimit(new BigDecimal("5000.00"));
        acct1.setOpenDate("2020-01-01");
        acct1.setExpirationDate("2025-12-31");
        acct1.setReissueDate("2023-01-01");
        acct1.setCurrCycCredit(new BigDecimal("1000.00"));
        acct1.setCurrCycDebit(new BigDecimal("500.00"));
        acct1.setAddrZip("10001");
        acct1.setGroupId("GRP001");
        accountRepository.save(acct1);

        // Account 2
        Account acct2 = new Account();
        acct2.setAcctId("00000067890");
        acct2.setActiveStatus("Y");
        acct2.setCurrBal(new BigDecimal("8000.00"));
        acct2.setCreditLimit(new BigDecimal("15000.00"));
        acct2.setCashCreditLimit(new BigDecimal("7500.00"));
        acct2.setOpenDate("2019-06-15");
        acct2.setExpirationDate("2026-06-15");
        acct2.setReissueDate("2022-06-15");
        acct2.setCurrCycCredit(new BigDecimal("2000.00"));
        acct2.setCurrCycDebit(new BigDecimal("1500.00"));
        acct2.setAddrZip("90210");
        acct2.setGroupId("GRP002");
        accountRepository.save(acct2);

        // Card cross-references
        cardXrefRepository.save(new CardXref("4111111111111111", "000000001", "00000012345"));
        cardXrefRepository.save(new CardXref("5222222222222222", "000000002", "00000067890"));

        // Disclosure groups
        disclosureGroupRepository.save(new DisclosureGroup("GRP001", "01", "0001", new BigDecimal("18.00")));
        disclosureGroupRepository.save(new DisclosureGroup("GRP001", "01", "0002", new BigDecimal("24.00")));
        disclosureGroupRepository.save(new DisclosureGroup("DEFAULT", "01", "0001", new BigDecimal("12.00")));
        disclosureGroupRepository.save(new DisclosureGroup("DEFAULT", "01", "0003", new BigDecimal("15.00")));

        // Category balances for account 1 (two categories)
        categoryBalanceRepository.save(
                new TransactionCategoryBalance("00000012345", "01", "0001", new BigDecimal("3000.00")));
        categoryBalanceRepository.save(
                new TransactionCategoryBalance("00000012345", "01", "0002", new BigDecimal("2000.00")));

        // Category balances for account 2 (one category, needs DEFAULT fallback for 0003)
        categoryBalanceRepository.save(
                new TransactionCategoryBalance("00000067890", "01", "0003", new BigDecimal("6000.00")));
    }

    // --- Integration Test 1: Full batch job runs successfully ---
    @Test
    @DisplayName("IT-1: Full batch job completes with COMPLETED status")
    void batchJob_completesSuccessfully() throws Exception {
        seedTestData();

        JobParameters params = new JobParametersBuilder()
                .addString("runDate", "2024-01-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    // --- Integration Test 2: Correct number of transactions generated ---
    @Test
    @DisplayName("IT-2: Generates correct number of interest transactions")
    void batchJob_generatesCorrectTransactionCount() throws Exception {
        seedTestData();

        JobParameters params = new JobParametersBuilder()
                .addString("runDate", "2024-01-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        List<TransactionRecord> transactions = transactionRecordRepository.findAll();
        // 2 categories for acct1 (both have rates) + 1 category for acct2 (DEFAULT fallback)
        assertEquals(3, transactions.size());
    }

    // --- Integration Test 3: Account balances updated correctly ---
    @Test
    @DisplayName("IT-3: Account balances updated with interest and cycle counters reset")
    void batchJob_updatesAccountBalances() throws Exception {
        seedTestData();

        JobParameters params = new JobParametersBuilder()
                .addString("runDate", "2024-01-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        // Account 1: original 5000, interest = (3000*18/1200) + (2000*24/1200) = 45.00 + 40.00 = 85.00
        Account acct1 = accountRepository.findById("00000012345").orElseThrow();
        assertEquals(0, new BigDecimal("5085.00").compareTo(acct1.getCurrBal()),
                "Account 1 balance should be 5000 + 85 = 5085.00, got " + acct1.getCurrBal());
        assertEquals(0, BigDecimal.ZERO.compareTo(acct1.getCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(acct1.getCurrCycDebit()));

        // Account 2: original 8000, interest = (6000*15/1200) = 75.00
        Account acct2 = accountRepository.findById("00000067890").orElseThrow();
        assertEquals(0, new BigDecimal("8075.00").compareTo(acct2.getCurrBal()),
                "Account 2 balance should be 8000 + 75 = 8075.00, got " + acct2.getCurrBal());
        assertEquals(0, BigDecimal.ZERO.compareTo(acct2.getCurrCycCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(acct2.getCurrCycDebit()));
    }

    // --- Integration Test 4: Disclosure group fallback works end-to-end ---
    @Test
    @DisplayName("IT-4: Disclosure group DEFAULT fallback produces correct interest")
    void batchJob_disclosureGroupFallback() throws Exception {
        seedTestData();

        JobParameters params = new JobParametersBuilder()
                .addString("runDate", "2024-01-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        // Account 2, cat 0003: GRP002 not in disclosure_group, falls back to DEFAULT rate 15.00
        // Interest = (6000 * 15) / 1200 = 75.00
        List<TransactionRecord> transactions = transactionRecordRepository.findAll();
        TransactionRecord acct2Txn = transactions.stream()
                .filter(t -> t.getTranDesc().contains("00000067890"))
                .findFirst()
                .orElseThrow();

        assertEquals(0, new BigDecimal("75.00").compareTo(acct2Txn.getTranAmt()),
                "Interest for acct2 should be 75.00, got " + acct2Txn.getTranAmt());
        assertEquals("5222222222222222", acct2Txn.getCardNum());
    }

    // --- Integration Test 5: Transaction record fields are correct ---
    @Test
    @DisplayName("IT-5: Generated transactions have correct field values")
    void batchJob_transactionFieldsCorrect() throws Exception {
        seedTestData();

        JobParameters params = new JobParametersBuilder()
                .addString("runDate", "2024-01-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        List<TransactionRecord> transactions = transactionRecordRepository.findAll();
        assertFalse(transactions.isEmpty());

        for (TransactionRecord txn : transactions) {
            // All interest transactions must have type '01', cat '05', source 'System'
            assertEquals("01", txn.getTranTypeCd(), "Type code should be 01");
            assertEquals("05", txn.getTranCatCd(), "Category code should be 05");
            assertEquals("System", txn.getTranSource(), "Source should be System");

            // Transaction ID format: PARM-DATE + 6-digit suffix
            assertTrue(txn.getTranId().startsWith("2024-01-15"),
                    "Tran ID should start with run date: " + txn.getTranId());
            assertEquals(16, txn.getTranId().length(),
                    "Tran ID should be 16 chars (10 date + 6 suffix): " + txn.getTranId());

            // Description format
            assertTrue(txn.getTranDesc().startsWith("Int. for a/c "),
                    "Description should start with 'Int. for a/c ': " + txn.getTranDesc());

            // Timestamps present
            assertNotNull(txn.getOrigTs());
            assertNotNull(txn.getProcTs());
        }
    }
}
