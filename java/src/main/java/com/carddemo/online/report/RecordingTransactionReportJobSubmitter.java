package com.carddemo.online.report;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COBOL program: CORPT00C — stand-in for the internal reader (JOBS transient data queue).
 *
 * <p>The TRANREPT batch job has not been migrated yet, so an accepted report request is recorded
 * and logged instead of launched. {@link TransactionReportJobConfiguration} only registers this
 * implementation while no other {@link TransactionReportJobSubmitter} bean exists, so a Spring
 * Batch implementation takes over as soon as one is added.
 */
public class RecordingTransactionReportJobSubmitter implements TransactionReportJobSubmitter {

    private static final Logger log = LoggerFactory.getLogger(RecordingTransactionReportJobSubmitter.class);

    private final List<SubmittedReport> submitted = new CopyOnWriteArrayList<>();

    @Override
    public void submit(String reportName, String startDate, String endDate) {
        submitted.add(new SubmittedReport(reportName, startDate, endDate));
        log.info("TRANREPT job submitted: report={} startDate={} endDate={}", reportName, startDate, endDate);
    }

    /** The report requests accepted so far, oldest first. */
    public List<SubmittedReport> getSubmitted() {
        return List.copyOf(submitted);
    }

    /** One accepted report request, the equivalent of one job on the internal reader. */
    @Data
    @AllArgsConstructor
    public static class SubmittedReport {
        private final String reportName;
        private final String startDate;
        private final String endDate;
    }
}
