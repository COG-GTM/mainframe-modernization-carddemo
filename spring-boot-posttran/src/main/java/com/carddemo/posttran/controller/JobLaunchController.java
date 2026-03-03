package com.carddemo.posttran.controller;

import com.carddemo.posttran.service.TransactionProcessingService;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller to launch the PostTransactionJob on demand.
 * Provides an alternative trigger mechanism to the @Scheduled nightly run,
 * suitable for AWS EventBridge or manual invocation.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobLaunchController {

    private final TransactionProcessingService processingService;

    public JobLaunchController(TransactionProcessingService processingService) {
        this.processingService = processingService;
    }

    /**
     * POST /api/jobs/post-transaction
     * Launches the post-transaction batch job and returns execution status.
     */
    @PostMapping("/post-transaction")
    public ResponseEntity<Map<String, Object>> launchPostTransactionJob() {
        JobExecution execution = processingService.runJob();
        Map<String, Object> response = Map.of(
                "jobId", execution.getJobId(),
                "status", execution.getExitStatus().getExitCode(),
                "startTime", execution.getStartTime() != null
                        ? execution.getStartTime().toString() : "",
                "endTime", execution.getEndTime() != null
                        ? execution.getEndTime().toString() : ""
        );
        return ResponseEntity.ok(response);
    }
}
