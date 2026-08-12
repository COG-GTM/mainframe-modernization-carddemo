package com.carddemo.online.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: CORPT00C — outcome of the CR00 screen (BMS map CORPT0A).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportResponse {

    /** ERR-FLG-OFF and the job was written to the JOBS TDQ. */
    private boolean success;

    /** ERRMSG PIC X(78). */
    private String message;

    /** WS-REPORT-NAME PIC X(10): Monthly, Yearly or Custom. */
    private String reportName;

    /** WS-START-DATE / PARM-START-DATE, YYYY-MM-DD. */
    private String startDate;

    /** WS-END-DATE / PARM-END-DATE, YYYY-MM-DD. */
    private String endDate;
}
