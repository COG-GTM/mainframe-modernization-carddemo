package com.carddemo.batch.account;

import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch wiring for {@code intcalcJob} — the Java port of {@code CBACT04C} (INTCALC).
 *
 * <p>Single chunk-oriented step: {@link TransactionCategoryBalanceItemReader} (one account +
 * its {@code TCATBAL} rows per item) → {@link AccountInterestProcessor} (compute interest,
 * update the account, build interest transactions) → {@link AccountInterestWriter} (persist the
 * account and the transactions).</p>
 *
 * <p>The reader and processor are {@link StepScope}-scoped so each launch starts from a clean
 * state (fresh grouping and a fresh {@code WS-TRANID-SUFFIX} counter) and can bind the
 * {@code parmDate} job parameter that seeds the interest transaction ids.</p>
 */
@Configuration
public class InterestCalculationJobConfig {

    static final int CHUNK = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    public InterestCalculationJobConfig(JobRepository jobRepository, PlatformTransactionManager txManager) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
    }

    @Bean
    public Job intcalcJob(Step intcalcStep) {
        return new JobBuilder("intcalcJob", jobRepository)
                .start(intcalcStep)
                .build();
    }

    @Bean
    public Step intcalcStep(TransactionCategoryBalanceItemReader interestReader,
                            AccountInterestProcessor interestProcessor,
                            AccountInterestWriter interestWriter) {
        return new StepBuilder("intcalcStep", jobRepository)
                .<AccountInterestItem, AccountInterestResult>chunk(CHUNK, txManager)
                .reader(interestReader)
                .processor(interestProcessor)
                .writer(interestWriter)
                .build();
    }

    @Bean
    @StepScope
    public TransactionCategoryBalanceItemReader interestReader(TransactionCategoryBalanceRepository repository) {
        return new TransactionCategoryBalanceItemReader(repository);
    }

    @Bean
    @StepScope
    public AccountInterestProcessor interestProcessor(
            AccountRepository accountRepository,
            DisclosureGroupRepository disclosureGroupRepository,
            CardXrefRepository cardXrefRepository,
            @Value("#{jobParameters['parmDate'] ?: T(java.time.LocalDate).now().toString()}") String parmDate) {
        return new AccountInterestProcessor(accountRepository, disclosureGroupRepository,
                cardXrefRepository, parmDate);
    }

    @Bean
    public AccountInterestWriter interestWriter(AccountRepository accountRepository,
                                                TransactionRepository transactionRepository) {
        return new AccountInterestWriter(accountRepository, transactionRepository);
    }
}
