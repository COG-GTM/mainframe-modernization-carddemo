package com.cardemo.batch.orchestration.integration;

import com.cardemo.batch.orchestration.BatchOrchestrationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the orchestrated batch jobs.
 * Verifies that all three job flows execute correctly end-to-end
 * using H2 in-memory database.
 */
@SpringBatchTest
@SpringBootTest(classes = BatchOrchestrationApplication.class)
@ActiveProfiles("test")
class OrchestrationJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("posttranJob")
    private Job posttranJob;

    @Autowired
    @Qualifier("intcalcJob")
    private Job intcalcJob;

    @Autowired
    @Qualifier("creastmtJob")
    private Job creastmtJob;

    @Test
    void posttranJob_executesSuccessfully() throws Exception {
        jobLauncherTestUtils.setJob(posttranJob);
        JobParameters params = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(ExitStatus.COMPLETED.getExitCode(),
                execution.getExitStatus().getExitCode());
        assertFalse(execution.getStepExecutions().isEmpty());
    }

    @Test
    void posttranJob_hasCorrectStepSequence() throws Exception {
        jobLauncherTestUtils.setJob(posttranJob);
        JobParameters params = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        var stepNames = execution.getStepExecutions().stream()
                .map(se -> se.getStepName())
                .toList();
        assertTrue(stepNames.contains("readDailyTransactions"));
        assertTrue(stepNames.contains("validateAndPost"));
    }

    @Test
    void intcalcJob_executesWithParmDate() throws Exception {
        jobLauncherTestUtils.setJob(intcalcJob);
        JobParameters params = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString())
                .addString("parm.date", "2022071800")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(ExitStatus.COMPLETED.getExitCode(),
                execution.getExitStatus().getExitCode());
    }

    @Test
    void intcalcJob_singleStepExecution() throws Exception {
        jobLauncherTestUtils.setJob(intcalcJob);
        JobParameters params = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(1, execution.getStepExecutions().size());
        var step = execution.getStepExecutions().iterator().next();
        assertEquals("computeInterest", step.getStepName());
    }

    @Test
    void creastmtJob_executesMultiStepFlow() throws Exception {
        jobLauncherTestUtils.setJob(creastmtJob);
        JobParameters params = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(ExitStatus.COMPLETED.getExitCode(),
                execution.getExitStatus().getExitCode());
        // CREASTMT has 3 steps: sort, archive, generate
        assertEquals(3, execution.getStepExecutions().size());
    }

    @Test
    void creastmtJob_stepsExecuteInCorrectOrder() throws Exception {
        jobLauncherTestUtils.setJob(creastmtJob);
        JobParameters params = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        var stepNames = execution.getStepExecutions().stream()
                .map(se -> se.getStepName())
                .toList();
        assertEquals("sortTransactions", stepNames.get(0));
        assertEquals("archivePreviousOutput", stepNames.get(1));
        assertEquals("generateStatements", stepNames.get(2));
    }

    @Test
    void allJobs_haveUniqueNames() {
        assertNotEquals(posttranJob.getName(), intcalcJob.getName());
        assertNotEquals(intcalcJob.getName(), creastmtJob.getName());
        assertNotEquals(posttranJob.getName(), creastmtJob.getName());
    }
}
