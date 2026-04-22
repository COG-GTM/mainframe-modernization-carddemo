package com.carddemo.batch.controller;

import com.carddemo.batch.processor.AccountClassificationProcessor;
import com.carddemo.batch.reader.DailyTransactionReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for triggering and monitoring batch jobs.
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final DailyTransactionReader dailyTransactionReader;
    private final AccountClassificationProcessor accountClassificationProcessor;

    @Qualifier("transactionPostingJob")
    private final Job transactionPostingJob;

    @Qualifier("interestCalculationJob")
    private final Job interestCalculationJob;

    @Qualifier("accountProcessingJob")
    private final Job accountProcessingJob;

    @PostMapping("/transaction-posting")
    public ResponseEntity<Map<String, Object>> triggerTransactionPosting() {
        return launchJob(transactionPostingJob, "transactionPostingJob");
    }

    @PostMapping("/interest-calculation")
    public ResponseEntity<Map<String, Object>> triggerInterestCalculation() {
        return launchJob(interestCalculationJob, "interestCalculationJob");
    }

    @PostMapping("/account-processing")
    public ResponseEntity<Map<String, Object>> triggerAccountProcessing() {
        return launchJob(accountProcessingJob, "accountProcessingJob");
    }

    @GetMapping("/jobs/{executionId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long executionId) {
        JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
        if (jobExecution == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("executionId", jobExecution.getId());
        response.put("jobName", jobExecution.getJobInstance().getJobName());
        response.put("status", jobExecution.getStatus().toString());
        response.put("startTime", jobExecution.getStartTime());
        response.put("endTime", jobExecution.getEndTime());
        response.put("exitStatus", jobExecution.getExitStatus().getExitCode());

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> launchJob(Job job, String jobName) {
        try {
            // Reset stateful components before launching jobs
            if ("transactionPostingJob".equals(jobName)) {
                dailyTransactionReader.resetReader();
            } else if ("accountProcessingJob".equals(jobName)) {
                accountClassificationProcessor.resetCounters();
            }

            JobParameters params = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, params);

            Map<String, Object> response = new HashMap<>();
            response.put("executionId", execution.getId());
            response.put("jobName", jobName);
            response.put("status", execution.getStatus().toString());
            response.put("message", "Job launched successfully");

            log.info("Launched job '{}' with execution ID: {}", jobName, execution.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to launch job '{}': {}", jobName, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("jobName", jobName);
            error.put("status", "FAILED");
            error.put("message", "Failed to launch job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
