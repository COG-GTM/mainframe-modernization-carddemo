package com.cardemo.batch.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JobCompletionListener.
 * Tests exit code = 4 behavior when rejections exist.
 */
class JobCompletionListenerTest {

    private MeterRegistry meterRegistry;
    private JobCompletionListener listener;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        listener = new JobCompletionListener(meterRegistry);
    }

    @Test
    @DisplayName("Exit status set to COMPLETED_WITH_REJECTS when reject count > 0")
    void afterJob_withRejections_setsExitCode4() {
        // Register and increment reject counter
        Counter.builder("cardemo.batch.transactions.total").register(meterRegistry).increment(10);
        Counter.builder("cardemo.batch.transactions.rejected").register(meterRegistry).increment(3);

        JobExecution jobExecution = new JobExecution(1L);
        jobExecution.setStatus(BatchStatus.COMPLETED);

        listener.afterJob(jobExecution);

        assertEquals("COMPLETED_WITH_REJECTS", jobExecution.getExitStatus().getExitCode());
        assertTrue(jobExecution.getExitStatus().getExitDescription().contains("3"));
    }

    @Test
    @DisplayName("Exit status unchanged when no rejections")
    void afterJob_noRejections_noExitCodeChange() {
        Counter.builder("cardemo.batch.transactions.total").register(meterRegistry).increment(10);
        Counter.builder("cardemo.batch.transactions.rejected").register(meterRegistry);

        JobExecution jobExecution = new JobExecution(1L);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        ExitStatus originalExit = jobExecution.getExitStatus();

        listener.afterJob(jobExecution);

        assertEquals(originalExit.getExitCode(), jobExecution.getExitStatus().getExitCode());
    }
}
