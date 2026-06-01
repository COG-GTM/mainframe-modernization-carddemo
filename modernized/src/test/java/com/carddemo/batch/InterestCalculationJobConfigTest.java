package com.carddemo.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.DisclosureGroup;
import com.carddemo.model.TranCatBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * End-to-end test of the Spring Batch job that replaces INTCALC.jcl. Batch auto-run on startup is
 * disabled; the job is launched explicitly with a {@code processingDate} parameter.
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
class InterestCalculationJobConfigTest {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job interestCalculationJob;

    @Autowired
    private TranCatBalanceRepository tranCatBalanceRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void seed() {
        transactionRepository.deleteAll();
        tranCatBalanceRepository.deleteAll();
        disclosureGroupRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();

        Account a = new Account();
        a.setAcctId(1L);
        a.setGroupId("A000000000");
        a.setCurrBal(new BigDecimal("1000.00"));
        accountRepository.save(a);
        cardXrefRepository.save(new CardXref("0000000000000001", 1L, 1L));
        disclosureGroupRepository.save(new DisclosureGroup("A000000000", "01", 1, new BigDecimal("12.00")));
        tranCatBalanceRepository.save(new TranCatBalance(1L, "01", 1, new BigDecimal("1000.00")));
    }

    @Test
    void jobRunsAndWritesInterest() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString(InterestCalculationJobConfig.PARAM_PROCESSING_DATE, "2022071800")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(interestCalculationJob, params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(transactionRepository.findAll()).hasSize(1);
        assertThat(accountRepository.findById(1L).orElseThrow().getCurrBal()).isEqualByComparingTo("1010.00");
    }
}
