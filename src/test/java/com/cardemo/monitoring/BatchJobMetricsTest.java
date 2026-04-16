package com.cardemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchJobMetricsTest {

    private MeterRegistry registry;
    private BatchJobMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new BatchJobMetrics(registry);
    }

    @Test
    void recordJobDurationInMilliseconds() {
        metrics.recordJobDuration(5000);

        assertEquals(1, metrics.getJobExecutionCount());
        assertTrue(metrics.getJobMeanDurationMs() > 0);
    }

    @Test
    void recordSuccessIncrementsCounter() {
        metrics.recordSuccess();
        metrics.recordSuccess();

        assertEquals(2.0, metrics.getSuccessCount());
    }

    @Test
    void recordFailureIncrementsCounter() {
        metrics.recordFailure();

        assertEquals(1.0, metrics.getFailureCount());
    }

    @Test
    void timerSampleRecordsDuration() throws InterruptedException {
        var sample = metrics.startTimer();
        Thread.sleep(50);
        metrics.recordJobDuration(sample);

        assertEquals(1, metrics.getJobExecutionCount());
        assertTrue(metrics.getJobMeanDurationMs() >= 40);
    }

    @Test
    void maxDurationTracked() {
        metrics.recordJobDuration(1000);
        metrics.recordJobDuration(5000);
        metrics.recordJobDuration(2000);

        assertEquals(5000.0, metrics.getJobMaxDurationMs(), 1.0);
    }
}
