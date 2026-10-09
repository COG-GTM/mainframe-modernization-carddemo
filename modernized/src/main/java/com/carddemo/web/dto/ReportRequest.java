package com.carddemo.web.dto;

/**
 * CORPT00C - transaction report request. The COBOL screen offered Monthly, Yearly and Custom
 * ranges and submitted the TRANREPT job through an extra partition TDQ.
 */
public record ReportRequest(String reportType, String startDate, String endDate) {
}
