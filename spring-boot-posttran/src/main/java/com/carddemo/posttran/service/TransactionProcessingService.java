package com.carddemo.posttran.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service that provides scheduling and on-demand launching of the
 * PostTransactionJob. Maps to the nightly batch trigger from
 * scripts/run_posting.sh / POSTTRAN JCL.
 */
@Service
public class TransactionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessingService.class);

    private final JobLauncher jobLauncher;
    private final Job postTransactionJob;

    public TransactionProcessingService(JobLauncher jobLauncher, Job postTransactionJob) {
        this.jobLauncher = jobLauncher;
        this.postTransactionJob = postTransactionJob;
    }

    /**
     * Nightly scheduled execution of the post-transaction job.
     * Runs at 2:00 AM daily, matching the nightly batch sequence from the mainframe.
     * Configurable via application property: posttran.schedule.cron
     */
    @Scheduled(cron = "${posttran.schedule.cron:0 0 2 * * *}")
    public void runScheduledJob() {
        log.info("Starting scheduled PostTransactionJob execution");
        runJob();
    }

    /**
     * Launch the post-transaction job on demand (e.g. via REST endpoint or EventBridge).
     *
     * @return the JobExecution for monitoring
     */
    public JobExecution runJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(postTransactionJob, params);
            log.info("PostTransactionJob completed with status: {}", execution.getExitStatus());
            return execution;
        } catch (Exception e) {
            log.error("Failed to execute PostTransactionJob", e);
            throw new RuntimeException("PostTransactionJob execution failed", e);
        }
    }
}
