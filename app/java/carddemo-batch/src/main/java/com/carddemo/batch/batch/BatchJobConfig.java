package com.carddemo.batch.batch;

import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.service.TransactionPostingService;
import com.carddemo.batch.service.TransactionValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

/**
 * Spring Batch job configuration for daily transaction posting.
 * Reads from a flat file, validates/posts transactions, writes rejects.
 *
 * Matches the COBOL CBTRN02C.cbl processing flow:
 * - Read sequential DALYTRAN file
 * - Validate each transaction (XREF lookup, account lookup, credit limit, expiration)
 * - Post valid transactions (update TCATBAL, update Account, write Transaction)
 * - Write rejected transactions to DALYREJS file
 * - Set exit code 4 if any rejects (COMPLETED_WITH_REJECTS)
 */
@Configuration
public class BatchJobConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchJobConfig.class);

    @Value("${batch.input.file:input/daily-transactions.dat}")
    private String inputFilePath;

    @Value("${batch.reject.file:output/daily-rejects.dat}")
    private String rejectFilePath;

    @Bean
    public FlatFileItemReader<DailyTransaction> dailyTransactionReader() {
        return DailyTransactionReader.create(new PathResource(inputFilePath));
    }

    @Bean
    public RejectWriter rejectWriter() {
        return new RejectWriter(rejectFilePath);
    }

    @Bean
    public TransactionProcessor transactionProcessor(
            TransactionValidationService validationService,
            TransactionPostingService postingService,
            RejectWriter rejectWriter) {
        return new TransactionProcessor(validationService, postingService, rejectWriter);
    }

    @Bean
    public TransactionWriter transactionWriter() {
        return new TransactionWriter();
    }

    @Bean
    public Step postTransactionsStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<DailyTransaction> reader,
                                     TransactionProcessor processor,
                                     TransactionWriter writer,
                                     RejectWriter rejectWriter) {
        return new StepBuilder("postTransactionsStep", jobRepository)
                .<DailyTransaction, DailyTransaction>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        try {
                            rejectWriter.open();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to open reject writer", e);
                        }
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        try {
                            rejectWriter.close();
                        } catch (IOException e) {
                            log.error("Error closing reject writer", e);
                        }

                        int rejectCount = rejectWriter.getRejectCount();
                        log.info("TRANSACTIONS PROCESSED: {}",
                                stepExecution.getReadCount());
                        log.info("TRANSACTIONS REJECTED: {}", rejectCount);

                        if (rejectCount > 0) {
                            return new ExitStatus("COMPLETED_WITH_REJECTS");
                        }
                        return ExitStatus.COMPLETED;
                    }
                })
                .build();
    }

    @Bean
    public Job postDailyTransactionsJob(JobRepository jobRepository,
                                        Step postTransactionsStep) {
        return new JobBuilder("postDailyTransactionsJob", jobRepository)
                .start(postTransactionsStep)
                .build();
    }
}
