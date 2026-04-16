package com.cardemo.batch.orchestration.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Step-level listener that logs return code information for each step.
 * Provides visibility into the conditional execution logic that replaces
 * JCL COND parameter evaluation.
 */
@Component
public class StepReturnCodeListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(StepReturnCodeListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("  >> Starting step: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        ExitStatus exitStatus = stepExecution.getExitStatus();

        log.info("  << Step '{}' completed with exit status: {} (read={}, write={}, skip={})",
                stepName,
                exitStatus.getExitCode(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());

        if (stepExecution.getFailureExceptions() != null
                && !stepExecution.getFailureExceptions().isEmpty()) {
            for (Throwable t : stepExecution.getFailureExceptions()) {
                log.error("  Step '{}' failure: {}", stepName, t.getMessage());
            }
        }

        return exitStatus;
    }
}
