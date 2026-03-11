package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import com.carddemo.service.InterestCalculationService;
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
public class InterestCalculationJobConfig {
    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step calculateInterestStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
            .start(calculateInterestStep)
            .build();
    }

    @Bean
    public Step calculateInterestStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                       InterestCalculationService service, AccountRepository accountRepository) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            for (Account account : accountRepository.findAll()) {
                service.calculateInterestForAccount(account.getAcctId());
            }
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("calculateInterestStep", jobRepository)
            .tasklet(tasklet, txManager)
            .build();
    }
}
