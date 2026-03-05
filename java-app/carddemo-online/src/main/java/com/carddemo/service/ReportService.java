package com.carddemo.service;

import com.carddemo.dto.ReportRequest;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Report generation service — replaces CORPT00C.cbl.
 * The original program submits a batch job via extra-partition TDQ.
 * In Java, triggers a Spring Batch job asynchronously.
 */
@Service
public class ReportService {

    private final JobLauncher jobLauncher;
    private final Job transactionReportJob;

    public ReportService(JobLauncher jobLauncher,
                         @Autowired(required = false) @Qualifier("transactionReportJob") Job transactionReportJob) {
        this.jobLauncher = jobLauncher;
        this.transactionReportJob = transactionReportJob;
    }

    /**
     * Submit a transaction report for async generation.
     * Mirrors CORPT00C: submits batch job with parameters.
     */
    public Map<String, Object> submitTransactionReport(ReportRequest request) {
        String reportId = UUID.randomUUID().toString();

        if (transactionReportJob == null) {
            throw new IllegalStateException("Report job not configured. Deploy the batch module to enable report generation.");
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("reportId", reportId)
                    .addString("startDate", request.getStartDate())
                    .addString("endDate", request.getEndDate())
                    .addLong("acctId", request.getAcctId() != null ? request.getAcctId() : 0L)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(transactionReportJob, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch report job: " + e.getMessage(), e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", reportId);
        result.put("status", "SUBMITTED");
        return result;
    }
}
