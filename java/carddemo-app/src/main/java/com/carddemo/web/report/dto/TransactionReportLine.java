package com.carddemo.web.report.dto;

import java.math.BigDecimal;

/**
 * One transaction detail line of the report — the JSON equivalent of the
 * {@code TRANSACTION-DETAIL-REPORT} record written by batch program {@code CBTRN03C}
 * ({@code 1120-WRITE-DETAIL}, copybook {@code CVTRA07Y}).
 *
 * @param transactionId TRAN-REPORT-TRANS-ID  — TRAN-ID PIC X(16)
 * @param accountId     TRAN-REPORT-ACCOUNT-ID — XREF-ACCT-ID PIC 9(11) (via card xref lookup)
 * @param typeCode      TRAN-REPORT-TYPE-CD   — TRAN-TYPE-CD PIC X(02)
 * @param typeDesc      TRAN-REPORT-TYPE-DESC — TRAN-TYPE-DESC PIC X(50) (trantype lookup)
 * @param categoryCode  TRAN-REPORT-CAT-CD    — TRAN-CAT-CD PIC 9(04)
 * @param categoryDesc  TRAN-REPORT-CAT-DESC  — TRAN-CAT-TYPE-DESC PIC X(50) (trancatg lookup)
 * @param source        TRAN-REPORT-SOURCE    — TRAN-SOURCE PIC X(10)
 * @param amount        TRAN-REPORT-AMT       — TRAN-AMT PIC S9(09)V99
 */
public record TransactionReportLine(
        String transactionId,
        String accountId,
        String typeCode,
        String typeDesc,
        Integer categoryCode,
        String categoryDesc,
        String source,
        BigDecimal amount) {
}
