package com.carddemo.web.report.dto;

/**
 * Transaction-report request payload for {@code POST /api/reports/transactions}.
 *
 * <p>Maps the {@code CORPT0A} map fields. {@code reportType} selects the mutually-exclusive
 * {@code MONTHLY}/{@code YEARLY}/{@code CUSTOM} radio flags; the {@code start*}/{@code end*}
 * fields are the {@code SDT*}/{@code EDT*} custom date-range inputs (only read for
 * {@link ReportType#CUSTOM}); {@code confirm} is the {@code CONFIRMI} {@code (Y/N)} field.</p>
 *
 * @param reportType MONTHLY / YEARLY / CUSTOM
 * @param startMonth SDTMM PIC X(02)   — custom start month
 * @param startDay   SDTDD PIC X(02)   — custom start day
 * @param startYear  SDTYYYY PIC X(04) — custom start year
 * @param endMonth   EDTMM PIC X(02)   — custom end month
 * @param endDay     EDTDD PIC X(02)   — custom end day
 * @param endYear    EDTYYYY PIC X(04) — custom end year
 * @param confirm    CONFIRMI PIC X(01) — {@code Y}/{@code N} (blank/{@code null} = not yet
 *                   confirmed)
 */
public record ReportRequest(
        ReportType reportType,
        String startMonth,
        String startDay,
        String startYear,
        String endMonth,
        String endDay,
        String endYear,
        String confirm) {
}
