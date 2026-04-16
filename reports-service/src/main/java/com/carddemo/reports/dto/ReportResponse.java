package com.carddemo.reports.dto;

import com.carddemo.reports.model.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Report job status and details")
public class ReportResponse {

    @Schema(description = "Unique report job identifier")
    private String id;

    @Schema(description = "Report type code")
    private String reportType;

    @Schema(description = "Report type display name")
    private String reportTypeName;

    @Schema(description = "Report date range start")
    private LocalDate startDate;

    @Schema(description = "Report date range end")
    private LocalDate endDate;

    @Schema(description = "Current job status")
    private ReportStatus status;

    @Schema(description = "Submitted job name")
    private String jobName;

    @Schema(description = "Error message if the job failed")
    private String errorMessage;

    @Schema(description = "Timestamp when the report was submitted")
    private LocalDateTime submittedAt;

    @Schema(description = "Timestamp when the report completed")
    private LocalDateTime completedAt;

    public ReportResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportTypeName() {
        return reportTypeName;
    }

    public void setReportTypeName(String reportTypeName) {
        this.reportTypeName = reportTypeName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
