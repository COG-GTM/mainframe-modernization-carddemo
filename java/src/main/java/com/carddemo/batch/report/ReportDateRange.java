package com.carddemo.batch.report;

import com.carddemo.util.CobolUtils;

/**
 * COBOL program: CBTRN03C — WS-DATEPARM-RECORD read by 0550-DATEPARM-READ from the DATEPARM
 * sequential file (PIC X(10) start date, one filler byte, PIC X(10) end date).
 *
 * @param startDate WS-START-DATE, {@code yyyy-MM-dd}
 * @param endDate   WS-END-DATE, {@code yyyy-MM-dd}
 */
public record ReportDateRange(String startDate, String endDate) {

    /** Parses one 80-byte DATEPARM record following the WS-DATEPARM-RECORD layout. */
    public static ReportDateRange parse(String record) {
        return new ReportDateRange(CobolUtils.str(record, 0, 10), CobolUtils.str(record, 11, 10));
    }

    /**
     * {@code IF TRAN-PROC-TS (1:10) >= WS-START-DATE AND TRAN-PROC-TS (1:10) <= WS-END-DATE}:
     * an alphanumeric comparison of the first ten bytes of the processing timestamp.
     */
    public boolean contains(String processingTimestamp) {
        String date = CobolUtils.padRight(processingTimestamp, 10).substring(0, 10);
        return date.compareTo(CobolUtils.padRight(startDate, 10)) >= 0
                && date.compareTo(CobolUtils.padRight(endDate, 10)) <= 0;
    }
}
