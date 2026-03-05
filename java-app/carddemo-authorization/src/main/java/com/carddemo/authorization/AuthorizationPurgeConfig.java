package com.carddemo.authorization;

import com.carddemo.entity.AuthorizationSummary;
import com.carddemo.repository.AuthorizationSummaryRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Batch configuration for authorization purge job.
 * Translates CBPAUP0C: purge expired authorizations older than a configurable threshold.
 */
@Configuration
public class AuthorizationPurgeConfig {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationPurgeConfig.class);

    @Value("${carddemo.batch.auth-purge-days:90}")
    private int purgeDays;

    @Bean
    public Job authorizationPurgeJob(JobRepository jobRepository, Step authorizationPurgeStep) {
        return new JobBuilder("authorizationPurgeJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(authorizationPurgeStep)
                .build();
    }

    @Bean
    public Step authorizationPurgeStep(JobRepository jobRepository,
                                        PlatformTransactionManager txManager,
                                        EntityManagerFactory emf,
                                        AuthorizationSummaryRepository authSummaryRepo) {
        JpaPagingItemReader<AuthorizationSummary> reader = new JpaPagingItemReaderBuilder<AuthorizationSummary>()
                .name("authPurgeReader")
                .entityManagerFactory(emf)
                .queryString("SELECT a FROM AuthorizationSummary a WHERE a.authTimestamp < CURRENT_TIMESTAMP - " + purgeDays + " DAY")
                .pageSize(100)
                .build();

        return new StepBuilder("authorizationPurgeStep", jobRepository)
                .<AuthorizationSummary, AuthorizationSummary>chunk(100, txManager)
                .reader(reader)
                .writer(chunk -> {
                    for (AuthorizationSummary summary : chunk) {
                        authSummaryRepo.delete(summary);
                        log.debug("Purged authorization: {}", summary.getAuthId());
                    }
                })
                .build();
    }
}
