package com.carddemo.reports.service;

import com.carddemo.reports.dto.ReportRequest;
import com.carddemo.reports.dto.ReportResponse;
import com.carddemo.reports.exception.ReportNotFoundException;
import com.carddemo.reports.exception.ReportNotReadyException;
import com.carddemo.reports.model.ReportJob;
import com.carddemo.reports.model.ReportStatus;
import com.carddemo.reports.model.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main service coordinating report submission and status tracking.
 * <p>
 * Mirrors the COBOL CORPT00C program flow:
 * 1. Validate the report request (type + date inputs)
 * 2. Compute the date range based on report type
 * 3. Submit async job for report generation
 * 4. Return job tracking information
 * <p>
 * In the original COBOL, the job name was constructed from the user ID
 * and submitted via INTRDR. Here we use a UUID-based job ID and Spring @Async.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportValidationService validationService;
    private final ReportGenerationService generationService;
    private final Map<String, ReportJob> jobStore = new ConcurrentHashMap<>();

    public ReportService(ReportValidationService validationService,
                         ReportGenerationService generationService) {
        this.validationService = validationService;
        this.generationService = generationService;
    }

    /**
     * Submit a new report request. Validates inputs, computes the date range,
     * and kicks off async generation.
     */
    public ReportResponse submitReport(ReportRequest request) {
        ReportValidationService.DateRange dateRange =
                validationService.validateAndComputeDateRange(request);

        ReportType reportType = ReportType.fromCode(request.getReportType());
        String jobId = UUID.randomUUID().toString();
        String jobName = "TRNRPT-" + jobId.substring(0, 8).toUpperCase();

        ReportJob job = new ReportJob(jobId, reportType,
                dateRange.startDate(), dateRange.endDate(), jobName);
        jobStore.put(jobId, job);

        log.info("{} report submitted for printing ... (jobId={}, jobName={})",
                reportType.displayName(), jobId, jobName);

        generationService.generateReport(job);

        return toResponse(job);
    }

    /**
     * Get the current status of a report job.
     */
    public ReportResponse getReportStatus(String id) {
        ReportJob job = jobStore.get(id);
        if (job == null) {
            throw new ReportNotFoundException(id);
        }
        return toResponse(job);
    }

    /**
     * Get the file path for a completed report, for download.
     */
    public Path getReportFilePath(String id) {
        ReportJob job = jobStore.get(id);
        if (job == null) {
            throw new ReportNotFoundException(id);
        }
        if (job.getStatus() != ReportStatus.COMPLETED || job.getFilePath() == null) {
            throw new ReportNotReadyException(id, job.getStatus());
        }
        return Path.of(job.getFilePath());
    }

    private ReportResponse toResponse(ReportJob job) {
        ReportResponse response = new ReportResponse();
        response.setId(job.getId());
        response.setReportType(job.getReportType().getCode());
        response.setReportTypeName(job.getReportType().displayName());
        response.setStartDate(job.getStartDate());
        response.setEndDate(job.getEndDate());
        response.setStatus(job.getStatus());
        response.setJobName(job.getJobName());
        response.setErrorMessage(job.getErrorMessage());
        response.setSubmittedAt(job.getSubmittedAt());
        response.setCompletedAt(job.getCompletedAt());
        return response;
    }
}
