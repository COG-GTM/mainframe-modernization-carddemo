package com.carddemo.web.report.dto;

/**
 * The three transaction-report choices offered by the {@code CORPT0A} map / {@code CORPT00C}
 * ({@code MONTHLY} / {@code YEARLY} / {@code CUSTOM}), plus the report name the COBOL moved
 * into {@code WS-REPORT-NAME} for its confirmation and success messages.
 */
public enum ReportType {

    /** {@code WHEN MONTHLYI} — first day of the current month to its last day. */
    MONTHLY("Monthly"),
    /** {@code WHEN YEARLYI} — Jan 01 to Dec 31 of the current year. */
    YEARLY("Yearly"),
    /** {@code WHEN CUSTOMI} — the operator-entered start/end date range. */
    CUSTOM("Custom");

    private final String reportName;

    ReportType(String reportName) {
        this.reportName = reportName;
    }

    /** The {@code WS-REPORT-NAME} value used in the COBOL screen messages. */
    public String reportName() {
        return reportName;
    }
}
