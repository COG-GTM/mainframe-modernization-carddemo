package com.cardemo.batch.model;

/**
 * Represents a single 133-byte report output line.
 * Maps to FD-REPTFILE-REC PIC X(133) from the COBOL program.
 */
public class ReportLine {

    public enum LineType {
        REPORT_NAME_HEADER,
        BLANK_LINE,
        TRANSACTION_HEADER_1,
        TRANSACTION_HEADER_2,
        DETAIL,
        PAGE_TOTALS,
        ACCOUNT_TOTALS,
        GRAND_TOTALS
    }

    private final LineType lineType;
    private final String content;

    public ReportLine(LineType lineType, String content) {
        this.lineType = lineType;
        this.content = content;
    }

    public LineType getLineType() {
        return lineType;
    }

    public String getContent() {
        return content;
    }
}
