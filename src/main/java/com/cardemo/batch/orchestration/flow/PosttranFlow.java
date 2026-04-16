package com.cardemo.batch.orchestration.flow;

import com.cardemo.batch.orchestration.model.JobFlowResult;
import com.cardemo.batch.orchestration.model.ReturnCode;
import com.cardemo.batch.orchestration.service.JobLaunchService;
import com.cardemo.batch.orchestration.service.ReturnCodeEvaluator;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;

/**
 * Orchestrates the POSTTRAN job flow.
 *
 * <p>JCL equivalent: POSTTRAN.jcl
 * <ul>
 *   <li>Step 1: CBTRN01C — Read daily transactions</li>
 *   <li>Step 2: CBTRN02C — Validate and post transactions</li>
 * </ul>
 *
 * <p>Conditional execution rules:
 * <ul>
 *   <li>If CBTRN02C returns RETURN-CODE=4 (rejections exist), log warning but continue</li>
 *   <li>If RETURN-CODE > 4, fail the job</li>
 * </ul>
 */
@Component
public class PosttranFlow {

    private static final Logger log = LoggerFactory.getLogger(PosttranFlow.class);
    private static final String FLOW_NAME = "POSTTRAN";

    private final JobLaunchService jobLaunchService;
    private final ReturnCodeEvaluator returnCodeEvaluator;
    private final MeterRegistry meterRegistry;

    public PosttranFlow(JobLaunchService jobLaunchService,
                        ReturnCodeEvaluator returnCodeEvaluator,
                        MeterRegistry meterRegistry) {
        this.jobLaunchService = jobLaunchService;
        this.returnCodeEvaluator = returnCodeEvaluator;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Executes the POSTTRAN flow with a combined job containing both steps.
     * The transactionPostingJob from PR #84 contains the read + validate/post logic.
     */
    public JobFlowResult execute(Job transactionPostingJob) {
        JobFlowResult result = new JobFlowResult(FLOW_NAME);
        log.info("*** POSTTRAN JOB FLOW START ***");

        try {
            Instant stepStart = Instant.now();
            JobExecution execution = jobLaunchService.launchJob(transactionPostingJob);

            ReturnCode jobReturnCode = returnCodeEvaluator.evaluate(execution);
            result.addStepResult(new JobFlowResult.StepResult(
                    "CBTRN02C-POST", jobReturnCode, stepStart, Instant.now()));

            if (jobReturnCode.isWarning()) {
                log.warn("POSTTRAN: Transaction posting completed with rejections (RC=4). "
                        + "Logging warning but continuing.");
                handleWarningRejects(execution);
            } else if (jobReturnCode.isError()) {
                log.error("POSTTRAN: Transaction posting FAILED (RC={}). Stopping flow.",
                        jobReturnCode.getCode());
            } else {
                log.info("POSTTRAN: Transaction posting completed successfully (RC=0).");
            }

        } catch (Exception e) {
            log.error("POSTTRAN: Unexpected error during flow execution", e);
            result.addStepResult(new JobFlowResult.StepResult(
                    "CBTRN02C-POST",
                    new ReturnCode(ReturnCode.ERROR, "Exception: " + e.getMessage()),
                    Instant.now(), Instant.now()));
        }

        result.complete();
        logFlowSummary(result);
        return result;
    }

    private void handleWarningRejects(JobExecution execution) {
        Collection<StepExecution> stepExecutions = execution.getStepExecutions();
        for (StepExecution step : stepExecutions) {
            long readCount = step.getReadCount();
            long writeCount = step.getWriteCount();
            long skipCount = step.getSkipCount();
            log.warn("  Step '{}': read={}, written={}, skipped={}",
                    step.getStepName(), readCount, writeCount, skipCount);
        }
    }

    private void logFlowSummary(JobFlowResult result) {
        log.info("*** POSTTRAN JOB FLOW END ***");
        log.info("  Overall RC: {}", result.getOverallReturnCode().getCode());
        log.info("  Successful: {}", result.isSuccessful());
        for (JobFlowResult.StepResult stepResult : result.getStepResults()) {
            log.info("  Step '{}': RC={}", stepResult.getStepName(),
                    stepResult.getReturnCode().getCode());
        }
    }
}
