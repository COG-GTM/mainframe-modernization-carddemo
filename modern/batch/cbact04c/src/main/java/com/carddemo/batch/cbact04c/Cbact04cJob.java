package com.carddemo.batch.cbact04c;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.CardDemoFile;
import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.io.IndexedFileStore;
import com.carddemo.batch.core.io.IoResult;
import com.carddemo.batch.core.io.SequentialFileWriter;
import com.carddemo.batch.core.record.AccountRecord;
import com.carddemo.batch.core.record.CardXrefRecord;
import com.carddemo.batch.core.record.DiscGroupRecord;
import com.carddemo.batch.core.record.TranCatBalRecord;
import com.carddemo.batch.core.record.TranRecord;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.util.Objects;

/**
 * Port of {@code app/cbl/CBACT04C.cbl} (JCL {@code INTCALC}, {@code PARM='2022071800'}): the
 * monthly interest calculator.
 *
 * <p>Reads the transaction category balance file (TCATBALF) sequentially in TRAN-CAT-KEY order,
 * looks up the interest rate of each balance in the disclosure group file (DISCGRP), writes one
 * system-generated interest transaction per balance with a non-zero rate (TRANSACT) and, on each
 * change of account, adds the accumulated interest to the account (ACCTFILE) and zeroes its cycle
 * credit/debit.
 *
 * <p>Files:
 * <ul>
 *   <li>inputs ({@link BatchConfig#input}): {@code tcatbal.txt}, {@code cardxref.txt},
 *       {@code acctdata.txt}, {@code discgrp.txt};</li>
 *   <li>outputs ({@link BatchConfig#output}): {@value #TRANSACT_FILE} (TRANSACT DD,
 *       {@code SYSTRAN(+1)}, 350-byte TRAN-RECORDs, line sequential) and {@value #ACCOUNT_FILE}
 *       (the ACCTFILE KSDS after the run).</li>
 * </ul>
 *
 * <p>Behaviour kept exactly as the COBOL has it:
 * <ul>
 *   <li>The account break fires only when a record of a <em>new</em> account is read. At end of
 *       file the loop ends without another break, so the interest accumulated for the last account
 *       in TCATBALF is never added to it (the last account is not rewritten).</li>
 *   <li>The DISCGRP READ falls back to the {@code DEFAULT} group (same type/category) on status
 *       '23'; a balance whose DIS-INT-RATE is zero writes no transaction.</li>
 *   <li>WS-MONTHLY-INT is truncated (no ROUNDED) to {@code S9(09)V99}.</li>
 * </ul>
 */
public final class Cbact04cJob {

    public static final String PROGRAM_NAME = "CBACT04C";
    /** Output file for the TRANSACT DD ({@code AWS.M2.CARDDEMO.SYSTRAN(+1)}). */
    public static final String TRANSACT_FILE = "systran.txt";
    /** Output file holding the ACCTFILE KSDS after the run. */
    public static final String ACCOUNT_FILE = CardDemoFile.ACCTDATA.fileName();
    public static final String PARM_DATE_PROPERTY = "carddemo.parmDate";
    public static final String PARM_DATE_ENV = "CARDDEMO_PARM_DATE";
    public static final int PARM_DATE_LENGTH = 10;

    private static final BigDecimal TWELVE_HUNDRED = BigDecimal.valueOf(1200);
    private static final String TRAN_TYPE_CD = "01";
    private static final int TRAN_CAT_CD = 5;
    private static final String TRAN_SOURCE = "System";
    private static final String TRAN_DESC_PREFIX = "Int. for a/c ";

    private final BatchConfig config;
    private final String parmDate;
    private final Clock clock;
    private final PrintStream sysout;

    // WS-MISC-VARS / WS-COUNTERS
    private String wsLastAcctNum;
    private BigDecimal wsMonthlyInt;
    private BigDecimal wsTotalInt;
    private boolean wsFirstTime;
    private long wsRecordCount;
    private int wsTranidSuffix;
    private String endOfFile;

    // Records read INTO working storage
    private AccountRecord accountRecord;
    private CardXrefRecord cardXrefRecord;
    private DiscGroupRecord disGroupRecord;

    private IndexedFileStore<String, TranCatBalRecord> tcatbalFile;
    private IndexedFileStore<String, CardXrefRecord> xrefFile;
    private IndexedFileStore<Long, AccountRecord> accountFile;
    private IndexedFileStore<String, DiscGroupRecord> discgrpFile;
    private SequentialFileWriter<TranRecord> transactFile;
    private long transactionsWritten;
    private long accountsRewritten;

    public Cbact04cJob(BatchConfig config, String parmDate, Clock clock, PrintStream sysout) {
        this.config = Objects.requireNonNull(config);
        this.parmDate = FixedWidth.fit(Objects.requireNonNull(parmDate, "parmDate"), PARM_DATE_LENGTH);
        this.clock = Objects.requireNonNull(clock);
        this.sysout = Objects.requireNonNull(sysout);
    }

    public static void main(String[] args) {
        Result result = new Cbact04cJob(BatchConfig.fromEnvironment(), resolveParmDate(args),
                Clock.systemDefaultZone(), System.out).run();
        System.out.println("TCATBAL RECORDS READ: " + result.recordCount()
                + ", TRANSACTIONS WRITTEN: " + result.transactionsWritten()
                + ", ACCOUNTS REWRITTEN: " + result.accountsRewritten());
        System.out.println("TRANSACTIONS: " + result.transactFile());
        System.out.println("ACCOUNTS:     " + result.accountFile());
    }

    /**
     * PARM-DATE (the JCL {@code PARM}): {@code args[0]}, else the {@value #PARM_DATE_PROPERTY}
     * system property, else the {@value #PARM_DATE_ENV} environment variable.
     */
    public static String resolveParmDate(String[] args) {
        String value = args != null && args.length > 0 ? args[0] : null;
        if (value == null || value.isBlank()) {
            value = System.getProperty(PARM_DATE_PROPERTY);
        }
        if (value == null || value.isBlank()) {
            value = System.getenv(PARM_DATE_ENV);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PARM-DATE is required: pass it as the first argument (e.g. "
                    + "2022071800), -D" + PARM_DATE_PROPERTY + "=... or " + PARM_DATE_ENV);
        }
        return value;
    }

    /** Counts and output paths of a completed run. */
    public record Result(long recordCount, long transactionsWritten, long accountsRewritten,
            Path transactFile, Path accountFile) {
    }

    /** Raised where the COBOL performs {@code 9999-ABEND-PROGRAM}. */
    public static final class AbendException extends RuntimeException {

        private final String fileStatus;

        AbendException(String message, String fileStatus) {
            super(message + " (FILE STATUS IS: NNNN" + ioStatus04(fileStatus) + ")");
            this.fileStatus = fileStatus;
        }

        public String fileStatus() {
            return fileStatus;
        }

        // 9910-DISPLAY-IO-STATUS (numeric statuses only)
        private static String ioStatus04(String status) {
            return "00" + status;
        }
    }

    public Result run() {
        sysout.println("START OF EXECUTION OF PROGRAM " + PROGRAM_NAME);
        wsLastAcctNum = " ".repeat(11);
        wsMonthlyInt = BigDecimal.ZERO.setScale(2);
        wsTotalInt = BigDecimal.ZERO.setScale(2);
        wsFirstTime = true;
        wsRecordCount = 0;
        wsTranidSuffix = 0;
        endOfFile = "N";
        transactionsWritten = 0;
        accountsRewritten = 0;

        // 0000-TCATBALF-OPEN .. 0400-TRANFILE-OPEN
        tcatbalFile = IndexedFileStore.load(config.input(CardDemoFile.TCATBAL), TranCatBalRecord.LENGTH,
                TranCatBalRecord::fromBytes, TranCatBalRecord::key);
        xrefFile = IndexedFileStore.load(config.input(CardDemoFile.CARDXREF), CardXrefRecord.LENGTH,
                CardXrefRecord::fromBytes, CardXrefRecord::cardNum);
        discgrpFile = IndexedFileStore.load(config.input(CardDemoFile.DISCGRP), DiscGroupRecord.LENGTH,
                DiscGroupRecord::fromBytes, DiscGroupRecord::key);
        accountFile = IndexedFileStore.load(config.input(CardDemoFile.ACCTDATA), AccountRecord.LENGTH,
                AccountRecord::fromBytes, AccountRecord::acctId);
        Path transactPath = config.output(TRANSACT_FILE);
        Path accountPath = config.output(ACCOUNT_FILE);

        requireAtomicRename(accountPath);
        transactFile = new SequentialFileWriter<>(transactPath, TranRecord.LENGTH, TranRecord::toBytes);
        RuntimeException failure = null;
        try {
            processTcatbal();
        } catch (AbendException e) {
            sysout.println(e.getMessage());
            sysout.println("ABENDING PROGRAM");
            failure = e;
        } catch (RuntimeException e) {
            failure = e;
        }
        // 9400-TRANFILE-CLOSE. On an abend DISP=(NEW,CATLG,DELETE) deletes the new SYSTRAN
        // generation; ACCTFILE rewrites already done stay in the KSDS, so the account store is
        // persisted whether or not the run (or the SYSTRAN close) failed.
        try {
            transactFile.close();
        } catch (RuntimeException e) {
            failure = addFailure(failure, e);
        }
        if (failure != null) {
            try {
                Files.deleteIfExists(transactPath);
            } catch (IOException e) {
                failure.addSuppressed(e);
            }
        }
        try {
            saveAccounts(accountPath);
        } catch (RuntimeException e) {
            failure = addFailure(failure, e);
        }
        if (failure != null) {
            throw failure;
        }
        sysout.println("END OF EXECUTION OF PROGRAM " + PROGRAM_NAME);
        return new Result(wsRecordCount, transactionsWritten, accountsRewritten, transactPath, accountPath);
    }

    private void processTcatbal() {
        while (!"Y".equals(endOfFile)) {
            if ("N".equals(endOfFile)) {
                TranCatBalRecord tranCatBal = tcatbalGetNext();
                if ("N".equals(endOfFile)) {
                    wsRecordCount++;
                    sysout.println(FixedWidth.decode(tranCatBal.toBytes()));
                    // IF TRANCAT-ACCT-ID NOT= WS-LAST-ACCT-NUM (PIC 9(11) vs PIC X(11))
                    String trancatAcctId = CobolDecimal.formatUnsigned(tranCatBal.acctId(), 11);
                    if (!trancatAcctId.equals(wsLastAcctNum)) {
                        if (!wsFirstTime) {
                            updateAccount();
                        } else {
                            wsFirstTime = false;
                        }
                        wsTotalInt = BigDecimal.ZERO.setScale(2);
                        wsLastAcctNum = trancatAcctId;
                        getAcctData(tranCatBal.acctId());
                        getXrefData(tranCatBal.acctId());
                    }
                    getInterestRate(accountRecord.groupId(), tranCatBal.typeCd(), tranCatBal.catCd());
                    if (disGroupRecord.intRate().signum() != 0) {
                        computeInterest(tranCatBal);
                        computeFees();
                    }
                }
            } else {
                // Unreachable, as in the COBOL: PERFORM UNTIL tests END-OF-FILE before every
                // iteration, so once 1000-TCATBALF-GET-NEXT sets it to 'Y' the loop ends and the
                // last account is never passed to 1050-UPDATE-ACCOUNT.
                updateAccount();
            }
        }
    }

    // 1000-TCATBALF-GET-NEXT
    private TranCatBalRecord tcatbalGetNext() {
        IoResult<TranCatBalRecord> result = tcatbalFile.readNext();
        if (result.isOk()) {
            return result.record();
        }
        if (result.isEndOfFile()) {
            endOfFile = "Y";
            return null;
        }
        throw new AbendException("ERROR READING TRANSACTION CATEGORY FILE", result.status());
    }

    // 1050-UPDATE-ACCOUNT
    private void updateAccount() {
        accountRecord = accountRecord
                .withCurrBal(accountRecord.currBal().add(wsTotalInt))
                .withCurrCycCredit(BigDecimal.ZERO)
                .withCurrCycDebit(BigDecimal.ZERO);
        IoResult<AccountRecord> result = accountFile.rewrite(accountRecord);
        if (!result.isOk()) {
            throw new AbendException("ERROR RE-WRITING ACCOUNT FILE", result.status());
        }
        accountsRewritten++;
    }

    // 1100-GET-ACCT-DATA
    private void getAcctData(long acctId) {
        IoResult<AccountRecord> result = accountFile.read(acctId);
        if (result.isNotFound()) {
            sysout.println("ACCOUNT NOT FOUND: " + CobolDecimal.formatUnsigned(acctId, 11));
        }
        if (!result.isOk()) {
            throw new AbendException("ERROR READING ACCOUNT FILE", result.status());
        }
        accountRecord = result.record();
    }

    // 1110-GET-XREF-DATA: READ XREF-FILE KEY IS FD-XREF-ACCT-ID (alternate key)
    private void getXrefData(long acctId) {
        IoResult<CardXrefRecord> result = xrefFile.readByAlternateKey(CardXrefRecord::acctId, acctId);
        if (result.isNotFound()) {
            sysout.println("ACCOUNT NOT FOUND: " + CobolDecimal.formatUnsigned(acctId, 11));
        }
        if (!result.isOk()) {
            throw new AbendException("ERROR READING XREF FILE", result.status());
        }
        cardXrefRecord = result.record();
    }

    // 1200-GET-INTEREST-RATE
    private void getInterestRate(String acctGroupId, String tranTypeCd, int tranCatCd) {
        IoResult<DiscGroupRecord> result = discgrpFile.read(DiscGroupRecord.key(acctGroupId, tranTypeCd, tranCatCd));
        if (result.isNotFound()) {
            sysout.println("DISCLOSURE GROUP RECORD MISSING");
            sysout.println("TRY WITH DEFAULT GROUP CODE");
        }
        if (!result.isOk() && !result.isNotFound()) {
            throw new AbendException("ERROR READING DISCLOSURE GROUP FILE", result.status());
        }
        if (result.isOk()) {
            disGroupRecord = result.record();
        } else {
            // MOVE 'DEFAULT' TO FD-DIS-ACCT-GROUP-ID; type and category stay in the key
            getDefaultIntRate(tranTypeCd, tranCatCd);
        }
    }

    // 1200-A-GET-DEFAULT-INT-RATE
    private void getDefaultIntRate(String tranTypeCd, int tranCatCd) {
        IoResult<DiscGroupRecord> result =
                discgrpFile.read(DiscGroupRecord.key(DiscGroupRecord.DEFAULT_GROUP, tranTypeCd, tranCatCd));
        if (!result.isOk()) {
            throw new AbendException("ERROR READING DEFAULT DISCLOSURE GROUP", result.status());
        }
        disGroupRecord = result.record();
    }

    // 1300-COMPUTE-INTEREST
    private void computeInterest(TranCatBalRecord tranCatBal) {
        // COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200   (truncated, no ROUNDED)
        BigDecimal monthly = tranCatBal.balance().multiply(disGroupRecord.intRate())
                .divide(TWELVE_HUNDRED, 2, RoundingMode.DOWN);
        wsMonthlyInt = CobolDecimal.toField(monthly, 9, 2);
        // ADD WS-MONTHLY-INT TO WS-TOTAL-INT
        wsTotalInt = CobolDecimal.toField(wsTotalInt.add(wsMonthlyInt), 9, 2);
        writeTx();
    }

    // 1300-B-WRITE-TX
    private void writeTx() {
        // ADD 1 TO WS-TRANID-SUFFIX (PIC 9(06))
        wsTranidSuffix = (int) CobolDecimal.toUnsignedField(wsTranidSuffix + 1L, 6);
        String tranId = parmDate + CobolDecimal.formatUnsigned(wsTranidSuffix, 6);
        String tranDesc = TRAN_DESC_PREFIX + CobolDecimal.formatUnsigned(accountRecord.acctId(), 11);
        // Z-GET-DB2-FORMAT-TIMESTAMP
        String db2FormatTs = CobolDecimal.db2Timestamp(clock);
        // TRAN-RECORD is never INITIALIZEd: STRING leaves TRAN-DESC beyond the 24 characters it
        // fills, and nothing sets the FILLER, so both keep their initial (space) value.
        TranRecord tranRecord = new TranRecord(
                tranId,
                TRAN_TYPE_CD,
                TRAN_CAT_CD,
                TRAN_SOURCE,
                tranDesc,
                wsMonthlyInt,
                0L,
                "",
                "",
                "",
                cardXrefRecord.cardNum(),
                db2FormatTs,
                db2FormatTs,
                "");
        transactFile.write(tranRecord);
        transactionsWritten++;
    }

    // 1400-COMPUTE-FEES: "To be implemented" in CBACT04C; the paragraph is just EXIT.
    private void computeFees() {
    }

    /** Writes to a fresh sibling temp file and swaps it in, so a failed export never truncates accounts. */
    private void saveAccounts(Path accountPath) {
        Path tmp = null;
        try {
            tmp = Files.createTempFile(outputDir(accountPath), accountPath.getFileName() + ".", ".tmp");
            accountFile.save(tmp, AccountRecord.LENGTH, AccountRecord::toBytes);
            // Same-directory rename; no non-atomic fallback, so a failure always leaves the old snapshot.
            Files.move(tmp, accountPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException | RuntimeException e) {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException cleanup) {
                    e.addSuppressed(cleanup);
                }
            }
            throw e instanceof IOException io ? new UncheckedIOException(io) : (RuntimeException) e;
        }
    }

    /**
     * Fails before any output is written if the account snapshot cannot be swapped in atomically. The probe runs
     * inside a freshly created private directory so it never touches pre-existing entries.
     */
    private static void requireAtomicRename(Path accountPath) {
        Path probeDir = null;
        try {
            probeDir = Files.createTempDirectory(outputDir(accountPath), ".cbact04c-probe");
            Path probe = Files.createFile(probeDir.resolve("a"));
            Files.move(probe, probeDir.resolve("b"), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("output directory does not support atomic rename: "
                    + outputDir(accountPath), e);
        } finally {
            if (probeDir != null) {
                try {
                    Files.deleteIfExists(probeDir.resolve("a"));
                    Files.deleteIfExists(probeDir.resolve("b"));
                    Files.deleteIfExists(probeDir);
                } catch (IOException ignored) {
                    // best effort
                }
            }
        }
    }

    private static Path outputDir(Path accountPath) {
        Path dir = accountPath.toAbsolutePath().getParent();
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static RuntimeException addFailure(RuntimeException first, RuntimeException next) {
        if (first == null) {
            return next;
        }
        first.addSuppressed(next);
        return first;
    }
}
