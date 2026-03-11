package com.carddemo.integration;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class BatchJobIntegrationTest {
    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired @Qualifier("transactionPostingJob") private Job transactionPostingJob;
    @Autowired @Qualifier("interestCalculationJob") private Job interestCalculationJob;
    @Autowired @Qualifier("combineTransactionsJob") private Job combineTransactionsJob;
    @Autowired @Qualifier("statementGenerationJob") private Job statementGenerationJob;
    @Autowired @Qualifier("transactionReportJob") private Job transactionReportJob;

    @Test
    void transactionPostingJob_runs() throws Exception {
        jobLauncherTestUtils.setJob(transactionPostingJob);
        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void interestCalculationJob_runs() throws Exception {
        jobLauncherTestUtils.setJob(interestCalculationJob);
        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void combineTransactionsJob_runs() throws Exception {
        jobLauncherTestUtils.setJob(combineTransactionsJob);
        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void statementGenerationJob_runs() throws Exception {
        jobLauncherTestUtils.setJob(statementGenerationJob);
        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void transactionReportJob_runs() throws Exception {
        jobLauncherTestUtils.setJob(transactionReportJob);
        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
