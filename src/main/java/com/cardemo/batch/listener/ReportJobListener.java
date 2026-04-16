package com.cardemo.batch.listener;

import com.cardemo.batch.writer.TransactionReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * Job listener that handles report finalization.
 * On job completion, writes the final page totals, account totals, and grand totals —
 * equivalent to the end-of-file processing in the COBOL main loop (lines 197-204).
 */
public class ReportJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ReportJobListener.class);

    private final TransactionReportWriter reportWriter;

    public ReportJobListener(TransactionReportWriter reportWriter) {
        this.reportWriter = reportWriter;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("START OF EXECUTION OF PROGRAM CBTRN03C (Transaction Detail Report)");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {
            reportWriter.writeClosingTotals();
            reportWriter.close();
            log.info("END OF EXECUTION OF PROGRAM CBTRN03C (Transaction Detail Report)");
            log.info("Job status: {}", jobExecution.getStatus());
        } catch (Exception e) {
            log.error("Error writing closing totals", e);
        }
    }
}
