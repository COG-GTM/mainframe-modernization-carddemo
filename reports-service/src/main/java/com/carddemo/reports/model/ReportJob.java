package com.carddemo.reports.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportJob {

    private String id;
    private ReportType reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private ReportStatus status;
    private String jobName;
    private String filePath;
    private String errorMessage;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    public ReportJob() {
    }

    public ReportJob(String id, ReportType reportType, LocalDate startDate,
                     LocalDate endDate, String jobName) {
        this.id = id;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.jobName = jobName;
        this.status = ReportStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
