package com.cardemo.batch.orchestration.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JobFlowResult model.
 */
class JobFlowResultTest {

    @Test
    void newFlowResult_hasSuccessReturnCode() {
        JobFlowResult result = new JobFlowResult("POSTTRAN");
        assertEquals("POSTTRAN", result.getFlowName());
        assertEquals(ReturnCode.SUCCESS, result.getOverallReturnCode().getCode());
        assertTrue(result.isSuccessful());
        assertTrue(result.getStepResults().isEmpty());
        assertNotNull(result.getStartTime());
    }

    @Test
    void addStepResult_updatesOverallReturnCode() {
        JobFlowResult result = new JobFlowResult("TEST");
        result.addStepResult(new JobFlowResult.StepResult(
                "step1", new ReturnCode(ReturnCode.SUCCESS),
                Instant.now(), Instant.now()));
        assertEquals(ReturnCode.SUCCESS, result.getOverallReturnCode().getCode());

        result.addStepResult(new JobFlowResult.StepResult(
                "step2", new ReturnCode(ReturnCode.WARNING),
                Instant.now(), Instant.now()));
        assertEquals(ReturnCode.WARNING, result.getOverallReturnCode().getCode());
        assertTrue(result.isSuccessful()); // Warning is still successful
    }

    @Test
    void addStepResult_withError_marksFlowFailed() {
        JobFlowResult result = new JobFlowResult("TEST");
        result.addStepResult(new JobFlowResult.StepResult(
                "step1", new ReturnCode(ReturnCode.ERROR),
                Instant.now(), Instant.now()));
        assertFalse(result.isSuccessful());
        assertEquals(ReturnCode.ERROR, result.getOverallReturnCode().getCode());
    }

    @Test
    void overallReturnCode_takesHighestCode() {
        JobFlowResult result = new JobFlowResult("TEST");
        result.addStepResult(new JobFlowResult.StepResult(
                "step1", new ReturnCode(ReturnCode.WARNING),
                Instant.now(), Instant.now()));
        result.addStepResult(new JobFlowResult.StepResult(
                "step2", new ReturnCode(ReturnCode.ERROR),
                Instant.now(), Instant.now()));
        result.addStepResult(new JobFlowResult.StepResult(
                "step3", new ReturnCode(ReturnCode.SUCCESS),
                Instant.now(), Instant.now()));
        assertEquals(ReturnCode.ERROR, result.getOverallReturnCode().getCode());
    }

    @Test
    void complete_setsEndTime() {
        JobFlowResult result = new JobFlowResult("TEST");
        assertNull(result.getEndTime());
        result.complete();
        assertNotNull(result.getEndTime());
    }

    @Test
    void stepResults_areUnmodifiable() {
        JobFlowResult result = new JobFlowResult("TEST");
        result.addStepResult(new JobFlowResult.StepResult(
                "step1", new ReturnCode(ReturnCode.SUCCESS),
                Instant.now(), Instant.now()));
        assertThrows(UnsupportedOperationException.class, () ->
                result.getStepResults().add(new JobFlowResult.StepResult(
                        "step2", new ReturnCode(ReturnCode.SUCCESS),
                        Instant.now(), Instant.now())));
    }
}
