package com.cardemo.batch.orchestration.scheduler;

import com.cardemo.batch.orchestration.flow.CreastmtFlow;
import com.cardemo.batch.orchestration.flow.IntcalcFlow;
import com.cardemo.batch.orchestration.flow.PosttranFlow;
import com.cardemo.batch.orchestration.model.JobFlowResult;
import com.cardemo.batch.orchestration.model.ReturnCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchJobScheduler.
 * Verifies that scheduled methods delegate to the correct flow components.
 */
@ExtendWith(MockitoExtension.class)
class BatchJobSchedulerTest {

    @Mock private PosttranFlow posttranFlow;
    @Mock private IntcalcFlow intcalcFlow;
    @Mock private CreastmtFlow creastmtFlow;
    @Mock private Job posttranJob;
    @Mock private Job intcalcJob;
    @Mock private Job creastmtJob;

    private BatchJobScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new BatchJobScheduler(
                posttranFlow, intcalcFlow, creastmtFlow,
                posttranJob, intcalcJob, creastmtJob);
    }

    @Test
    void runPosttran_delegatesToPosttranFlow() {
        JobFlowResult result = createSuccessResult("POSTTRAN");
        when(posttranFlow.execute(any(Job.class))).thenReturn(result);

        scheduler.runPosttran();

        verify(posttranFlow).execute(posttranJob);
    }

    @Test
    void runIntcalc_delegatesToIntcalcFlow() {
        JobFlowResult result = createSuccessResult("INTCALC");
        when(intcalcFlow.execute(any(Job.class))).thenReturn(result);

        scheduler.runIntcalc();

        verify(intcalcFlow).execute(intcalcJob);
    }

    @Test
    void runCreastmt_delegatesToCreastmtFlow() {
        JobFlowResult result = createSuccessResult("CREASTMT");
        when(creastmtFlow.execute(any(Job.class))).thenReturn(result);

        scheduler.runCreastmt();

        verify(creastmtFlow).execute(creastmtJob);
    }

    @Test
    void runPosttran_handlesException() {
        when(posttranFlow.execute(any(Job.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Should not throw
        scheduler.runPosttran();

        verify(posttranFlow).execute(posttranJob);
    }

    private JobFlowResult createSuccessResult(String flowName) {
        JobFlowResult result = new JobFlowResult(flowName);
        result.addStepResult(new JobFlowResult.StepResult(
                "step1", new ReturnCode(ReturnCode.SUCCESS),
                Instant.now(), Instant.now()));
        result.complete();
        return result;
    }
}
