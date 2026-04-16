package com.cardemo.batch.orchestration.flow;

import com.cardemo.batch.orchestration.model.JobFlowResult;
import com.cardemo.batch.orchestration.model.ReturnCode;
import com.cardemo.batch.orchestration.service.JobLaunchService;
import com.cardemo.batch.orchestration.service.ReturnCodeEvaluator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IntcalcFlow.
 */
@ExtendWith(MockitoExtension.class)
class IntcalcFlowTest {

    @Mock
    private JobLaunchService jobLaunchService;

    @Mock
    private Job interestCalculationJob;

    private IntcalcFlow intcalcFlow;
    private ReturnCodeEvaluator returnCodeEvaluator;

    @BeforeEach
    void setUp() {
        returnCodeEvaluator = new ReturnCodeEvaluator();
        intcalcFlow = new IntcalcFlow(jobLaunchService, returnCodeEvaluator,
                new SimpleMeterRegistry());
    }

    @Test
    void execute_withParmDate_passesDateParameter() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.COMPLETED);
        when(jobLaunchService.launchWithParmDate(any(Job.class), eq("2022071800")))
                .thenReturn(execution);

        JobFlowResult result = intcalcFlow.execute(interestCalculationJob, "2022071800");

        assertTrue(result.isSuccessful());
        verify(jobLaunchService).launchWithParmDate(interestCalculationJob, "2022071800");
    }

    @Test
    void execute_withoutParmDate_usesCurrentDate() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.COMPLETED);
        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        when(jobLaunchService.launchWithParmDate(any(Job.class), dateCaptor.capture()))
                .thenReturn(execution);

        JobFlowResult result = intcalcFlow.execute(interestCalculationJob);

        assertTrue(result.isSuccessful());
        String parmDate = dateCaptor.getValue();
        assertNotNull(parmDate);
        assertTrue(parmDate.endsWith("00"));
        assertEquals(10, parmDate.length());
    }

    @Test
    void execute_successful_returnsRC0() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.COMPLETED);
        when(jobLaunchService.launchWithParmDate(any(Job.class), any()))
                .thenReturn(execution);

        JobFlowResult result = intcalcFlow.execute(interestCalculationJob, "2022071800");

        assertEquals(ReturnCode.SUCCESS, result.getOverallReturnCode().getCode());
        assertEquals("INTCALC", result.getFlowName());
        assertEquals(1, result.getStepResults().size());
    }

    @Test
    void execute_failure_returnsError() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.FAILED);
        when(jobLaunchService.launchWithParmDate(any(Job.class), any()))
                .thenReturn(execution);

        JobFlowResult result = intcalcFlow.execute(interestCalculationJob, "2022071800");

        assertFalse(result.isSuccessful());
        assertTrue(result.getOverallReturnCode().isError());
    }

    @Test
    void execute_exception_returnsError() throws Exception {
        when(jobLaunchService.launchWithParmDate(any(Job.class), any()))
                .thenThrow(new RuntimeException("Interest calc failed"));

        JobFlowResult result = intcalcFlow.execute(interestCalculationJob, "2022071800");

        assertFalse(result.isSuccessful());
        assertTrue(result.getOverallReturnCode().getMessage().contains("Interest calc failed"));
    }

    private JobExecution createJobExecution(ExitStatus exitStatus) {
        JobExecution execution = new JobExecution(
                new JobInstance(1L, "interestCalculationJob"), new JobParameters());
        execution.setExitStatus(exitStatus);
        return execution;
    }
}
