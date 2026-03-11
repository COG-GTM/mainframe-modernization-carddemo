package com.carddemo.batch;

import com.carddemo.service.TransactionPostingService;
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
public class TransactionPostingJobConfig {
    @Bean
    public Job transactionPostingJob(JobRepository jobRepository, Step postTransactionsStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
            .start(postTransactionsStep)
            .build();
    }

    @Bean
    public Step postTransactionsStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                      TransactionPostingService service) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            service.postDailyTransactions();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("postTransactionsStep", jobRepository)
            .tasklet(tasklet, txManager)
            .build();
    }
}
