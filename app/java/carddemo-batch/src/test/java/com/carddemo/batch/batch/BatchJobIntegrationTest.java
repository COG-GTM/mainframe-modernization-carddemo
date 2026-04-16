package com.carddemo.batch.batch;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.CardXref;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.model.TransactionCategoryBalance;
import com.carddemo.batch.model.TransactionCategoryBalanceId;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardXrefRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import com.carddemo.batch.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the batch job end-to-end.
 * Uses @SpringBatchTest with H2 in-memory database.
 */
@SpringBatchTest
@SpringBootTest
class BatchJobIntegrationTest {

    @TempDir
    static Path tempDir;

    private static Path inputFile;
    private static Path rejectFile;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job postDailyTransactionsJob;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionCategoryBalanceRepository tcatbalRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.batch.jdbc.initialize-schema", () -> "always");

        try {
            inputFile = tempDir.resolve("daily-transactions.dat");
            rejectFile = tempDir.resolve("daily-rejects.dat");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        registry.add("batch.input.file", () -> inputFile.toString());
        registry.add("batch.reject.file", () -> rejectFile.toString());
    }

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        tcatbalRepository.deleteAll();
        accountRepository.deleteAll();
        cardXrefRepository.deleteAll();

        jobLauncherTestUtils.setJob(postDailyTransactionsJob);
    }

    /**
     * Builds a 350-character fixed-width line from the given fields.
     */
    private String buildInputLine(String txnId, String typeCode, int catCode, String source,
                                   String desc, String amount, long merchantId,
                                   String merchantName, String merchantCity, String merchantZip,
                                   String cardNumber, String originTs, String processedTs) {
        StringBuilder sb = new StringBuilder(350);
        sb.append(padRight(txnId, 16));
        sb.append(padRight(typeCode, 2));
        sb.append(padLeft(String.valueOf(catCode), 4, '0'));
        sb.append(padRight(source, 10));
        sb.append(padRight(desc, 100));
        sb.append(padLeft(amount, 11, '0'));
        sb.append(padLeft(String.valueOf(merchantId), 9, '0'));
        sb.append(padRight(merchantName, 50));
        sb.append(padRight(merchantCity, 50));
        sb.append(padRight(merchantZip, 10));
        sb.append(padRight(cardNumber, 16));
        sb.append(padRight(originTs, 26));
        sb.append(padRight(processedTs, 26));
        sb.append(padRight("", 20)); // filler
        return sb.toString();
    }

    private void setupValidCard(String cardNumber, long accountId, BigDecimal creditLimit,
                                 BigDecimal currentBalance, BigDecimal cycCredit,
                                 BigDecimal cycDebit, LocalDate expirationDate) {
        CardXref xref = new CardXref();
        xref.setCardNumber(cardNumber);
        xref.setCustomerId(123456789L);
        xref.setAccountId(accountId);
        cardXrefRepository.save(xref);

        Account account = new Account();
        account.setAccountId(accountId);
        account.setActiveStatus("Y");
        account.setCurrentBalance(currentBalance);
        account.setCreditLimit(creditLimit);
        account.setCashCreditLimit(new BigDecimal("5000.00"));
        account.setOpenDate(LocalDate.of(2020, 1, 1));
        account.setExpirationDate(expirationDate);
        account.setReissueDate(LocalDate.of(2025, 1, 1));
        account.setCurrentCycleCredit(cycCredit);
        account.setCurrentCycleDebit(cycDebit);
        account.setAddressZip("12345");
        account.setGroupId("GRP001");
        accountRepository.save(account);
    }

    @Test
    void testEndToEndWithMixedTransactions() throws Exception {
        // Setup: 2 valid cards, 1 invalid card
        setupValidCard("4111111111111111", 10000000001L,
                new BigDecimal("10000.00"), new BigDecimal("5000.00"),
                new BigDecimal("1000.00"), new BigDecimal("500.00"),
                LocalDate.of(2030, 12, 31));

        setupValidCard("4222222222222222", 10000000002L,
                new BigDecimal("500.00"), new BigDecimal("200.00"),
                new BigDecimal("400.00"), new BigDecimal("100.00"),
                LocalDate.of(2030, 12, 31));

        // 5 transactions:
        // 1. Valid transaction on card 4111
        // 2. Invalid card number (not in XREF)
        // 3. Valid transaction on card 4222
        // 4. Overlimit on card 4222 (cycCredit 400 - cycDebit 100 + 300 = 600 > creditLimit 500)
        // 5. Valid transaction on card 4111
        String line1 = buildInputLine("TXN0000000000001", "SA", 5001, "ONLINE",
                "Valid purchase", "00000010000", 111111111L,
                "Amazon", "Seattle", "98101",
                "4111111111111111", "2025-06-15-10.30.00.000000", "");

        String line2 = buildInputLine("TXN0000000000002", "SA", 5001, "ONLINE",
                "Bad card purchase", "00000005000", 222222222L,
                "BadMerch", "Nowhere", "00000",
                "9999999999999999", "2025-06-15-11.00.00.000000", "");

        String line3 = buildInputLine("TXN0000000000003", "SA", 5002, "POS",
                "Valid POS purchase", "00000005000", 333333333L,
                "Walmart", "Bentonville", "72716",
                "4222222222222222", "2025-06-15-11.30.00.000000", "");

        String line4 = buildInputLine("TXN0000000000004", "SA", 5001, "ONLINE",
                "Overlimit purchase", "00000030000", 444444444L,
                "Expensive", "NYC", "10001",
                "4222222222222222", "2025-06-15-12.00.00.000000", "");

        String line5 = buildInputLine("TXN0000000000005", "SA", 5001, "POS",
                "Another valid purchase", "00000020000", 555555555L,
                "Target", "Minneapolis", "55403",
                "4111111111111111", "2025-06-15-12.30.00.000000", "");

        Files.writeString(inputFile, line1 + "\n" + line2 + "\n" + line3 + "\n"
                + line4 + "\n" + line5 + "\n");

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals("COMPLETED_WITH_REJECTS",
                execution.getStepExecutions().iterator().next().getExitStatus().getExitCode());

        // 3 valid transactions should be in DB
        List<Transaction> transactions = transactionRepository.findAll();
        assertEquals(3, transactions.size());

        // 2 rejected transactions in reject file
        List<String> rejectLines = Files.readAllLines(rejectFile);
        assertEquals(2, rejectLines.size());

        // Verify account 1 balance: 5000 + 100 + 200 = 5300
        Account acct1 = accountRepository.findById(10000000001L).orElseThrow();
        assertEquals(0, new BigDecimal("5300.00").compareTo(acct1.getCurrentBalance()));

        // Verify account 2 balance: 200 + 50 = 250 (only TXN3 posted, TXN4 rejected)
        Account acct2 = accountRepository.findById(10000000002L).orElseThrow();
        assertEquals(0, new BigDecimal("250.00").compareTo(acct2.getCurrentBalance()));

        // Verify TCATBAL records created
        TransactionCategoryBalanceId key1 = new TransactionCategoryBalanceId(
                10000000001L, "SA", 5001);
        assertTrue(tcatbalRepository.findById(key1).isPresent());
        // TXN1 (100) + TXN5 (200) = 300
        assertEquals(0, new BigDecimal("300.00").compareTo(
                tcatbalRepository.findById(key1).get().getBalance()));

        TransactionCategoryBalanceId key2 = new TransactionCategoryBalanceId(
                10000000002L, "SA", 5002);
        assertTrue(tcatbalRepository.findById(key2).isPresent());
        assertEquals(0, new BigDecimal("50.00").compareTo(
                tcatbalRepository.findById(key2).get().getBalance()));
    }

    @Test
    void testEndToEndAllValid() throws Exception {
        setupValidCard("4111111111111111", 10000000001L,
                new BigDecimal("10000.00"), new BigDecimal("5000.00"),
                new BigDecimal("1000.00"), new BigDecimal("500.00"),
                LocalDate.of(2030, 12, 31));

        String line1 = buildInputLine("TXN0000000000010", "SA", 5001, "ONLINE",
                "Purchase 1", "00000010000", 111111111L,
                "Amazon", "Seattle", "98101",
                "4111111111111111", "2025-06-15-10.30.00.000000", "");

        String line2 = buildInputLine("TXN0000000000011", "SA", 5002, "POS",
                "Purchase 2", "00000020000", 222222222L,
                "Target", "Minneapolis", "55403",
                "4111111111111111", "2025-06-15-11.00.00.000000", "");

        Files.writeString(inputFile, line1 + "\n" + line2 + "\n");

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(ExitStatus.COMPLETED,
                execution.getStepExecutions().iterator().next().getExitStatus());

        List<Transaction> transactions = transactionRepository.findAll();
        assertEquals(2, transactions.size());

        List<String> rejectLines = Files.readAllLines(rejectFile);
        assertEquals(0, rejectLines.size());
    }

    @Test
    void testEndToEndAllRejected() throws Exception {
        // No XREF records at all — all transactions will fail with reason 100

        String line1 = buildInputLine("TXN0000000000020", "SA", 5001, "ONLINE",
                "Bad purchase 1", "00000010000", 111111111L,
                "Merchant1", "City1", "11111",
                "9999999999999991", "2025-06-15-10.30.00.000000", "");

        String line2 = buildInputLine("TXN0000000000021", "SA", 5001, "ONLINE",
                "Bad purchase 2", "00000020000", 222222222L,
                "Merchant2", "City2", "22222",
                "9999999999999992", "2025-06-15-11.00.00.000000", "");

        String line3 = buildInputLine("TXN0000000000022", "SA", 5001, "ONLINE",
                "Bad purchase 3", "00000030000", 333333333L,
                "Merchant3", "City3", "33333",
                "9999999999999993", "2025-06-15-11.30.00.000000", "");

        Files.writeString(inputFile, line1 + "\n" + line2 + "\n" + line3 + "\n");

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals("COMPLETED_WITH_REJECTS",
                execution.getStepExecutions().iterator().next().getExitStatus().getExitCode());

        // No Transaction records should exist
        List<Transaction> transactions = transactionRepository.findAll();
        assertEquals(0, transactions.size());

        // All 3 transactions should be in reject file
        List<String> rejectLines = Files.readAllLines(rejectFile);
        assertEquals(3, rejectLines.size());
    }

    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    private static String padLeft(String s, int width, char padChar) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return String.valueOf(padChar).repeat(width - s.length()) + s;
    }
}
