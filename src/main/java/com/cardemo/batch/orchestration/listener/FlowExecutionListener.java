package com.cardemo.batch.orchestration.listener;

import com.cardemo.batch.orchestration.model.ReturnCode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneOffset;

/**
 * Listener for batch job flow executions.
 * Logs flow lifecycle events and publishes metrics via Micrometer.
 */
@Component
public class FlowExecutionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionListener.class);

    private final MeterRegistry meterRegistry;

    public FlowExecutionListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        log.info("=== STARTING JOB FLOW: {} ===", jobName);
        log.info("Job parameters: {}", jobExecution.getJobParameters());

        Counter.builder("cardemo.batch.flow.started")
                .tag("flow", jobName)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Duration duration = Duration.between(
                jobExecution.getStartTime().toInstant(ZoneOffset.UTC),
                jobExecution.getEndTime() != null
                        ? jobExecution.getEndTime().toInstant(ZoneOffset.UTC)
                        : java.time.Instant.now());

        Timer.builder("cardemo.batch.flow.duration")
                .tag("flow", jobName)
                .tag("status", status.toString())
                .register(meterRegistry)
                .record(duration);

        String exitCode = exitStatus.getExitCode();
        String metricStatus;
        if (ExitStatus.COMPLETED.getExitCode().equals(exitCode)) {
            metricStatus = "success";
        } else if ("COMPLETED_WITH_REJECTS".equals(exitCode)) {
            metricStatus = "warning";
        } else {
            metricStatus = "failure";
        }

        Counter.builder("cardemo.batch.flow.completed")
                .tag("flow", jobName)
                .tag("status", metricStatus)
                .register(meterRegistry)
                .increment();

        log.info("=== COMPLETED JOB FLOW: {} ===", jobName);
        log.info("  Status: {}", status);
        log.info("  Exit Status: {} - {}", exitCode, exitStatus.getExitDescription());
        log.info("  Duration: {} ms", duration.toMillis());

        if ("COMPLETED_WITH_REJECTS".equals(exitCode)) {
            log.warn("  Job completed with RETURN-CODE=4 (warnings/rejections exist)");
        } else if (!ExitStatus.COMPLETED.getExitCode().equals(exitCode)) {
            log.error("  Job FAILED with exit code: {}", exitCode);
        }
    }
}
