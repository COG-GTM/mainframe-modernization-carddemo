package com.cardemo.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Batch job execution metrics.
 * Tracks job execution time (histogram), success/failure counts.
 */
@Component
public class BatchJobMetrics {

    private final Timer jobExecutionTimer;
    private final Counter jobSuccessCount;
    private final Counter jobFailureCount;
    private final MeterRegistry registry;

    public BatchJobMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.jobExecutionTimer = Timer.builder("batch_job_execution_seconds")
                .description("Batch job execution time")
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(100))
                .maximumExpectedValue(Duration.ofHours(2))
                .register(registry);

        this.jobSuccessCount = Counter.builder("batch_job_total")
                .description("Total batch job executions")
                .tag("result", "success")
                .register(registry);

        this.jobFailureCount = Counter.builder("batch_job_total")
                .description("Total batch job executions")
                .tag("result", "failure")
                .register(registry);
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void recordJobDuration(Timer.Sample sample) {
        sample.stop(jobExecutionTimer);
    }

    public void recordJobDuration(long durationMs) {
        jobExecutionTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordSuccess() {
        jobSuccessCount.increment();
    }

    public void recordFailure() {
        jobFailureCount.increment();
    }

    public double getSuccessCount() {
        return jobSuccessCount.count();
    }

    public double getFailureCount() {
        return jobFailureCount.count();
    }

    public long getJobExecutionCount() {
        return jobExecutionTimer.count();
    }

    public double getJobMeanDurationMs() {
        return jobExecutionTimer.mean(TimeUnit.MILLISECONDS);
    }

    public double getJobMaxDurationMs() {
        return jobExecutionTimer.max(TimeUnit.MILLISECONDS);
    }
}
