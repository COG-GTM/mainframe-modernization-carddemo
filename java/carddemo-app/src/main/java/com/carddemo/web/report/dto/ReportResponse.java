package com.carddemo.web.report.dto;

/**
 * Response for {@code POST /api/reports/transactions}.
 *
 * <p>Mirrors the {@code CORPT00C} outcomes: a prompt to confirm, or — once confirmed — the
 * submitted report. The legacy program submits a batch job to the {@code JOBS} TDQ and only
 * shows "&lt;name&gt; report submitted for printing ..."; here the report is generated inline
 * from {@code Transaction} data and returned in {@link #report} (see the CS-7 mapping doc for
 * how this relates to the {@code TRANREPT}/{@code CBTRN03C} batch report CS-14 will schedule).
 * </p>
 *
 * @param reportName           WS-REPORT-NAME (Monthly / Yearly / Custom)
 * @param startDate            resolved inclusive start date ({@code yyyy-MM-dd})
 * @param endDate              resolved inclusive end date ({@code yyyy-MM-dd})
 * @param message              the {@code ERRMSGO} message (prompt or success)
 * @param submitted            whether the report was generated ({@code CONFIRMI = 'Y'})
 * @param confirmationRequired whether the caller must resubmit with {@code confirm=Y}
 * @param report               the generated report (null unless {@code submitted})
 */
public record ReportResponse(
        String reportName,
        String startDate,
        String endDate,
        String message,
        boolean submitted,
        boolean confirmationRequired,
        TransactionReport report) {

    /** Awaiting a {@code Y}/{@code N} confirmation ({@code CONFIRMI = SPACES}). */
    public static ReportResponse confirmationRequired(ReportType type, String startDate,
            String endDate, String message) {
        return new ReportResponse(type.reportName(), startDate, endDate, message, false, true, null);
    }

    /** Confirmation declined ({@code CONFIRMI = 'N'}) — the COBOL clear-screen branch. */
    public static ReportResponse declined(ReportType type, String startDate, String endDate) {
        return new ReportResponse(type.reportName(), startDate, endDate, null, false, false, null);
    }

    /** Report generated ({@code CONFIRMI = 'Y'}). */
    public static ReportResponse submitted(ReportType type, String startDate, String endDate,
            String message, TransactionReport report) {
        return new ReportResponse(type.reportName(), startDate, endDate, message, true, false, report);
    }
}
