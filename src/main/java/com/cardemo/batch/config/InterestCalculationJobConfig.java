package com.cardemo.batch.config;

import com.cardemo.batch.job.InterestCalculationTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job configuration for the interest calculation batch.
 * Defines the job and step that correspond to the CBACT04C COBOL batch program.
 */
@Configuration
public class InterestCalculationJobConfig {

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }

    @Bean
    public Step interestCalculationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            InterestCalculationTasklet interestCalculationTasklet) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .tasklet(interestCalculationTasklet, transactionManager)
                .build();
    }
}
