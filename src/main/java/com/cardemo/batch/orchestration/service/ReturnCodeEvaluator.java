package com.cardemo.batch.orchestration.service;

import com.cardemo.batch.orchestration.model.ReturnCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Service;

/**
 * Evaluates Spring Batch job/step exit statuses and maps them to JCL-style return codes.
 * Implements the JCL COND parameter logic for conditional step execution.
 *
 * <p>JCL COND parameter semantics:
 * <ul>
 *   <li>RETURN-CODE = 0: Success</li>
 *   <li>RETURN-CODE = 4: Warning (rejections exist) — log and continue</li>
 *   <li>RETURN-CODE > 4: Failure — stop the job</li>
 * </ul>
 */
@Service
public class ReturnCodeEvaluator {

    private static final Logger log = LoggerFactory.getLogger(ReturnCodeEvaluator.class);

    public static final String EXIT_CODE_COMPLETED_WITH_REJECTS = "COMPLETED_WITH_REJECTS";

    /**
     * Maps a Spring Batch StepExecution to a JCL-style return code.
     */
    public ReturnCode evaluate(StepExecution stepExecution) {
        if (stepExecution == null) {
            return new ReturnCode(ReturnCode.ERROR, "Step execution is null");
        }

        ExitStatus exitStatus = stepExecution.getExitStatus();
        String exitCode = exitStatus.getExitCode();

        if (ExitStatus.COMPLETED.getExitCode().equals(exitCode)) {
            return new ReturnCode(ReturnCode.SUCCESS);
        }

        if (EXIT_CODE_COMPLETED_WITH_REJECTS.equals(exitCode)) {
            log.warn("Step '{}' completed with rejections (RETURN-CODE=4): {}",
                    stepExecution.getStepName(), exitStatus.getExitDescription());
            return new ReturnCode(ReturnCode.WARNING, exitStatus.getExitDescription());
        }

        if (ExitStatus.FAILED.getExitCode().equals(exitCode)) {
            return new ReturnCode(ReturnCode.ERROR,
                    "Step failed: " + exitStatus.getExitDescription());
        }

        return new ReturnCode(ReturnCode.ERROR,
                "Unexpected exit status: " + exitCode);
    }

    /**
     * Maps a Spring Batch JobExecution to a JCL-style return code.
     */
    public ReturnCode evaluate(JobExecution jobExecution) {
        if (jobExecution == null) {
            return new ReturnCode(ReturnCode.ERROR, "Job execution is null");
        }

        ExitStatus exitStatus = jobExecution.getExitStatus();
        String exitCode = exitStatus.getExitCode();

        if (ExitStatus.COMPLETED.getExitCode().equals(exitCode)) {
            return new ReturnCode(ReturnCode.SUCCESS);
        }

        if (EXIT_CODE_COMPLETED_WITH_REJECTS.equals(exitCode)) {
            return new ReturnCode(ReturnCode.WARNING, exitStatus.getExitDescription());
        }

        return new ReturnCode(ReturnCode.ERROR,
                "Job ended with status: " + exitCode + " - " + exitStatus.getExitDescription());
    }

    /**
     * Determines whether the next step should execute based on the previous step's return code.
     * Implements JCL COND parameter: if RETURN-CODE > threshold, skip (fail).
     *
     * @param previousReturnCode the return code from the previous step
     * @param threshold          the maximum acceptable return code (typically 4)
     * @return true if the next step should execute
     */
    public boolean shouldContinue(ReturnCode previousReturnCode, int threshold) {
        boolean continueExecution = previousReturnCode.getCode() <= threshold;
        if (!continueExecution) {
            log.error("Stopping job flow: previous step returned {} which exceeds threshold {}",
                    previousReturnCode.getCode(), threshold);
        }
        return continueExecution;
    }
}
