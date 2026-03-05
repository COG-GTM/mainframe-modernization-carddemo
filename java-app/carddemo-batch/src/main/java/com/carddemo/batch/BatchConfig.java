package com.carddemo.batch;

import com.carddemo.batch.interest.InterestCalculationProcessor;
import com.carddemo.batch.posttran.TransactionPostingProcessor;
import com.carddemo.batch.posttran.TransactionPostingResult;
import com.carddemo.batch.posttran.TransactionPostingWriter;
import com.carddemo.batch.statement.StatementData;
import com.carddemo.batch.statement.StatementGenerationProcessor;
import com.carddemo.batch.statement.StatementWriter;
import com.carddemo.entity.Account;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.*;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.Map;

/**
 * Spring Batch configuration for all batch jobs.
 * Replaces JCL job definitions: POSTTRAN.jcl, INTCALC.jcl, CREASTMT.JCL, TRANREPT.jcl
 */
@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Value("${carddemo.batch.statement-output-dir:./statements}")
    private String statementOutputDir;

    // ==================== Phase 9: Post Transactions Job ====================

    @Bean
    public JpaPagingItemReader<DailyTransaction> dailyTransactionReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.transactionId")
                .pageSize(100)
                .build();
    }

    @Bean
    public TransactionPostingProcessor transactionPostingProcessor(
            CardXrefRepository cardXrefRepo,
            AccountRepository accountRepo,
            TransactionRepository transactionRepo,
            TransactionCategoryBalanceRepository tcatBalRepo) {
        return new TransactionPostingProcessor(cardXrefRepo, accountRepo, transactionRepo, tcatBalRepo);
    }

    @Bean
    public TransactionPostingWriter transactionPostingWriter(
            TransactionRepository transactionRepo,
            DailyTransactionRejectRepository rejectRepo) {
        return new TransactionPostingWriter(transactionRepo, rejectRepo);
    }

    @Bean
    public Step postTransactionsStep(JobRepository jobRepository,
                                      PlatformTransactionManager txManager,
                                      JpaPagingItemReader<DailyTransaction> reader,
                                      TransactionPostingProcessor processor,
                                      TransactionPostingWriter writer) {
        return new StepBuilder("postTransactionsStep", jobRepository)
                .<DailyTransaction, TransactionPostingResult>chunk(100, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job postTransactionsJob(JobRepository jobRepository, Step postTransactionsStep) {
        return new JobBuilder("postTransactionsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(postTransactionsStep)
                .build();
    }

    // ==================== Phase 10: Interest Calculation Job ====================

    @Bean
    public JpaPagingItemReader<TransactionCategoryBalance> tcatBalReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<TransactionCategoryBalance>()
                .name("tcatBalReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM TransactionCategoryBalance t ORDER BY t.id.acctId, t.id.tranTypeCd, t.id.tranCatCd")
                .pageSize(100)
                .build();
    }

    @Bean
    public InterestCalculationProcessor interestCalculationProcessor(
            AccountRepository accountRepo,
            DisclosureGroupRepository discGrpRepo) {
        return new InterestCalculationProcessor(accountRepo, discGrpRepo);
    }

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                         PlatformTransactionManager txManager,
                                         JpaPagingItemReader<TransactionCategoryBalance> tcatBalReader,
                                         InterestCalculationProcessor processor,
                                         TransactionRepository transactionRepo) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .<TransactionCategoryBalance, Transaction>chunk(100, txManager)
                .reader(tcatBalReader)
                .processor(processor)
                .writer(chunk -> {
                    for (Transaction t : chunk) {
                        if (t != null) {
                            transactionRepo.save(t);
                        }
                    }
                })
                .build();
    }

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(interestCalculationStep)
                .build();
    }

    // ==================== Phase 11: Statement Generation Job ====================

    @Bean
    public JpaPagingItemReader<Account> accountReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Account>()
                .name("accountReader")
                .entityManagerFactory(emf)
                .queryString("SELECT a FROM Account a WHERE a.activeStatus = 'Y' ORDER BY a.acctId")
                .pageSize(50)
                .build();
    }

    @Bean
    public StatementGenerationProcessor statementGenerationProcessor(
            TransactionRepository transactionRepo,
            CardXrefRepository cardXrefRepo,
            CustomerRepository customerRepo) {
        return new StatementGenerationProcessor(transactionRepo, cardXrefRepo, customerRepo);
    }

    @Bean
    public StatementWriter statementWriter() {
        return new StatementWriter(statementOutputDir);
    }

    @Bean
    public Step statementGenerationStep(JobRepository jobRepository,
                                         PlatformTransactionManager txManager,
                                         JpaPagingItemReader<Account> accountReader,
                                         StatementGenerationProcessor processor,
                                         StatementWriter writer) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .<Account, StatementData>chunk(50, txManager)
                .reader(accountReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job statementGenerationJob(JobRepository jobRepository, Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(statementGenerationStep)
                .build();
    }

    // ==================== Phase 12: Transaction Report Job ====================

    @Bean
    public Job transactionReportJob(JobRepository jobRepository,
                                     Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(transactionReportStep)
                .build();
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                       PlatformTransactionManager txManager,
                                       EntityManagerFactory emf,
                                       TransactionRepository transactionRepo) {
        JpaPagingItemReader<Transaction> reader = new JpaPagingItemReaderBuilder<Transaction>()
                .name("transactionReportReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM Transaction t ORDER BY t.origTimestamp")
                .pageSize(100)
                .build();

        return new StepBuilder("transactionReportStep", jobRepository)
                .<Transaction, String>chunk(100, txManager)
                .reader(reader)
                .processor(item -> String.format("%-16s %-16s %-2s %12.2f %s",
                        item.getTransactionId(),
                        item.getCardNum() != null ? item.getCardNum() : "",
                        item.getTypeCd() != null ? item.getTypeCd() : "",
                        item.getAmount(),
                        item.getOrigTimestamp() != null ? item.getOrigTimestamp().toString() : ""))
                .writer(chunk -> {
                    // Report lines are written; in production this would go to a file
                    for (String line : chunk) {
                        log.info("REPORT: {}", line);
                    }
                })
                .build();
    }

    // ==================== Phase 13: Nightly Cycle Orchestration ====================
    // CLOSEFIL and OPENFIL (VSAM lock/unlock) are NOT NEEDED — RDBMS handles concurrency.
    // Sequence: postTransactions → interestCalculation → statementGeneration → transactionReport

    @Bean
    public Job nightlyCycleJob(JobRepository jobRepository,
                                Step postTransactionsStep,
                                Step interestCalculationStep,
                                Step statementGenerationStep,
                                Step transactionReportStep) {
        return new JobBuilder("nightlyCycleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(postTransactionsStep)
                .next(interestCalculationStep)
                .next(statementGenerationStep)
                .next(transactionReportStep)
                .build();
    }
}
