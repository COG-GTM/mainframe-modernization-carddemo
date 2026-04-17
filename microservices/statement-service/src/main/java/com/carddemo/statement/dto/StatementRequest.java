package com.carddemo.statement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for statement generation.
 * Maps to the input parameters that CBSTM03A used to determine
 * which account and date range to generate a statement for.
 */
public class StatementRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "Start date is required")
    private String startDate;

    @NotBlank(message = "End date is required")
    private String endDate;

    public StatementRequest() {
    }

    public StatementRequest(String accountId, String startDate, String endDate) {
        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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
