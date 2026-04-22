package com.carddemo.batch.config;

import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.processor.TransactionPostingProcessor;
import com.carddemo.batch.reader.DailyTransactionReader;
import com.carddemo.batch.writer.TransactionWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration for the Transaction Posting batch job.
 * Migrated from COBOL programs CBTRN01C + CBTRN02C.
 *
 * Reads daily transactions, validates cards, updates account balances,
 * updates category balance totals, and writes to the transaction table.
 */
@Configuration
@RequiredArgsConstructor
public class TransactionPostingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DailyTransactionReader dailyTransactionReader;
    private final TransactionPostingProcessor transactionPostingProcessor;
    private final TransactionWriter transactionWriter;

    @Value("${batch.chunk-size:10}")
    private int chunkSize;

    @Bean
    public Job transactionPostingJob() {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(transactionPostingStep())
                .build();
    }

    @Bean
    public Step transactionPostingStep() {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .<DailyTransaction, Transaction>chunk(chunkSize, transactionManager)
                .reader(dailyTransactionReader)
                .processor(transactionPostingProcessor)
                .writer(transactionWriter)
                .build();
    }
}
