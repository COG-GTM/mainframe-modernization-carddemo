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
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Orchestrates the INTCALC (Interest Calculation) job flow.
 *
 * <p>JCL equivalent: INTCALC.jcl
 * <ul>
 *   <li>Single step: CBACT04C — Compute interest for all accounts</li>
 *   <li>Takes PARM-DATE parameter (e.g., '2022071800')</li>
 *   <li>Generates interest transaction records</li>
 * </ul>
 *
 * <p>JCL: //STEP15 EXEC PGM=CBACT04C,PARM='2022071800'
 */
@Component
public class IntcalcFlow {

    private static final Logger log = LoggerFactory.getLogger(IntcalcFlow.class);
    private static final String FLOW_NAME = "INTCALC";

    private final JobLaunchService jobLaunchService;
    private final ReturnCodeEvaluator returnCodeEvaluator;
    private final MeterRegistry meterRegistry;

    public IntcalcFlow(JobLaunchService jobLaunchService,
                       ReturnCodeEvaluator returnCodeEvaluator,
                       MeterRegistry meterRegistry) {
        this.jobLaunchService = jobLaunchService;
        this.returnCodeEvaluator = returnCodeEvaluator;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Executes the INTCALC flow with a specific PARM-DATE.
     *
     * @param interestCalculationJob the Spring Batch job for interest calculation
     * @param parmDate              the date parameter in format 'yyyyMMddHH' (e.g., '2022071800')
     */
    public JobFlowResult execute(Job interestCalculationJob, String parmDate) {
        JobFlowResult result = new JobFlowResult(FLOW_NAME);
        log.info("*** INTCALC JOB FLOW START ***");
        log.info("  PARM-DATE: {}", parmDate);

        try {
            Instant stepStart = Instant.now();
            JobExecution execution = jobLaunchService.launchWithParmDate(
                    interestCalculationJob, parmDate);

            ReturnCode jobReturnCode = returnCodeEvaluator.evaluate(execution);
            result.addStepResult(new JobFlowResult.StepResult(
                    "CBACT04C-INTEREST", jobReturnCode, stepStart, Instant.now()));

            if (jobReturnCode.isError()) {
                log.error("INTCALC: Interest calculation FAILED (RC={}). "
                        + "Interest transaction records may be incomplete.",
                        jobReturnCode.getCode());
            } else {
                log.info("INTCALC: Interest calculation completed successfully (RC={}).",
                        jobReturnCode.getCode());
            }

        } catch (Exception e) {
            log.error("INTCALC: Unexpected error during flow execution", e);
            result.addStepResult(new JobFlowResult.StepResult(
                    "CBACT04C-INTEREST",
                    new ReturnCode(ReturnCode.ERROR, "Exception: " + e.getMessage()),
                    Instant.now(), Instant.now()));
        }

        result.complete();
        logFlowSummary(result);
        return result;
    }

    /**
     * Executes the INTCALC flow using the current date as PARM-DATE.
     */
    public JobFlowResult execute(Job interestCalculationJob) {
        String parmDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "00";
        return execute(interestCalculationJob, parmDate);
    }

    private void logFlowSummary(JobFlowResult result) {
        log.info("*** INTCALC JOB FLOW END ***");
        log.info("  Overall RC: {}", result.getOverallReturnCode().getCode());
        log.info("  Successful: {}", result.isSuccessful());
    }
}
