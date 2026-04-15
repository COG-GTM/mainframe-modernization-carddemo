package com.carddemo.batch.config;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.processor.AccountClassificationProcessor;
import com.carddemo.batch.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * Configuration for the Account Processing batch job.
 * Migrated from COBOL program CBACT01C.
 *
 * Reads all accounts, classifies them into active/inactive,
 * and generates summary counts and totals per group.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AccountProcessingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountClassificationProcessor accountClassificationProcessor;
    private final AccountRepository accountRepository;

    @Value("${batch.chunk-size:10}")
    private int chunkSize;

    @Bean
    public Job accountProcessingJob() {
        return new JobBuilder("accountProcessingJob", jobRepository)
                .start(classificationStep())
                .next(summaryStep())
                .build();
    }

    @Bean
    public Step classificationStep() {
        return new StepBuilder("classificationStep", jobRepository)
                .<Account, Account>chunk(chunkSize, transactionManager)
                .reader(accountReaderForClassification())
                .processor(accountClassificationProcessor)
                .writer(chunk -> { /* no-op writer; classification is tracked in processor */ })
                .build();
    }

    @Bean
    public Step summaryStep() {
        return new StepBuilder("summaryStep", jobRepository)
                .tasklet(summaryTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet summaryTasklet() {
        return (StepContribution contribution, ChunkContext chunkContext) -> {
            String summary = accountClassificationProcessor.getSummary();
            log.info(summary);
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .putString("classificationSummary", summary);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public RepositoryItemReader<Account> accountReaderForClassification() {
        return new RepositoryItemReaderBuilder<Account>()
                .name("accountReaderForClassification")
                .repository(accountRepository)
                .methodName("findAll")
                .sorts(Map.of("acctId", Sort.Direction.ASC))
                .build();
    }
}
