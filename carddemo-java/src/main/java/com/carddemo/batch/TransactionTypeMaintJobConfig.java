package com.carddemo.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionTypeMaintJobConfig {
    @Bean
    public Job transactionTypeMaintJob(JobRepository jobRepository, Step maintStep) {
        return new JobBuilder("transactionTypeMaintJob", jobRepository)
            .start(maintStep)
            .build();
    }

    @Bean
    public Step maintStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("maintStep", jobRepository)
            .tasklet(tasklet, txManager)
            .build();
    }
}
