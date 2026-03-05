package com.carddemo.dto;

import jakarta.validation.constraints.Pattern;

/**
 * DTO for report generation request.
 * Mirrors CORPT00C.cbl report submission.
 */
public class ReportRequest {

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Start date must be in YYYY-MM-DD format")
    private String startDate;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "End date must be in YYYY-MM-DD format")
    private String endDate;

    private Long acctId;

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
}
