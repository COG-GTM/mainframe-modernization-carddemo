package com.cardemo.batch.config;

import com.cardemo.batch.listener.JobCompletionListener;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.DailyTransactionEntity;
import com.cardemo.batch.model.TransactionPostingResult;
import com.cardemo.batch.processor.TransactionPostingProcessor;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.RejectedTransactionRepository;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.repository.TransactionRepository;
import com.cardemo.batch.service.AccountUpdateService;
import com.cardemo.batch.service.TimestampService;
import com.cardemo.batch.service.TransactionCategoryBalanceService;
import com.cardemo.batch.service.TransactionValidationService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

/**
 * Spring Batch configuration for the transaction posting job.
 * This replaces the main PROCEDURE DIVISION loop from CBTRN02C.
 */
@Configuration
public class BatchJobConfig {

    @Bean
    public ItemReader<DailyTransactionEntity> dailyTransactionReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<DailyTransactionEntity>()
                .name("dailyTransactionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT d FROM DailyTransactionEntity d ORDER BY d.id")
                .pageSize(100)
                .build();
    }

    @Bean
    public TransactionPostingProcessor transactionProcessor(
            TransactionValidationService validationService,
            AccountUpdateService accountUpdateService,
            TransactionCategoryBalanceService categoryBalanceService,
            TimestampService timestampService,
            MeterRegistry meterRegistry) {
        return new TransactionPostingProcessor(
                validationService, accountUpdateService, categoryBalanceService,
                timestampService, meterRegistry);
    }

    @Bean
    public TransactionPostingWriter transactionWriter(
            TransactionRepository transactionRepository,
            RejectedTransactionRepository rejectedTransactionRepository,
            AccountRepository accountRepository,
            TransactionCategoryBalanceRepository categoryBalanceRepository) {
        return new TransactionPostingWriter(
                transactionRepository, rejectedTransactionRepository,
                accountRepository, categoryBalanceRepository);
    }

    @Bean
    public ItemProcessor<DailyTransactionEntity, TransactionPostingResult> compositeProcessor(
            TransactionPostingProcessor transactionProcessor) {
        return entity -> {
            DailyTransaction domain = entity.toDomain();
            return transactionProcessor.process(domain);
        };
    }

    @Bean
    public Step transactionPostingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<DailyTransactionEntity> reader,
            ItemProcessor<DailyTransactionEntity, TransactionPostingResult> compositeProcessor,
            ItemWriter<TransactionPostingResult> writer) {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .<DailyTransactionEntity, TransactionPostingResult>chunk(10, transactionManager)
                .reader(reader)
                .processor(compositeProcessor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository,
                                      Step transactionPostingStep,
                                      MeterRegistry meterRegistry) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .listener(new JobCompletionListener(meterRegistry))
                .start(transactionPostingStep)
                .build();
    }
}
