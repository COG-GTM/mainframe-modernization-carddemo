package com.carddemo.online.report;

/**
 * COBOL program: CORPT00C — SUBMIT-JOB-TO-INTRDR / WIRTE-JOBSUB-TDQ.
 *
 * <p>CORPT00C builds the TRNRPT00 JCL (which runs the TRANREPT procedure over the TRANSACT file
 * between PARM-START-DATE and PARM-END-DATE) and writes it line by line to the CICS JOBS
 * transient data queue, i.e. the internal reader. This interface is the seam that replaces the
 * internal reader: once the TRANREPT batch job exists as a Spring Batch job on the Java side, an
 * implementation launching that job replaces {@link RecordingTransactionReportJobSubmitter}
 * without any change to {@link TransactionReportService}.
 */
public interface TransactionReportJobSubmitter {

    /**
     * Submits the transaction report job for an inclusive date range.
     *
     * @param reportName WS-REPORT-NAME: Monthly, Yearly or Custom
     * @param startDate PARM-START-DATE, YYYY-MM-DD
     * @param endDate PARM-END-DATE, YYYY-MM-DD
     */
    void submit(String reportName, String startDate, String endDate);
}
