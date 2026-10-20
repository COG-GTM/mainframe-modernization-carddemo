package com.carddemo.batch.cbtrn03c;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.CardDemoFile;
import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.io.FileStatus;
import com.carddemo.batch.core.io.IndexedFileStore;
import com.carddemo.batch.core.io.IoResult;
import com.carddemo.batch.core.io.RecordFormat;
import com.carddemo.batch.core.io.SequentialFileReader;
import com.carddemo.batch.core.io.SequentialFileWriter;
import com.carddemo.batch.core.record.CardXrefRecord;
import com.carddemo.batch.core.record.TranCatRecord;
import com.carddemo.batch.core.record.TranRecord;
import com.carddemo.batch.core.record.TranTypeRecord;
import com.carddemo.batch.core.report.ReportAccountTotals;
import com.carddemo.batch.core.report.ReportGrandTotals;
import com.carddemo.batch.core.report.ReportLines;
import com.carddemo.batch.core.report.ReportNameHeader;
import com.carddemo.batch.core.report.ReportPageTotals;
import com.carddemo.batch.core.report.TransactionDetailLine;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Port of {@code app/cbl/CBTRN03C.cbl} (TRANREPT step 10): prints the transaction detail report
 * {@code TRANREPT} (133-byte {@code CVTRA07Y} lines) for the transactions in {@code TRANFILE} whose
 * {@code TRAN-PROC-TS} date lies in the {@code DATEPARM} range. The TRANREPT JCL feeds it the
 * transaction backup sorted by card number and already filtered to the date range by DFSORT.
 *
 * <p>The COBOL control flow is replicated as written, including these quirks:
 * <ul>
 *   <li><b>Date filter exits the loop.</b> An out-of-range record hits {@code NEXT SENTENCE}, which
 *       jumps past the period after {@code END-PERFORM}: the main loop ends, nothing more is read,
 *       and no page, account or grand totals are written. (GnuCOBOL behaves the same way.)</li>
 *   <li><b>Last record counted twice.</b> At end of file {@code TRAN-RECORD} still holds the last
 *       record read, and the EOF branch adds its {@code TRAN-AMT} to the page and account totals
 *       again before writing the page totals (which also adds the page total to the grand total)
 *       and the grand totals.</li>
 *   <li><b>No final account total.</b> Account totals are only written on a card-number change
 *       (and not before the first card), so the last card's account total never prints.</li>
 *   <li><b>Page breaks can be skipped.</b> {@code WS-LINE-COUNTER} counts every line written
 *       except the grand total (headers 4, account totals 2, page totals 2, detail 1) and the
 *       {@code MOD(WS-LINE-COUNTER, 20) = 0} test runs only before a detail line, so a page break
 *       is missed when an account total moves the counter past a multiple of 20.</li>
 *   <li><b>Headers.</b> The first detail writes the headers (name header with the date range,
 *       blank line, header 1, the all-'-' header 2); every page break writes page totals plus the
 *       '-' line and then the same four header lines. The account break and the xref lookup happen
 *       before the first-time headers.</li>
 *   <li><b>Lookups.</b> CARDXREF is read only when the card number changes (its account id is
 *       reused for the following details); TRANTYPE and TRANCATG are read for every detail. A
 *       missing key abends (U0999) via {@code 9999-ABEND-PROGRAM}.</li>
 *   <li><b>Empty input.</b> An empty DATEPARM skips the loop; an empty TRANFILE compares the
 *       initial blank {@code TRAN-PROC-TS} against the range, which exits the loop. Either way the
 *       report is empty.</li>
 *   <li>Totals are {@code S9(09)V99}: ADDs truncate high-order digits on overflow (no
 *       {@code ON SIZE ERROR}). Dates compare as alphanumerics (ASCII collation here; it orders
 *       {@code YYYY-MM-DD} dates the same way EBCDIC does).</li>
 * </ul>
 * The program does not read the system clock, so no {@code Clock} is needed.
 */
public final class Cbtrn03cJob {

    public static final String TRANFILE = "tranfile.txt";
    public static final String DATEPARM = "dateparm.txt";
    public static final String REPORT_FILE = "tranrept.txt";
    static final int DATEPARM_LENGTH = 80;
    static final int PAGE_SIZE = 20;

    private static final TranRecord INITIAL_TRAN_RECORD = new TranRecord(
            "", "", 0, "", "", BigDecimal.ZERO, 0, "", "", "", "", "", "", "");

    private final Path tranPath;
    private final Path cardXrefPath;
    private final Path tranTypePath;
    private final Path tranCatgPath;
    private final Path dateParmPath;
    private final Path reportPath;
    private final PrintStream sysout;

    private SequentialFileReader<TranRecord> transactFile;
    private SequentialFileWriter<byte[]> reportWriter;
    private IndexedFileStore<String, CardXrefRecord> xrefFile;
    private IndexedFileStore<String, TranTypeRecord> tranTypeFile;
    private IndexedFileStore<String, TranCatRecord> tranCatgFile;

    private TranRecord tranRecord;
    private CardXrefRecord cardXrefRecord;
    private TranTypeRecord tranTypeRecord;
    private TranCatRecord tranCatRecord;
    private String startDate;
    private String endDate;
    private boolean endOfFile;
    private boolean firstTime;
    private long lineCounter;
    private BigDecimal pageTotal;
    private BigDecimal accountTotal;
    private BigDecimal grandTotal;
    private String currCardNum;
    private final RecordFormat tranFormat;

    public Cbtrn03cJob(Path tranPath, Path cardXrefPath, Path tranTypePath, Path tranCatgPath,
            Path dateParmPath, Path reportPath, PrintStream sysout) {
        this(tranPath, RecordFormat.LINE_SEQUENTIAL, cardXrefPath, tranTypePath, tranCatgPath, dateParmPath,
                reportPath, sysout);
    }

    /**
     * {@code tranFormat} is {@link RecordFormat#FIXED} for a raw RECFM=F TRANFILE (350-byte records with no
     * delimiter, as TRANREPT's sort step writes it) or {@link RecordFormat#LINE_SEQUENTIAL} for ASCII text.
     */
    public Cbtrn03cJob(Path tranPath, RecordFormat tranFormat, Path cardXrefPath, Path tranTypePath,
            Path tranCatgPath, Path dateParmPath, Path reportPath, PrintStream sysout) {
        this.tranPath = tranPath;
        this.tranFormat = tranFormat;
        this.cardXrefPath = cardXrefPath;
        this.tranTypePath = tranTypePath;
        this.tranCatgPath = tranCatgPath;
        this.dateParmPath = dateParmPath;
        this.reportPath = reportPath;
        this.sysout = sysout;
    }

    /**
     * Job wired to {@link BatchConfig}: CARDXREF/TRANTYPE/TRANCATG from the input directory, the
     * report to {@code <outputDir>/tranrept.txt}. {@code args[0]} overrides the TRANFILE path
     * (default {@code <inputDir>/tranfile.txt}, normally CBTRN02C's posted transactions) and
     * {@code args[1]} the DATEPARM path (default {@code <inputDir>/dateparm.txt}). {@code args[2]} is the
     * TRANFILE {@link RecordFormat} name, {@code LINE_SEQUENTIAL} (default) or {@code FIXED}.
     */
    public static Cbtrn03cJob fromConfig(BatchConfig config, String[] args, PrintStream sysout) {
        Path tran = args.length > 0 ? Paths.get(args[0]) : config.input(TRANFILE);
        Path dateParm = args.length > 1 ? Paths.get(args[1]) : config.input(DATEPARM);
        RecordFormat format = args.length > 2 ? RecordFormat.valueOf(args[2]) : RecordFormat.LINE_SEQUENTIAL;
        return new Cbtrn03cJob(tran, format, config.input(CardDemoFile.CARDXREF), config.input(CardDemoFile.TRANTYPE),
                config.input(CardDemoFile.TRANCATG), dateParm, config.output(REPORT_FILE), sysout);
    }

    public static void main(String[] args) {
        try {
            fromConfig(BatchConfig.fromEnvironment(), args, System.out).run();
        } catch (Cbtrn03cAbend e) {
            System.err.println("CBTRN03C abended (U" + e.abendCode() + "): " + e.getMessage());
            System.exit(1);
        }
    }

    public Path reportFile() {
        return reportPath;
    }

    /** The PROCEDURE DIVISION mainline. */
    public void run() {
        tranRecord = INITIAL_TRAN_RECORD;
        cardXrefRecord = null;
        tranTypeRecord = null;
        tranCatRecord = null;
        startDate = " ".repeat(10);
        endDate = " ".repeat(10);
        endOfFile = false;
        firstTime = true;
        lineCounter = 0;
        pageTotal = BigDecimal.ZERO.setScale(2);
        accountTotal = BigDecimal.ZERO.setScale(2);
        grandTotal = BigDecimal.ZERO.setScale(2);
        currCardNum = " ".repeat(16);

        sysout.println("START OF EXECUTION OF PROGRAM CBTRN03C");
        transactFile = open(tranPath, "ERROR OPENING TRANFILE",
                p -> new SequentialFileReader<>(p, TranRecord.LENGTH, TranRecord::fromBytes, tranFormat)); // 0000-TRANFILE-OPEN
        try {
            reportWriter = openReport(); // 0100-REPTFILE-OPEN
            try {
                xrefFile = open(cardXrefPath, "ERROR OPENING CROSS REF FILE", p -> IndexedFileStore.load(
                        p, CardXrefRecord.LENGTH, CardXrefRecord::fromBytes, CardXrefRecord::cardNum)); // 0200
                tranTypeFile = open(tranTypePath, "ERROR OPENING TRANSACTION TYPE FILE", p -> IndexedFileStore.load(
                        p, TranTypeRecord.LENGTH, TranTypeRecord::fromBytes, TranTypeRecord::tranType)); // 0300
                tranCatgFile = open(tranCatgPath, "ERROR OPENING TRANSACTION CATG FILE", p -> IndexedFileStore.load(
                        p, TranCatRecord.LENGTH, TranCatRecord::fromBytes, TranCatRecord::key)); // 0400
                try (SequentialFileReader<byte[]> dateParms = open(dateParmPath, "ERROR OPENING DATE PARM FILE",
                        p -> new SequentialFileReader<>(p, DATEPARM_LENGTH, Function.identity()))) { // 0500
                    dateParmRead(dateParms); // 0550-DATEPARM-READ
                    mainLoop();
                }
            } finally {
                reportWriter.close(); // 9100-REPTFILE-CLOSE
            }
        } finally {
            transactFile.close(); // 9000-TRANFILE-CLOSE
        }
        sysout.println("END OF EXECUTION OF PROGRAM CBTRN03C");
    }

    private void mainLoop() {
        while (!endOfFile) {
            tranFileGetNext(); // 1000-TRANFILE-GET-NEXT
            String procDate = tranRecord.procTs().substring(0, 10);
            if (!(procDate.compareTo(startDate) >= 0 && procDate.compareTo(endDate) <= 0)) {
                // NEXT SENTENCE: control passes to the sentence after END-PERFORM, ending the loop.
                return;
            }
            if (!endOfFile) {
                sysout.println(FixedWidth.decode(tranRecord.toBytes())); // DISPLAY TRAN-RECORD
                if (!currCardNum.equals(tranRecord.cardNum())) {
                    if (!firstTime) {
                        writeAccountTotals(); // 1120-WRITE-ACCOUNT-TOTALS
                    }
                    currCardNum = tranRecord.cardNum();
                    lookupXref(tranRecord.cardNum()); // 1500-A-LOOKUP-XREF
                }
                lookupTranType(tranRecord.typeCd()); // 1500-B-LOOKUP-TRANTYPE
                lookupTranCatg(TranCatRecord.key(tranRecord.typeCd(), tranRecord.catCd())); // 1500-C
                writeTransactionReport(); // 1100-WRITE-TRANSACTION-REPORT
            } else {
                sysout.println("TRAN-AMT " + CobolDecimal.formatSigned(tranRecord.amt(), 9, 2));
                sysout.println("WS-PAGE-TOTAL" + CobolDecimal.formatSigned(pageTotal, 9, 2));
                // ADD TRAN-AMT TO WS-PAGE-TOTAL WS-ACCOUNT-TOTAL: the last record is added a second time.
                pageTotal = add(pageTotal, tranRecord.amt());
                accountTotal = add(accountTotal, tranRecord.amt());
                writePageTotals(); // 1110-WRITE-PAGE-TOTALS
                writeGrandTotals(); // 1110-WRITE-GRAND-TOTALS
            }
        }
    }

    // 0550-DATEPARM-READ: READ DATE-PARMS-FILE INTO WS-DATEPARM-RECORD (start X(10), filler X(1), end X(10)).
    private void dateParmRead(SequentialFileReader<byte[]> dateParms) {
        IoResult<byte[]> result = dateParms.read();
        if (result.isOk()) {
            String rec = FixedWidth.decode(result.record());
            startDate = rec.substring(0, 10);
            endDate = rec.substring(11, 21);
            sysout.println("Reporting from " + startDate + " to " + endDate);
        } else if (result.isEndOfFile()) {
            endOfFile = true;
        } else {
            abend("ERROR READING DATEPARM FILE", result.status());
        }
    }

    // 1000-TRANFILE-GET-NEXT: READ ... INTO TRAN-RECORD; at end of file TRAN-RECORD keeps the last record.
    private void tranFileGetNext() {
        IoResult<TranRecord> result = transactFile.read();
        if (result.isOk()) {
            tranRecord = result.record();
        } else if (result.isEndOfFile()) {
            endOfFile = true;
        } else {
            abend("ERROR READING TRANSACTION FILE", result.status());
        }
    }

    // 1100-WRITE-TRANSACTION-REPORT
    private void writeTransactionReport() {
        if (firstTime) {
            firstTime = false;
            writeHeaders(); // 1120-WRITE-HEADERS
        }
        if (lineCounter % PAGE_SIZE == 0) {
            writePageTotals();
            writeHeaders();
        }
        pageTotal = add(pageTotal, tranRecord.amt());
        accountTotal = add(accountTotal, tranRecord.amt());
        writeDetail(); // 1120-WRITE-DETAIL
    }

    // 1110-WRITE-PAGE-TOTALS
    private void writePageTotals() {
        writeReportRec(new ReportPageTotals(pageTotal).toBytes());
        grandTotal = add(grandTotal, pageTotal);
        pageTotal = BigDecimal.ZERO.setScale(2);
        lineCounter++;
        writeReportRec(ReportLines.transactionHeader2());
        lineCounter++;
    }

    // 1120-WRITE-ACCOUNT-TOTALS
    private void writeAccountTotals() {
        writeReportRec(new ReportAccountTotals(accountTotal).toBytes());
        accountTotal = BigDecimal.ZERO.setScale(2);
        lineCounter++;
        writeReportRec(ReportLines.transactionHeader2());
        lineCounter++;
    }

    // 1110-WRITE-GRAND-TOTALS (does not touch WS-LINE-COUNTER)
    private void writeGrandTotals() {
        writeReportRec(new ReportGrandTotals(grandTotal).toBytes());
    }

    // 1120-WRITE-HEADERS
    private void writeHeaders() {
        writeReportRec(new ReportNameHeader(startDate, endDate).toBytes());
        lineCounter++;
        writeReportRec(ReportLines.blankLine());
        lineCounter++;
        writeReportRec(ReportLines.transactionHeader1());
        lineCounter++;
        writeReportRec(ReportLines.transactionHeader2());
        lineCounter++;
    }

    // 1120-WRITE-DETAIL
    private void writeDetail() {
        TransactionDetailLine detail = new TransactionDetailLine(
                tranRecord.id(),
                CobolDecimal.formatUnsigned(cardXrefRecord.acctId(), 11), // MOVE XREF-ACCT-ID 9(11) TO X(11)
                tranRecord.typeCd(),
                tranTypeRecord.tranTypeDesc(),
                tranRecord.catCd(),
                tranCatRecord.tranCatTypeDesc(),
                tranRecord.source(),
                tranRecord.amt());
        writeReportRec(detail.toBytes());
        lineCounter++;
    }

    // 1111-WRITE-REPORT-REC
    private void writeReportRec(byte[] line) {
        String status = reportWriter.write(line);
        if (!FileStatus.OK.equals(status)) {
            abend("ERROR WRITING REPTFILE", status);
        }
    }

    // 1500-A-LOOKUP-XREF
    private void lookupXref(String cardNum) {
        IoResult<CardXrefRecord> result = xrefFile.read(cardNum);
        if (!result.isOk()) {
            abend("INVALID CARD NUMBER : " + cardNum, FileStatus.NOT_FOUND);
        }
        cardXrefRecord = result.record();
    }

    // 1500-B-LOOKUP-TRANTYPE
    private void lookupTranType(String typeCd) {
        IoResult<TranTypeRecord> result = tranTypeFile.read(typeCd);
        if (!result.isOk()) {
            abend("INVALID TRANSACTION TYPE : " + typeCd, FileStatus.NOT_FOUND);
        }
        tranTypeRecord = result.record();
    }

    // 1500-C-LOOKUP-TRANCATG
    private void lookupTranCatg(String key) {
        IoResult<TranCatRecord> result = tranCatgFile.read(key);
        if (!result.isOk()) {
            abend("INVALID TRAN CATG KEY : " + key, FileStatus.NOT_FOUND);
        }
        tranCatRecord = result.record();
    }

    private SequentialFileWriter<byte[]> openReport() {
        try {
            return new SequentialFileWriter<>(reportPath, ReportLines.LENGTH, Function.identity());
        } catch (UncheckedIOException e) {
            abend("ERROR OPENING REPTFILE", "30");
            throw e;
        }
    }

    private <T> T open(Path path, String error, Function<Path, T> opener) {
        if (!Files.isRegularFile(path)) {
            abend(error, FileStatus.FILE_NOT_FOUND);
        }
        return opener.apply(path);
    }

    private static BigDecimal add(BigDecimal total, BigDecimal amount) {
        return CobolDecimal.toField(total.add(amount), 9, 2);
    }

    // 9910-DISPLAY-IO-STATUS + 9999-ABEND-PROGRAM
    private void abend(String message, String ioStatus) {
        sysout.println(message);
        sysout.println("FILE STATUS IS: NNNN00" + ioStatus);
        sysout.println("ABENDING PROGRAM");
        throw new Cbtrn03cAbend(message + " (file status " + ioStatus + ")");
    }
}
