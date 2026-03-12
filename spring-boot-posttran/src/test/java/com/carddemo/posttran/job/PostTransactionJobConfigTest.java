package com.carddemo.posttran.job;

import com.carddemo.posttran.model.Account;
import com.carddemo.posttran.model.CardXref;
import com.carddemo.posttran.model.DailyTransaction;
import com.carddemo.posttran.model.TransactionReject;
import com.carddemo.posttran.repository.AccountRepository;
import com.carddemo.posttran.repository.CardXrefRepository;
import com.carddemo.posttran.repository.DailyTransactionRepository;
import com.carddemo.posttran.repository.TransactionRejectRepository;
import com.carddemo.posttran.repository.TransactionRepository;
import com.carddemo.posttran.repository.CategoryBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the PostTransactionJob.
 * Tests the complete batch flow: read → validate → post/reject → write.
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class PostTransactionJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionRejectRepository transactionRejectRepository;

    @Autowired
    private CategoryBalanceRepository categoryBalanceRepository;

    @BeforeEach
    void setUp() {
        // Clean all tables
        transactionRejectRepository.deleteAll();
        transactionRepository.deleteAll();
        categoryBalanceRepository.deleteAll();
        dailyTransactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private void setupValidScenario() {
        // Create account
        Account account = new Account();
        account.setAcctId(12345678901L);
        account.setAcctActiveStatus("Y");
        account.setAcctCurrBal(new BigDecimal("1000.00"));
        account.setAcctCreditLimit(new BigDecimal("5000.00"));
        account.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        account.setAcctOpenDate("2020-01-01");
        account.setAcctExpirationDate("2027-12-31");
        account.setAcctReissueDate("2025-01-01");
        account.setAcctCurrCycCredit(new BigDecimal("200.00"));
        account.setAcctCurrCycDebit(new BigDecimal("50.00"));
        account.setAcctAddrZip("12345");
        account.setAcctGroupId("GRP001");
        accountRepository.save(account);

        // Create card xref
        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setXrefCustId(100000001L);
        xref.setXrefAcctId(12345678901L);
        cardXrefRepository.save(xref);

        // Create daily transaction
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("0000000000000001");
        dt.setTranTypeCd("SA");
        dt.setTranCatCd(5001);
        dt.setTranSource("ONLINE");
        dt.setTranDesc("Test purchase");
        dt.setTranAmt(new BigDecimal("100.00"));
        dt.setTranMerchantId(123456789L);
        dt.setTranMerchantName("Test Merchant");
        dt.setTranMerchantCity("Test City");
        dt.setTranMerchantZip("12345");
        dt.setTranCardNum("4111111111111111");
        dt.setTranOrigTs("2025-06-15-10.30.00.000000");
        dailyTransactionRepository.save(dt);
    }

    @Test
    void validTransaction_shouldBePosted() throws Exception {
        setupValidScenario();

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // Verify transaction was posted
        assertEquals(1, transactionRepository.count());
        assertEquals(0, transactionRejectRepository.count());

        // Verify category balance was created
        assertEquals(1, categoryBalanceRepository.count());

        // Verify account was updated
        Account updatedAccount = accountRepository.findById(12345678901L).orElseThrow();
        assertEquals(new BigDecimal("1100.00"), updatedAccount.getAcctCurrBal());
        assertEquals(new BigDecimal("300.00"), updatedAccount.getAcctCurrCycCredit());
    }

    @Test
    void invalidCardNumber_shouldBeRejected() throws Exception {
        // Create a daily transaction with no matching xref
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("0000000000000002");
        dt.setTranTypeCd("SA");
        dt.setTranCatCd(5001);
        dt.setTranSource("ONLINE");
        dt.setTranDesc("Unknown card");
        dt.setTranAmt(new BigDecimal("50.00"));
        dt.setTranMerchantId(123456789L);
        dt.setTranMerchantName("Test Merchant");
        dt.setTranMerchantCity("Test City");
        dt.setTranMerchantZip("12345");
        dt.setTranCardNum("9999999999999999");
        dt.setTranOrigTs("2025-06-15-10.30.00.000000");
        dailyTransactionRepository.save(dt);

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // Transaction should be rejected, not posted
        assertEquals(0, transactionRepository.count());
        assertEquals(1, transactionRejectRepository.count());

        // Verify reject record
        List<TransactionReject> rejects = transactionRejectRepository.findAll();
        assertEquals(100, rejects.get(0).getValidationFailReason());

        // Exit status should indicate rejects
        assertTrue(execution.getExitStatus().getExitCode().contains("COMPLETED_WITH_REJECTS")
                || execution.getExitStatus().equals(ExitStatus.COMPLETED));
    }

    @Test
    void mixedTransactions_shouldPostValidAndRejectInvalid() throws Exception {
        setupValidScenario();

        // Add a second transaction with invalid card
        DailyTransaction dtBad = new DailyTransaction();
        dtBad.setTranId("0000000000000003");
        dtBad.setTranTypeCd("SA");
        dtBad.setTranCatCd(5001);
        dtBad.setTranSource("ONLINE");
        dtBad.setTranDesc("Bad card transaction");
        dtBad.setTranAmt(new BigDecimal("75.00"));
        dtBad.setTranMerchantId(987654321L);
        dtBad.setTranMerchantName("Another Merchant");
        dtBad.setTranMerchantCity("Another City");
        dtBad.setTranMerchantZip("54321");
        dtBad.setTranCardNum("0000000000000000");
        dtBad.setTranOrigTs("2025-06-15-11.00.00.000000");
        dailyTransactionRepository.save(dtBad);

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // One posted, one rejected
        assertEquals(1, transactionRepository.count());
        assertEquals(1, transactionRejectRepository.count());
    }

    @Test
    void emptyInput_shouldCompleteSuccessfully() throws Exception {
        // No daily transactions loaded — should complete with no output
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(0, transactionRepository.count());
        assertEquals(0, transactionRejectRepository.count());
    }
}
