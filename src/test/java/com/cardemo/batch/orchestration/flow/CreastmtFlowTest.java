package com.cardemo.batch.orchestration.flow;

import com.cardemo.batch.orchestration.model.JobFlowResult;
import com.cardemo.batch.orchestration.model.ReturnCode;
import com.cardemo.batch.orchestration.service.GdgService;
import com.cardemo.batch.orchestration.service.JobLaunchService;
import com.cardemo.batch.orchestration.service.ReturnCodeEvaluator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreastmtFlow.
 */
@ExtendWith(MockitoExtension.class)
class CreastmtFlowTest {

    @Mock
    private JobLaunchService jobLaunchService;

    @Mock
    private GdgService gdgService;

    @Mock
    private Job statementGenerationJob;

    private CreastmtFlow creastmtFlow;
    private ReturnCodeEvaluator returnCodeEvaluator;

    @BeforeEach
    void setUp() {
        returnCodeEvaluator = new ReturnCodeEvaluator();
        when(gdgService.getBaseOutputDir()).thenReturn("/tmp/test-output");
        creastmtFlow = new CreastmtFlow(jobLaunchService, returnCodeEvaluator,
                gdgService, new SimpleMeterRegistry());
    }

    @Test
    void execute_allStepsSuccessful_returnsRC0() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.COMPLETED);
        when(jobLaunchService.launchJob(any(Job.class))).thenReturn(execution);

        JobFlowResult result = creastmtFlow.execute(statementGenerationJob);

        assertTrue(result.isSuccessful());
        assertEquals(ReturnCode.SUCCESS, result.getOverallReturnCode().getCode());
        assertEquals("CREASTMT", result.getFlowName());
        // 3 steps: DATA-PREP, OUTPUT-ARCHIVE, CBSTM03A-STATEMENT
        assertEquals(3, result.getStepResults().size());
    }

    @Test
    void execute_multiStepFlow_executesAllSteps() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.COMPLETED);
        when(jobLaunchService.launchJob(any(Job.class))).thenReturn(execution);

        JobFlowResult result = creastmtFlow.execute(statementGenerationJob);

        assertEquals("DATA-PREP", result.getStepResults().get(0).getStepName());
        assertEquals("OUTPUT-ARCHIVE", result.getStepResults().get(1).getStepName());
        assertEquals("CBSTM03A-STATEMENT", result.getStepResults().get(2).getStepName());
    }

    @Test
    void execute_statementGenFails_returnsError() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.FAILED);
        when(jobLaunchService.launchJob(any(Job.class))).thenReturn(execution);

        JobFlowResult result = creastmtFlow.execute(statementGenerationJob);

        assertFalse(result.isSuccessful());
        assertTrue(result.getOverallReturnCode().isError());
    }

    @Test
    void execute_statementGenException_returnsError() throws Exception {
        when(jobLaunchService.launchJob(any(Job.class)))
                .thenThrow(new RuntimeException("Statement gen failed"));

        JobFlowResult result = creastmtFlow.execute(statementGenerationJob);

        assertFalse(result.isSuccessful());
        assertEquals(3, result.getStepResults().size());
        // First two steps succeed, third fails
        assertEquals(ReturnCode.SUCCESS,
                result.getStepResults().get(0).getReturnCode().getCode());
        assertEquals(ReturnCode.SUCCESS,
                result.getStepResults().get(1).getReturnCode().getCode());
        assertTrue(result.getStepResults().get(2).getReturnCode().isError());
    }

    @Test
    void execute_completionSetsEndTime() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.COMPLETED);
        when(jobLaunchService.launchJob(any(Job.class))).thenReturn(execution);

        JobFlowResult result = creastmtFlow.execute(statementGenerationJob);

        assertNotNull(result.getEndTime());
    }

    private JobExecution createJobExecution(ExitStatus exitStatus) {
        JobExecution execution = new JobExecution(
                new JobInstance(1L, "statementGenerationJob"), new JobParameters());
        execution.setExitStatus(exitStatus);
        return execution;
    }
}
