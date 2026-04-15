package com.carddemo.report.dto;

/**
 * Request DTO for generating reports.
 * Maps to the date range input from CORPT00C (start/end date parameters).
 */
public class ReportRequest {

    private String startDate;
    private String endDate;
    private String accountId;
    private String cardNumber;

    public ReportRequest() {
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
