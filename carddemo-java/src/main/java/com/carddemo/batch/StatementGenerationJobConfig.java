package com.carddemo.batch;

import com.carddemo.service.StatementGenerationService;
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
public class StatementGenerationJobConfig {
    @Bean
    public Job statementGenerationJob(JobRepository jobRepository, Step generateStatementsStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
            .start(generateStatementsStep)
            .build();
    }

    @Bean
    public Step generateStatementsStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                        StatementGenerationService service) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            var statements = service.generateStatements();
            chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext().putInt("statementsGenerated", statements.size());
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("generateStatementsStep", jobRepository)
            .tasklet(tasklet, txManager)
            .build();
    }
}
