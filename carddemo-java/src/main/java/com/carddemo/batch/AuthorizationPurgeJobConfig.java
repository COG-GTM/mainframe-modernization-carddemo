package com.carddemo.batch;

import com.carddemo.repository.AuthSummaryRepository;
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
public class AuthorizationPurgeJobConfig {
    @Bean
    public Job authorizationPurgeJob(JobRepository jobRepository, Step purgeAuthStep) {
        return new JobBuilder("authorizationPurgeJob", jobRepository)
            .start(purgeAuthStep)
            .build();
    }

    @Bean
    public Step purgeAuthStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                               AuthSummaryRepository authSummaryRepository) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            long count = authSummaryRepository.count();
            authSummaryRepository.deleteAll();
            chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext().putLong("purgedRecords", count);
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("purgeAuthStep", jobRepository)
            .tasklet(tasklet, txManager)
            .build();
    }
}
