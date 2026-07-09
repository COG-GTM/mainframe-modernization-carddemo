package com.carddemo.web.report.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The generated transaction report — the structured JSON equivalent of the flat 133-column
 * {@code TRANREPT} dataset produced by batch program {@code CBTRN03C} (copybook
 * {@code CVTRA07Y}). Detail lines are ordered by card number then transaction id (mirroring
 * the {@code SORT FIELDS=(TRAN-CARD-NUM,A)} step of the {@code TRANREPT} proc), with per-
 * account subtotals, page subtotals and a grand total.
 *
 * @param reportName    WS-REPORT-NAME (Monthly / Yearly / Custom)
 * @param startDate     REPT-START-DATE (inclusive, {@code yyyy-MM-dd})
 * @param endDate       REPT-END-DATE (inclusive, {@code yyyy-MM-dd})
 * @param lines         the transaction detail lines
 * @param accountTotals REPORT-ACCOUNT-TOTALS, one per contiguous card group
 * @param pageTotals    REPORT-PAGE-TOTALS, one per page of {@link #pageSize} detail lines
 * @param pageSize      WS-PAGE-SIZE (20) — detail lines per page total
 * @param grandTotal    REPT-GRAND-TOTAL — sum of every {@code TRAN-AMT}
 * @param recordCount   number of transactions in the date range
 */
public record TransactionReport(
        String reportName,
        String startDate,
        String endDate,
        List<TransactionReportLine> lines,
        List<AccountTotal> accountTotals,
        List<BigDecimal> pageTotals,
        int pageSize,
        BigDecimal grandTotal,
        int recordCount) {
}
