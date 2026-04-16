package com.cardemo.batch.orchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobLaunchService.
 */
@ExtendWith(MockitoExtension.class)
class JobLaunchServiceTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job job;

    private JobLaunchService jobLaunchService;

    @BeforeEach
    void setUp() {
        jobLaunchService = new JobLaunchService(jobLauncher);
        when(job.getName()).thenReturn("testJob");
    }

    @Test
    void launchJob_callsJobLauncher() throws Exception {
        JobExecution expectedExecution = new JobExecution(
                new JobInstance(1L, "testJob"), new JobParameters());
        when(jobLauncher.run(eq(job), any(JobParameters.class)))
                .thenReturn(expectedExecution);

        JobExecution result = jobLaunchService.launchJob(job);
        assertNotNull(result);
        verify(jobLauncher).run(eq(job), any(JobParameters.class));
    }

    @Test
    void launchJob_includesRunId() throws Exception {
        JobExecution expectedExecution = new JobExecution(
                new JobInstance(1L, "testJob"), new JobParameters());
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        when(jobLauncher.run(eq(job), any(JobParameters.class)))
                .thenReturn(expectedExecution);

        jobLaunchService.launchJob(job);

        verify(jobLauncher).run(eq(job), paramsCaptor.capture());
        JobParameters params = paramsCaptor.getValue();
        assertNotNull(params.getString("run.id"));
    }

    @Test
    void launchJob_withAdditionalParams_includesAllParams() throws Exception {
        JobExecution expectedExecution = new JobExecution(
                new JobInstance(1L, "testJob"), new JobParameters());
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        when(jobLauncher.run(eq(job), any(JobParameters.class)))
                .thenReturn(expectedExecution);

        jobLaunchService.launchJob(job, Map.of("custom.param", "value1"));

        verify(jobLauncher).run(eq(job), paramsCaptor.capture());
        JobParameters params = paramsCaptor.getValue();
        assertNotNull(params.getString("run.id"));
        assertEquals("value1", params.getString("custom.param"));
    }

    @Test
    void launchWithParmDate_includesParmDate() throws Exception {
        JobExecution expectedExecution = new JobExecution(
                new JobInstance(1L, "testJob"), new JobParameters());
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        when(jobLauncher.run(eq(job), any(JobParameters.class)))
                .thenReturn(expectedExecution);

        jobLaunchService.launchWithParmDate(job, "2022071800");

        verify(jobLauncher).run(eq(job), paramsCaptor.capture());
        assertEquals("2022071800", paramsCaptor.getValue().getString("parm.date"));
    }

    @Test
    void launchWithCurrentDate_generatesDateParameter() throws Exception {
        JobExecution expectedExecution = new JobExecution(
                new JobInstance(1L, "testJob"), new JobParameters());
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        when(jobLauncher.run(eq(job), any(JobParameters.class)))
                .thenReturn(expectedExecution);

        jobLaunchService.launchWithCurrentDate(job);

        verify(jobLauncher).run(eq(job), paramsCaptor.capture());
        String parmDate = paramsCaptor.getValue().getString("parm.date");
        assertNotNull(parmDate);
        assertTrue(parmDate.endsWith("00"));
        assertEquals(10, parmDate.length());
    }
}
