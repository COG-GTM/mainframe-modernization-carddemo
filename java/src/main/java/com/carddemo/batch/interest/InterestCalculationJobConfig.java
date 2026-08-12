package com.carddemo.batch.interest;

import java.time.LocalDateTime;
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
 * COBOL job: {@code app/jcl/INTCALC.jcl} STEP15 (PGM=CBACT04C, PARM='2022071800').
 *
 * <p>Single-step Spring Batch job around {@link InterestCalculationProcessor}; the PARM value
 * is supplied as the {@code parmDate} job parameter and defaults to the JCL literal.
 */
@Configuration
public class InterestCalculationJobConfig {

    /** PARM value coded in INTCALC.jcl. */
    public static final String DEFAULT_PARM_DATE = "2022071800";

    public static final String JOB_NAME = "interestCalculationJob";

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJobConfig.class);

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder(JOB_NAME, jobRepository).start(interestCalculationStep).build();
    }

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        InterestCalculationProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            String parmDate = (String) chunkContext.getStepContext()
                    .getJobParameters().getOrDefault("parmDate", DEFAULT_PARM_DATE);
            InterestCalculationResult result = processor.run(parmDate, LocalDateTime.now());
            log.info("CBACT04C: read {} TCATBALF records, wrote {} interest transactions, "
                            + "updated {} accounts", result.recordsRead(),
                    result.transactionsWritten(), result.accountsUpdated());
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("interestCalculationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
