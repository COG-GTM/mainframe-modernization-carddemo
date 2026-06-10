package com.carddemo.batch;

import com.carddemo.entity.DailyTransaction;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job that posts daily transactions, equivalent to the CBTRN02C batch program.
 * Reads DailyTransaction rows, processes each through the posting service, and tallies results.
 */
@Configuration
public class BatchConfig {

    public static final String JOB_NAME = "transactionPostingJob";
    public static final String STEP_NAME = "transactionPostingStep";

    @Bean
    public JpaPagingItemReader<DailyTransaction> dailyTransactionReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.id")
                .pageSize(100)
                .build();
    }

    @Bean
    public Step transactionPostingStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       JpaPagingItemReader<DailyTransaction> dailyTransactionReader,
                                       TransactionItemProcessor processor,
                                       TransactionItemWriter writer) {
        // chunk size 1 -> one commit per record, matching the COBOL per-record posting
        // semantics (a single bad record cannot roll back others in the same chunk).
        return new StepBuilder(STEP_NAME, jobRepository)
                .<DailyTransaction, ProcessedTransaction>chunk(1, transactionManager)
                .reader(dailyTransactionReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository, Step transactionPostingStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(transactionPostingStep)
                .build();
    }
}
