package com.carddemo;

import com.carddemo.batch.TransactionItemWriter;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRejectRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the full Spring Batch path end-to-end: launches {@code transactionPostingJob},
 * which uses the {@code JpaPagingItemReader}, processor, and the {@code @BeforeStep}-driven
 * writer wiring in {@link com.carddemo.batch.BatchConfig}.
 */
@SpringBootTest
@SpringBatchTest
class BatchJobIntegrationTest {

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
    private TranCatBalanceRepository tranCatBalanceRepository;
    @Autowired
    private TransactionRejectRepository transactionRejectRepository;

    @BeforeEach
    void setUp() {
        transactionRejectRepository.deleteAll();
        transactionRepository.deleteAll();
        tranCatBalanceRepository.deleteAll();
        accountRepository.deleteAll();
        cardXrefRepository.deleteAll();
        dailyTransactionRepository.deleteAll();

        cardXrefRepository.save(TestDataFactory.xref());
        accountRepository.save(TestDataFactory.account());
    }

    @Test
    void jobPostsValidAndRejectsInvalidTransactions() throws Exception {
        DailyTransaction valid = TestDataFactory.transaction("TXN0000000000100", new BigDecimal("40.00"));
        DailyTransaction invalid = TestDataFactory.transaction("TXN0000000000101", new BigDecimal("40.00"));
        invalid.setCardNum("0000000000000000"); // not in XREF -> reject 100
        dailyTransactionRepository.save(valid);
        dailyTransactionRepository.save(invalid);

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        StepExecution step = execution.getStepExecutions().iterator().next();
        assertEquals(2L, step.getExecutionContext().getLong(TransactionItemWriter.TRANSACTION_COUNT_KEY));
        assertEquals(1L, step.getExecutionContext().getLong(TransactionItemWriter.REJECT_COUNT_KEY));

        // Valid transaction posted; invalid one rejected.
        assertTrue(transactionRepository.findById("TXN0000000000100").isPresent());
        assertEquals(1, transactionRejectRepository.count());
        assertEquals(40.0,
                accountRepository.findByAcctId(TestDataFactory.ACCT_ID).orElseThrow()
                        .getCurrCycCredit().doubleValue());
    }
}
