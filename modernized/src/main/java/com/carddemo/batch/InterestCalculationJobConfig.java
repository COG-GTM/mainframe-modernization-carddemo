package com.carddemo.batch;

import com.carddemo.service.InterestCalculationResult;
import com.carddemo.service.InterestCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Spring Batch job replacing {@code INTCALC.jcl} (which runs {@code EXEC PGM=CBACT04C,PARM='...'}).
 *
 * <p>The single tasklet step invokes {@link InterestCalculationService#calculateInterest(String)}
 * inside the step's transaction. Any {@link com.carddemo.service.InterestCalculationException}
 * propagates and rolls the transaction back — the modern equivalent of the COBOL
 * {@code 9999-ABEND-PROGRAM}.
 *
 * <p>The {@code processingDate} job parameter corresponds to the JCL {@code PARM='2022071800'}.
 */
@Configuration
public class InterestCalculationJobConfig {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJobConfig.class);

    public static final String JOB_NAME = "interestCalculationJob";
    public static final String PARAM_PROCESSING_DATE = "processingDate";

    @Bean
    public Tasklet interestCalculationTasklet(InterestCalculationService service) {
        return (contribution, chunkContext) -> {
            String processingDate = (String) chunkContext.getStepContext()
                    .getJobParameters().get(PARAM_PROCESSING_DATE);
            InterestCalculationResult result = service.calculateInterest(processingDate);
            log.info("Interest run complete: {} records read, {} transactions written, "
                            + "{} accounts updated, total interest {}",
                    result.recordsRead(), result.transactionsWritten(),
                    result.accountsUpdated(), result.totalInterest());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step interestCalculationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Tasklet interestCalculationTasklet) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .tasklet(interestCalculationTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(interestCalculationStep)
                .build();
    }
}
