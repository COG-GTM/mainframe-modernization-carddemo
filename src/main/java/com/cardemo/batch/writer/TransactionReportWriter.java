package com.cardemo.batch.writer;

import com.cardemo.batch.model.CardXrefRecord;
import com.cardemo.batch.model.ReportLine;
import com.cardemo.batch.model.TranCategoryRecord;
import com.cardemo.batch.model.TranTypeRecord;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.service.ReferenceDataService;
import com.cardemo.batch.service.ReportFormatterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom ItemWriter that produces the 133-byte fixed-width transaction detail report.
 * Implements the full report logic from CBTRN03C.CBL including:
 * - Page break at WS-PAGE-SIZE (20) lines with headers/footers
 * - Account break totals when card number changes
 * - Three levels of totals: page, account, grand
 * - Reference table lookups for type/category descriptions and XREF account ID
 */
public class TransactionReportWriter implements ItemWriter<TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportWriter.class);

    private final BufferedWriter writer;
    private final ReportFormatterService formatter;
    private final ReferenceDataService referenceDataService;
    private final String startDate;
    private final String endDate;
    private final int pageSize;

    private boolean firstTime = true;
    private int lineCounter = 0;
    private BigDecimal pageTotal = BigDecimal.ZERO;
    private BigDecimal accountTotal = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private String currentCardNum = "";
    private String currentAccountId = "";

    public TransactionReportWriter(BufferedWriter writer,
                                   ReportFormatterService formatter,
                                   ReferenceDataService referenceDataService,
                                   String startDate,
                                   String endDate,
                                   int pageSize) {
        this.writer = writer;
        this.formatter = formatter;
        this.referenceDataService = referenceDataService;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pageSize = pageSize;
    }

    @Override
    public void write(Chunk<? extends TransactionRecord> chunk) throws Exception {
        for (TransactionRecord tran : chunk) {
            writeTransactionReport(tran);
        }
    }

    /**
     * Main report writing logic — equivalent to 1100-WRITE-TRANSACTION-REPORT.
     */
    private void writeTransactionReport(TransactionRecord tran) throws IOException {
        // First time: write report header
        if (firstTime) {
            firstTime = false;
            writeHeaders();
        }

        // Page break logic: when lineCounter mod pageSize == 0 (and not first line)
        if (lineCounter > 0 && lineCounter % pageSize == 0) {
            writePageTotals();
            writeHeaders();
        }

        // Account break logic: when card number changes
        if (!currentCardNum.equals(tran.getTranCardNum())) {
            if (!currentCardNum.isEmpty()) {
                writeAccountTotals();
            }
            currentCardNum = tran.getTranCardNum();
            CardXrefRecord xref = referenceDataService.lookupCardXref(tran.getTranCardNum());
            currentAccountId = xref.getXrefAcctId();
        }

        // Accumulate totals
        pageTotal = pageTotal.add(tran.getTranAmt());
        accountTotal = accountTotal.add(tran.getTranAmt());

        // Lookup reference data
        TranTypeRecord typeRec = referenceDataService.lookupTranType(tran.getTranTypeCd());
        TranCategoryRecord catRec = referenceDataService.lookupTranCategory(
                tran.getTranTypeCd(), tran.getTranCatCd());

        // Write detail line
        writeDetailLine(tran, typeRec, catRec);
    }

    /**
     * 1120-WRITE-DETAIL: Write a transaction detail line.
     */
    private void writeDetailLine(TransactionRecord tran,
                                 TranTypeRecord typeRec,
                                 TranCategoryRecord catRec) throws IOException {
        ReportLine line = formatter.formatDetailLine(
                tran.getTranId(),
                currentAccountId,
                tran.getTranTypeCd(),
                typeRec.getTranTypeDesc(),
                tran.getTranCatCd(),
                catRec.getTranCatTypeDesc(),
                tran.getTranSource(),
                tran.getTranAmt()
        );
        writeReportRec(line);
        lineCounter++;
    }

    /**
     * 1120-WRITE-HEADERS: Write report name header, blank line, column headers, separator.
     */
    private void writeHeaders() throws IOException {
        writeReportRec(formatter.formatReportNameHeader(startDate, endDate));
        lineCounter++;

        writeReportRec(formatter.formatBlankLine());
        lineCounter++;

        writeReportRec(formatter.formatTransactionHeader1());
        lineCounter++;

        writeReportRec(formatter.formatTransactionHeader2());
        lineCounter++;
    }

    /**
     * 1110-WRITE-PAGE-TOTALS: Write page total, accumulate into grand total, reset page total.
     */
    private void writePageTotals() throws IOException {
        ReportLine pageTotalLine = formatter.formatPageTotals(pageTotal);
        writeReportRec(pageTotalLine);
        grandTotal = grandTotal.add(pageTotal);
        pageTotal = BigDecimal.ZERO;
        lineCounter++;

        writeReportRec(formatter.formatTransactionHeader2());
        lineCounter++;
    }

    /**
     * 1120-WRITE-ACCOUNT-TOTALS: Write account total and reset.
     */
    private void writeAccountTotals() throws IOException {
        ReportLine accountTotalLine = formatter.formatAccountTotals(accountTotal);
        writeReportRec(accountTotalLine);
        accountTotal = BigDecimal.ZERO;
        lineCounter++;

        writeReportRec(formatter.formatTransactionHeader2());
        lineCounter++;
    }

    /**
     * Write the final page totals and grand totals at end-of-file.
     * Called from the job listener on completion.
     */
    public void writeClosingTotals() throws IOException {
        // Final page total
        ReportLine pageTotalLine = formatter.formatPageTotals(pageTotal);
        writeReportRec(pageTotalLine);
        grandTotal = grandTotal.add(pageTotal);
        pageTotal = BigDecimal.ZERO;

        // Final account total
        if (!currentCardNum.isEmpty()) {
            writeAccountTotals();
        }

        // Grand total
        ReportLine grandTotalLine = formatter.formatGrandTotals(grandTotal);
        writeReportRec(grandTotalLine);

        writer.flush();
    }

    /**
     * 1111-WRITE-REPORT-REC: Write a single 133-byte record.
     */
    private void writeReportRec(ReportLine line) throws IOException {
        writer.write(line.getContent());
        writer.newLine();
    }

    public void close() throws IOException {
        writer.close();
    }

    // Accessors for testing
    public BigDecimal getPageTotal() {
        return pageTotal;
    }

    public BigDecimal getAccountTotal() {
        return accountTotal;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public int getLineCounter() {
        return lineCounter;
    }

    public String getCurrentCardNum() {
        return currentCardNum;
    }
}
