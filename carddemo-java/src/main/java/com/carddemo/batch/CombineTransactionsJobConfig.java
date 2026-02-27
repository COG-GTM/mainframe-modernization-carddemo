package com.carddemo.batch;

import com.carddemo.entity.Transaction;
import com.carddemo.repository.TransactionRepository;
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
import java.util.Comparator;
import java.util.List;

@Configuration
public class CombineTransactionsJobConfig {
    @Bean
    public Job combineTransactionsJob(JobRepository jobRepository, Step combineStep) {
        return new JobBuilder("combineTransactionsJob", jobRepository)
            .start(combineStep)
            .build();
    }

    @Bean
    public Step combineStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                             TransactionRepository transactionRepository) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            List<Transaction> sorted = transactionRepository.findAll().stream()
                .sorted(Comparator.comparing((Transaction t) -> t.getTranCardNum() != null ? t.getTranCardNum() : "")
                    .thenComparing(t -> t.getTranOrigTs() != null ? t.getTranOrigTs() : ""))
                .toList();
            chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext().putInt("sortedCount", sorted.size());
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("combineStep", jobRepository)
            .tasklet(tasklet, txManager)
            .build();
    }
}
