package com.cardemo.batch.processor;

import com.cardemo.batch.model.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * Filters transactions by date range, equivalent to the COBOL logic:
 *   IF TRAN-PROC-TS (1:10) >= WS-START-DATE
 *      AND TRAN-PROC-TS (1:10) <= WS-END-DATE
 *
 * Transactions outside the range are filtered (return null to skip).
 */
public class DateRangeFilterProcessor implements ItemProcessor<TransactionRecord, TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(DateRangeFilterProcessor.class);

    private final String startDate;
    private final String endDate;

    public DateRangeFilterProcessor(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public TransactionRecord process(TransactionRecord item) {
        String procDate = extractDate(item.getTranProcTs());

        if (procDate.compareTo(startDate) >= 0 && procDate.compareTo(endDate) <= 0) {
            return item;
        }

        log.debug("Filtered transaction {} with proc date {} (range: {} to {})",
                item.getTranId(), procDate, startDate, endDate);
        return null; // Spring Batch skips null items
    }

    /**
     * Extracts the first 10 characters (date portion) from the processing timestamp.
     * Equivalent to TRAN-PROC-TS(1:10) in COBOL.
     */
    private String extractDate(String procTs) {
        if (procTs == null || procTs.length() < 10) {
            return "";
        }
        return procTs.substring(0, 10);
    }
}
