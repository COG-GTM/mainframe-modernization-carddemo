package com.carddemo.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to submit a report for generation")
public class ReportRequest {

    @NotNull(message = "Report type is required")
    @Schema(description = "Report type code: 01 (Monthly), 02 (Yearly), 03 (Custom)",
            example = "01", allowableValues = {"01", "02", "03"})
    private String reportType;

    @Schema(description = "Month (1-12), required for Monthly report type", example = "6")
    private Integer month;

    @Schema(description = "Year, required for Monthly and Yearly report types", example = "2025")
    private Integer year;

    @Schema(description = "Start date (YYYY-MM-DD), required for Custom report type",
            example = "2025-01-01")
    private String startDate;

    @Schema(description = "End date (YYYY-MM-DD), required for Custom report type",
            example = "2025-06-30")
    private String endDate;

    public ReportRequest() {
    }

    public ReportRequest(String reportType, Integer month, Integer year,
                         String startDate, String endDate) {
        this.reportType = reportType;
        this.month = month;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
