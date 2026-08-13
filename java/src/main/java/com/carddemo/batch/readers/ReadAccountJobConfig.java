package com.carddemo.batch.readers;

import com.carddemo.model.entity.Account;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * COBOL program: CBACT01C — read and print the account master file, driven by
 * {@code app/jcl/READACCT.jcl} (STEP05, ACCTFILE = ACCTDATA VSAM KSDS).
 * Copybook CVACT01Y; records are read in ACCT-ID order, as the KSDS sequential read does.
 */
@Configuration
public class ReadAccountJobConfig {

    public static final String JOB_NAME = "readAccountJob";
    static final String PROGRAM = "CBACT01C";

    @Bean
    JpaPagingItemReader<Account> accountMasterReader(EntityManagerFactory entityManagerFactory) {
        return VsamReaderJobSupport.reader("accountMasterReader", entityManagerFactory,
                "select a from Account a order by a.accountId");
    }

    @Bean
    ItemWriter<Account> accountRecordWriter() {
        return VsamReaderJobSupport.displayWriter(PROGRAM, Cbact01cDisplay::displayLines);
    }

    @Bean
    Step readAccountStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager,
                         JpaPagingItemReader<Account> accountMasterReader,
                         ItemWriter<Account> accountRecordWriter) {
        return new StepBuilder("readAccountStep", jobRepository)
                .<Account, Account>chunk(100, transactionManager)
                .reader(accountMasterReader)
                .writer(accountRecordWriter)
                .build();
    }

    @Bean
    Job readAccountJob(JobRepository jobRepository, Step readAccountStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(VsamReaderJobSupport.executionListener(PROGRAM, "ERROR READING ACCOUNT FILE"))
                .start(readAccountStep)
                .build();
    }
}
