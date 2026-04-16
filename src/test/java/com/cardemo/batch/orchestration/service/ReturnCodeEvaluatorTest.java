package com.cardemo.batch.orchestration.service;

import com.cardemo.batch.orchestration.model.ReturnCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReturnCodeEvaluator.
 */
class ReturnCodeEvaluatorTest {

    private ReturnCodeEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ReturnCodeEvaluator();
    }

    @Test
    void evaluateStep_completed_returnsSuccess() {
        StepExecution stepExecution = createStepExecution(ExitStatus.COMPLETED);
        ReturnCode rc = evaluator.evaluate(stepExecution);
        assertEquals(ReturnCode.SUCCESS, rc.getCode());
        assertTrue(rc.isSuccess());
    }

    @Test
    void evaluateStep_completedWithRejects_returnsWarning() {
        StepExecution stepExecution = createStepExecution(
                new ExitStatus("COMPLETED_WITH_REJECTS", "5 transactions rejected"));
        ReturnCode rc = evaluator.evaluate(stepExecution);
        assertEquals(ReturnCode.WARNING, rc.getCode());
        assertTrue(rc.isWarning());
    }

    @Test
    void evaluateStep_failed_returnsError() {
        StepExecution stepExecution = createStepExecution(ExitStatus.FAILED);
        ReturnCode rc = evaluator.evaluate(stepExecution);
        assertEquals(ReturnCode.ERROR, rc.getCode());
        assertTrue(rc.isError());
    }

    @Test
    void evaluateStep_null_returnsError() {
        ReturnCode rc = evaluator.evaluate((StepExecution) null);
        assertTrue(rc.isError());
    }

    @Test
    void evaluateStep_unknownExitCode_returnsError() {
        StepExecution stepExecution = createStepExecution(
                new ExitStatus("UNKNOWN_STATUS"));
        ReturnCode rc = evaluator.evaluate(stepExecution);
        assertTrue(rc.isError());
    }

    @Test
    void evaluateJob_completed_returnsSuccess() {
        JobExecution jobExecution = createJobExecution(ExitStatus.COMPLETED);
        ReturnCode rc = evaluator.evaluate(jobExecution);
        assertEquals(ReturnCode.SUCCESS, rc.getCode());
    }

    @Test
    void evaluateJob_completedWithRejects_returnsWarning() {
        JobExecution jobExecution = createJobExecution(
                new ExitStatus("COMPLETED_WITH_REJECTS"));
        ReturnCode rc = evaluator.evaluate(jobExecution);
        assertEquals(ReturnCode.WARNING, rc.getCode());
    }

    @Test
    void evaluateJob_failed_returnsError() {
        JobExecution jobExecution = createJobExecution(ExitStatus.FAILED);
        ReturnCode rc = evaluator.evaluate(jobExecution);
        assertTrue(rc.isError());
    }

    @Test
    void evaluateJob_null_returnsError() {
        ReturnCode rc = evaluator.evaluate((JobExecution) null);
        assertTrue(rc.isError());
    }

    @Test
    void shouldContinue_belowThreshold_returnsTrue() {
        ReturnCode rc = new ReturnCode(ReturnCode.SUCCESS);
        assertTrue(evaluator.shouldContinue(rc, ReturnCode.WARNING));
    }

    @Test
    void shouldContinue_atThreshold_returnsTrue() {
        ReturnCode rc = new ReturnCode(ReturnCode.WARNING);
        assertTrue(evaluator.shouldContinue(rc, ReturnCode.WARNING));
    }

    @Test
    void shouldContinue_aboveThreshold_returnsFalse() {
        ReturnCode rc = new ReturnCode(ReturnCode.ERROR);
        assertFalse(evaluator.shouldContinue(rc, ReturnCode.WARNING));
    }

    private StepExecution createStepExecution(ExitStatus exitStatus) {
        JobExecution jobExecution = new JobExecution(
                new JobInstance(1L, "testJob"), new JobParameters());
        StepExecution stepExecution = new StepExecution("testStep", jobExecution);
        stepExecution.setExitStatus(exitStatus);
        return stepExecution;
    }

    private JobExecution createJobExecution(ExitStatus exitStatus) {
        JobExecution jobExecution = new JobExecution(
                new JobInstance(1L, "testJob"), new JobParameters());
        jobExecution.setExitStatus(exitStatus);
        return jobExecution;
    }
}
