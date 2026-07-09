package com.carddemo.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.domain.Account;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.SecurityUserRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionTypeRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

/**
 * Runs {@code dataLoadJob} end-to-end against H2 and asserts each table's row count matches
 * the number of records in the corresponding ASCII seed file, plus that a sample monetary
 * field parses to the expected {@link BigDecimal}.
 */
@SpringBootTest
@ActiveProfiles("test")
class SeedDataLoadJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job dataLoadJob;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;
    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;
    @Autowired
    private TransactionCategoryBalanceRepository tranCatBalanceRepository;
    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;
    @Autowired
    private TransactionTypeRepository transactionTypeRepository;
    @Autowired
    private SecurityUserRepository securityUserRepository;

    private static long recordCount(String resource) throws Exception {
        long count = 0;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new ClassPathResource(resource).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void loadsAllSeedFilesWithMatchingRowCounts() throws Exception {
        JobExecution execution = jobLauncher.run(dataLoadJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(customerRepository.count()).isEqualTo(recordCount("seed/custdata.txt"));
        assertThat(accountRepository.count()).isEqualTo(recordCount("seed/acctdata.txt"));
        assertThat(cardRepository.count()).isEqualTo(recordCount("seed/carddata.txt"));
        assertThat(cardXrefRepository.count()).isEqualTo(recordCount("seed/cardxref.txt"));
        assertThat(transactionTypeRepository.count()).isEqualTo(recordCount("seed/trantype.txt"));
        assertThat(transactionCategoryRepository.count()).isEqualTo(recordCount("seed/trancatg.txt"));
        assertThat(disclosureGroupRepository.count()).isEqualTo(recordCount("seed/discgrp.txt"));
        assertThat(tranCatBalanceRepository.count()).isEqualTo(recordCount("seed/tcatbal.txt"));
        assertThat(dailyTransactionRepository.count()).isEqualTo(recordCount("seed/dailytran.txt"));
        assertThat(securityUserRepository.count()).isEqualTo(recordCount("seed/usrsec.txt"));

        // Sample monetary field: account 00000000001 current balance "00000001940{" -> 194.00
        Account first = accountRepository.findById("00000000001").orElseThrow();
        assertThat(first.getAcctCurrBal()).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(first.getAcctCurrBal().scale()).isEqualTo(2);
        assertThat(first.getAcctCreditLimit()).isEqualByComparingTo(new BigDecimal("2020.00"));
    }
}
