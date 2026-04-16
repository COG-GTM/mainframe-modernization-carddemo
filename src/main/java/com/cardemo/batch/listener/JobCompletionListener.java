package com.cardemo.batch.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * Listener that sets exit code = 4 when rejections exist.
 * Maps to CBTRN02C: IF WS-REJECT-COUNT > 0 MOVE 4 TO RETURN-CODE.
 */
public class JobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);

    private final MeterRegistry meterRegistry;

    public JobCompletionListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("START OF EXECUTION OF PROGRAM CBTRN02C");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Counter rejectCounter = meterRegistry.find("cardemo.batch.transactions.rejected")
                .counter();
        Counter totalCounter = meterRegistry.find("cardemo.batch.transactions.total")
                .counter();

        double totalCount = totalCounter != null ? totalCounter.count() : 0;
        double rejectCount = rejectCounter != null ? rejectCounter.count() : 0;

        log.info("TRANSACTIONS PROCESSED :{}", (long) totalCount);
        log.info("TRANSACTIONS REJECTED  :{}", (long) rejectCount);

        if (rejectCount > 0) {
            jobExecution.setExitStatus(
                    new org.springframework.batch.core.ExitStatus("COMPLETED_WITH_REJECTS",
                            "Exit code 4: " + (long) rejectCount + " transactions rejected"));
            log.info("Job exit code set to 4 (rejections found)");
        }

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("END OF EXECUTION OF PROGRAM CBTRN02C");
        }
    }
}
