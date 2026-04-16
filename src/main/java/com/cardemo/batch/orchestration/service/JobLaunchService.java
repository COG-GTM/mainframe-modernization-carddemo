package com.cardemo.batch.orchestration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service for launching Spring Batch jobs with proper parameter handling.
 * Centralizes job launch logic to support orchestration flows.
 */
@Service
public class JobLaunchService {

    private static final Logger log = LoggerFactory.getLogger(JobLaunchService.class);

    private final JobLauncher jobLauncher;

    public JobLaunchService(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    /**
     * Launches a job with a unique run identifier to allow re-execution.
     */
    public JobExecution launchJob(Job job) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString())
                .toJobParameters();

        log.info("Launching job '{}' with parameters: {}", job.getName(), params);
        JobExecution execution = jobLauncher.run(job, params);
        log.info("Job '{}' completed with status: {}", job.getName(), execution.getExitStatus());
        return execution;
    }

    /**
     * Launches a job with additional parameters (e.g., PARM-DATE for INTCALC).
     */
    public JobExecution launchJob(Job job, Map<String, String> additionalParams) throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString("run.id", Instant.now().toString());

        additionalParams.forEach(builder::addString);

        JobParameters params = builder.toJobParameters();
        log.info("Launching job '{}' with parameters: {}", job.getName(), params);
        JobExecution execution = jobLauncher.run(job, params);
        log.info("Job '{}' completed with status: {}", job.getName(), execution.getExitStatus());
        return execution;
    }

    /**
     * Launches the interest calculation job with a PARM-DATE parameter.
     * Maps to JCL: EXEC PGM=CBACT04C,PARM='2022071800'
     */
    public JobExecution launchWithParmDate(Job job, String parmDate) throws Exception {
        return launchJob(job, Map.of("parm.date", parmDate));
    }

    /**
     * Launches the interest calculation job using today's date.
     */
    public JobExecution launchWithCurrentDate(Job job) throws Exception {
        String parmDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "00";
        return launchWithParmDate(job, parmDate);
    }
}
