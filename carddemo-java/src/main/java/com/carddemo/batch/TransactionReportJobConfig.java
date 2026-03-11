package com.carddemo.batch;

import com.carddemo.service.ReportService;
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
public class TransactionReportJobConfig {
    @Bean
    public Job transactionReportJob(JobRepository jobRepository, Step generateReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
            .start(generateReportStep)
            .build();
    }

    @Bean
    public Step generateReportStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                    ReportService reportService) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            reportService.generateReport();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("generateReportStep", jobRepository)
            .tasklet(tasklet, txManager)
            .build();
    }
}
