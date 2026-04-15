package com.carddemo.batch.config;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.processor.InterestCalculationProcessor;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.writer.AccountBalanceWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * Configuration for the Interest Calculation batch job.
 * Migrated from COBOL program CBACT04C.
 *
 * Reads all accounts, calculates monthly interest for active accounts
 * with positive balances, updates account balances, and creates
 * interest transaction records.
 */
@Configuration
@RequiredArgsConstructor
public class InterestCalculationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InterestCalculationProcessor interestCalculationProcessor;
    private final AccountBalanceWriter accountBalanceWriter;
    private final AccountRepository accountRepository;

    @Value("${batch.chunk-size:10}")
    private int chunkSize;

    @Bean
    public Job interestCalculationJob() {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep())
                .build();
    }

    @Bean
    public Step interestCalculationStep() {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .<Account, Account>chunk(chunkSize, transactionManager)
                .reader(accountReaderForInterest())
                .processor(interestCalculationProcessor)
                .writer(accountBalanceWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<Account> accountReaderForInterest() {
        return new RepositoryItemReaderBuilder<Account>()
                .name("accountReaderForInterest")
                .repository(accountRepository)
                .methodName("findAll")
                .sorts(Map.of("acctId", Sort.Direction.ASC))
                .build();
    }
}
