package com.cardemo.batch.service;

import com.cardemo.batch.model.ReportLine;
import com.cardemo.batch.model.ReportLine.LineType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Formats report lines to match the 133-byte COBOL report record structure
 * defined in CVTRA07Y.cpy.
 */
@Service
public class ReportFormatterService {

    private static final int RECORD_LENGTH = 133;

    /**
     * REPORT-NAME-HEADER: 38 + 41 + 12 + 10 + 4 + 10 = 115 chars
     * Padded to 133.
     */
    public ReportLine formatReportNameHeader(String startDate, String endDate) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(padRight("DALYREPT", 38));
        sb.append(padRight("Daily Transaction Report", 41));
        sb.append("Date Range: ");
        sb.append(padRight(startDate, 10));
        sb.append(" to ");
        sb.append(padRight(endDate, 10));
        String content = padRight(sb.toString(), RECORD_LENGTH);
        return new ReportLine(LineType.REPORT_NAME_HEADER, content);
    }

    /**
     * TRANSACTION-HEADER-1: Column headers line.
     */
    public ReportLine formatTransactionHeader1() {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(padRight("Transaction ID", 17));
        sb.append(padRight("Account ID", 12));
        sb.append(padRight("Transaction Type", 19));
        sb.append(padRight("Tran Category", 35));
        sb.append(padRight("Tran Source", 14));
        sb.append(" ");
        sb.append(padRight("        Amount", 16));
        String content = padRight(sb.toString(), RECORD_LENGTH);
        return new ReportLine(LineType.TRANSACTION_HEADER_1, content);
    }

    /**
     * TRANSACTION-HEADER-2: Separator line of dashes.
     */
    public ReportLine formatTransactionHeader2() {
        String content = "-".repeat(RECORD_LENGTH);
        return new ReportLine(LineType.TRANSACTION_HEADER_2, content);
    }

    /**
     * Blank line.
     */
    public ReportLine formatBlankLine() {
        String content = " ".repeat(RECORD_LENGTH);
        return new ReportLine(LineType.BLANK_LINE, content);
    }

    /**
     * TRANSACTION-DETAIL-REPORT from CVTRA07Y:
     *   TRAN-REPORT-TRANS-ID     PIC X(16)
     *   FILLER                   PIC X(01)
     *   TRAN-REPORT-ACCOUNT-ID   PIC X(11)
     *   FILLER                   PIC X(01)
     *   TRAN-REPORT-TYPE-CD      PIC X(02)
     *   FILLER '-'               PIC X(01)
     *   TRAN-REPORT-TYPE-DESC    PIC X(15)
     *   FILLER                   PIC X(01)
     *   TRAN-REPORT-CAT-CD       PIC 9(04)
     *   FILLER '-'               PIC X(01)
     *   TRAN-REPORT-CAT-DESC     PIC X(29)
     *   FILLER                   PIC X(01)
     *   TRAN-REPORT-SOURCE       PIC X(10)
     *   FILLER                   PIC X(04)
     *   TRAN-REPORT-AMT          PIC -ZZZ,ZZZ,ZZZ.ZZ (15 chars)
     *   FILLER                   PIC X(02)
     *   Total = 16+1+11+1+2+1+15+1+4+1+29+1+10+4+15+2 = 114 ... padded to 133
     */
    public ReportLine formatDetailLine(String transId, String accountId,
                                       String typeCd, String typeDesc,
                                       int catCd, String catDesc,
                                       String source, BigDecimal amount) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(padRight(transId, 16));
        sb.append(" ");
        sb.append(padRight(accountId, 11));
        sb.append(" ");
        sb.append(padRight(typeCd, 2));
        sb.append("-");
        sb.append(padRight(typeDesc, 15));
        sb.append(" ");
        sb.append(String.format("%04d", catCd));
        sb.append("-");
        sb.append(padRight(catDesc, 29));
        sb.append(" ");
        sb.append(padRight(source, 10));
        sb.append("    ");
        sb.append(formatAmount(amount));
        sb.append("  ");
        String content = padRight(sb.toString(), RECORD_LENGTH);
        return new ReportLine(LineType.DETAIL, content);
    }

    /**
     * REPORT-PAGE-TOTALS from CVTRA07Y:
     *   'Page Total'  PIC X(11)
     *   dots          PIC X(86) VALUE ALL '.'
     *   amount        PIC +ZZZ,ZZZ,ZZZ.ZZ (15 chars)
     *   Remaining to fill 133: 11 + 86 + 15 = 112... padded
     */
    public ReportLine formatPageTotals(BigDecimal pageTotal) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(padRight("Page Total", 11));
        sb.append(".".repeat(86));
        sb.append(formatSignedAmount(pageTotal));
        String content = padRight(sb.toString(), RECORD_LENGTH);
        return new ReportLine(LineType.PAGE_TOTALS, content);
    }

    /**
     * REPORT-ACCOUNT-TOTALS:
     *   'Account Total'  PIC X(13)
     *   dots             PIC X(84) VALUE ALL '.'
     *   amount           PIC +ZZZ,ZZZ,ZZZ.ZZ (15 chars)
     */
    public ReportLine formatAccountTotals(BigDecimal accountTotal) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(padRight("Account Total", 13));
        sb.append(".".repeat(84));
        sb.append(formatSignedAmount(accountTotal));
        String content = padRight(sb.toString(), RECORD_LENGTH);
        return new ReportLine(LineType.ACCOUNT_TOTALS, content);
    }

    /**
     * REPORT-GRAND-TOTALS:
     *   'Grand Total'  PIC X(11)
     *   dots           PIC X(86) VALUE ALL '.'
     *   amount         PIC +ZZZ,ZZZ,ZZZ.ZZ (15 chars)
     */
    public ReportLine formatGrandTotals(BigDecimal grandTotal) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(padRight("Grand Total", 11));
        sb.append(".".repeat(86));
        sb.append(formatSignedAmount(grandTotal));
        String content = padRight(sb.toString(), RECORD_LENGTH);
        return new ReportLine(LineType.GRAND_TOTALS, content);
    }

    /**
     * Formats amount as -ZZZ,ZZZ,ZZZ.ZZ (15 chars, right-aligned).
     * Negative amounts show leading '-'; positive amounts show leading space.
     */
    public String formatAmount(BigDecimal amount) {
        String formatted = String.format("%,.2f", amount);
        if (amount.signum() >= 0) {
            formatted = " " + formatted;
        }
        return padLeft(formatted, 15);
    }

    /**
     * Formats amount as +ZZZ,ZZZ,ZZZ.ZZ (15 chars, right-aligned).
     * Positive amounts show '+'; negative show '-'.
     */
    public String formatSignedAmount(BigDecimal amount) {
        String formatted = String.format("%,.2f", amount.abs());
        String sign = amount.signum() >= 0 ? "+" : "-";
        formatted = sign + formatted;
        return padLeft(formatted, 15);
    }

    private String padRight(String value, int length) {
        if (value == null) {
            return " ".repeat(length);
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return value + " ".repeat(length - value.length());
    }

    private String padLeft(String value, int length) {
        if (value == null) {
            return " ".repeat(length);
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return " ".repeat(length - value.length()) + value;
    }
}
