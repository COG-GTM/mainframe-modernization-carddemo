package com.carddemo.batch.orchestration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Orchestration {@link Job} beans — each faithfully sequences the ordered steps of a legacy JCL
 * pipeline, reusing the already-built CS-10/11/12 steps for the financial work and the
 * {@link OrchestrationSteps} glue steps for the mainframe-utility steps.
 *
 * <p>The reused step beans ({@code intcalcStep}, {@code postTranStep},
 * {@code transactionReportStep}, {@code createStatementStep}) are injected by name; Spring Batch
 * allows a step to participate in more than one job, so the CS-10/11/12 jobs and these pipeline
 * jobs share the same step instances.</p>
 */
@Configuration
@EnableConfigurationProperties(BatchOrchestrationProperties.class)
public class BatchPipelineConfig {

    private final JobRepository jobRepository;

    public BatchPipelineConfig(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * POSTTRAN daily pipeline (POSTTRAN.jcl + supporting TRANBKP/DALYREJS/TRANREPT):
     * <pre>
     *   backup (TRANBKP REPRO) → define rejects (DALYREJS) → post (CBTRN02C / postTranStep)
     *       → reject handling → report (CBTRN03C / transactionReportStep)
     * </pre>
     */
    @Bean
    public Job postTranPipelineJob(@Qualifier("backupTransactionsStep") Step backupTransactionsStep,
                                   @Qualifier("defineRejectsStep") Step defineRejectsStep,
                                   @Qualifier("postTranStep") Step postTranStep,
                                   @Qualifier("handleRejectsStep") Step handleRejectsStep,
                                   @Qualifier("transactionReportStep") Step transactionReportStep) {
        return new JobBuilder("postTranPipelineJob", jobRepository)
                .start(backupTransactionsStep)
                .next(defineRejectsStep)
                .next(postTranStep)
                .next(handleRejectsStep)
                .next(transactionReportStep)
                .build();
    }

    /**
     * INTCALC monthly pipeline (INTCALC.jcl + COMBTRAN.jcl):
     * <pre>
     *   interest (CBACT04C / intcalcStep) → combine system transactions (COMBTRAN SORT+REPRO)
     * </pre>
     */
    @Bean
    public Job intcalcPipelineJob(@Qualifier("intcalcStep") Step intcalcStep,
                                  @Qualifier("combineTransactionsStep") Step combineTransactionsStep) {
        return new JobBuilder("intcalcPipelineJob", jobRepository)
                .start(intcalcStep)
                .next(combineTransactionsStep)
                .build();
    }

    /**
     * CREASTMT monthly statement pipeline (CREASTMT.JCL):
     * <pre>
     *   prepare files (IDCAMS DELETE/DEFINE + IEFBR14) → statements (CBSTM03A / createStatementStep)
     * </pre>
     * The intermediate SORT/REPRO copy of TRANSACT into the {@code TRXFL} work KSDS is not needed —
     * the CS-12 reader drives directly off the JPA repositories.
     */
    @Bean
    public Job createStatementPipelineJob(@Qualifier("initStatementFilesStep") Step initStatementFilesStep,
                                          @Qualifier("createStatementStep") Step createStatementStep) {
        return new JobBuilder("createStatementPipelineJob", jobRepository)
                .start(initStatementFilesStep)
                .next(createStatementStep)
                .build();
    }

    /**
     * TRANREPT stand-alone report pipeline (TRANREPT.jcl / TRANREPT.prc):
     * <pre>
     *   unload/backup (REPRO) → report (CBTRN03C / transactionReportStep)
     * </pre>
     * The SORT (filter by date + sort by card) is folded into {@code transactionReportStep}'s
     * date-range filtering.
     */
    @Bean
    public Job transactionReportPipelineJob(@Qualifier("backupTransactionsStep") Step backupTransactionsStep,
                                            @Qualifier("transactionReportStep") Step transactionReportStep) {
        return new JobBuilder("transactionReportPipelineJob", jobRepository)
                .start(backupTransactionsStep)
                .next(transactionReportStep)
                .build();
    }

    /** PRTCATBL pipeline (PRTCATBL.jcl): sorted unload + formatted print of TCATBAL. */
    @Bean
    public Job printCategoryBalancePipelineJob(
            @Qualifier("printCategoryBalanceStep") Step printCategoryBalanceStep) {
        return new JobBuilder("printCategoryBalancePipelineJob", jobRepository)
                .start(printCategoryBalanceStep)
                .build();
    }
}
