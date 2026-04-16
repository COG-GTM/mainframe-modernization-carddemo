package com.cardemo.batch.orchestration.flow;

import com.cardemo.batch.orchestration.model.JobFlowResult;
import com.cardemo.batch.orchestration.model.ReturnCode;
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
 * Unit tests for PosttranFlow.
 */
@ExtendWith(MockitoExtension.class)
class PosttranFlowTest {

    @Mock
    private JobLaunchService jobLaunchService;

    @Mock
    private Job transactionPostingJob;

    private PosttranFlow posttranFlow;
    private ReturnCodeEvaluator returnCodeEvaluator;

    @BeforeEach
    void setUp() {
        returnCodeEvaluator = new ReturnCodeEvaluator();
        posttranFlow = new PosttranFlow(jobLaunchService, returnCodeEvaluator,
                new SimpleMeterRegistry());
    }

    @Test
    void execute_successfulPosting_returnsSuccessRC0() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.COMPLETED);
        when(jobLaunchService.launchJob(any(Job.class))).thenReturn(execution);

        JobFlowResult result = posttranFlow.execute(transactionPostingJob);

        assertTrue(result.isSuccessful());
        assertEquals(ReturnCode.SUCCESS, result.getOverallReturnCode().getCode());
        assertEquals(1, result.getStepResults().size());
        assertNotNull(result.getEndTime());
    }

    @Test
    void execute_withRejections_returnsWarningRC4() throws Exception {
        JobExecution execution = createJobExecution(
                new ExitStatus("COMPLETED_WITH_REJECTS", "5 rejections"));
        when(jobLaunchService.launchJob(any(Job.class))).thenReturn(execution);

        JobFlowResult result = posttranFlow.execute(transactionPostingJob);

        assertTrue(result.isSuccessful()); // RC=4 is still considered successful
        assertEquals(ReturnCode.WARNING, result.getOverallReturnCode().getCode());
    }

    @Test
    void execute_jobFailure_returnsErrorRC8() throws Exception {
        JobExecution execution = createJobExecution(ExitStatus.FAILED);
        when(jobLaunchService.launchJob(any(Job.class))).thenReturn(execution);

        JobFlowResult result = posttranFlow.execute(transactionPostingJob);

        assertFalse(result.isSuccessful());
        assertTrue(result.getOverallReturnCode().isError());
    }

    @Test
    void execute_exception_returnsErrorRC8() throws Exception {
        when(jobLaunchService.launchJob(any(Job.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        JobFlowResult result = posttranFlow.execute(transactionPostingJob);

        assertFalse(result.isSuccessful());
        assertTrue(result.getOverallReturnCode().isError());
        assertTrue(result.getOverallReturnCode().getMessage().contains("Database connection failed"));
    }

    private JobExecution createJobExecution(ExitStatus exitStatus) {
        JobExecution execution = new JobExecution(
                new JobInstance(1L, "transactionPostingJob"), new JobParameters());
        execution.setExitStatus(exitStatus);
        return execution;
    }
}
