package com.carddemo.batch.cbtrn02c;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.CardDemoFile;
import com.carddemo.batch.core.CobolDecimal;
import com.carddemo.batch.core.io.IndexedFileStore;
import com.carddemo.batch.core.io.IoResult;
import com.carddemo.batch.core.io.SequentialFileReader;
import com.carddemo.batch.core.io.SequentialFileWriter;
import com.carddemo.batch.core.record.AccountRecord;
import com.carddemo.batch.core.record.CardXrefRecord;
import com.carddemo.batch.core.record.DailyTranRecord;
import com.carddemo.batch.core.record.RejectRecord;
import com.carddemo.batch.core.record.TranCatBalRecord;
import com.carddemo.batch.core.record.TranRecord;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

/**
 * Port of {@code app/cbl/CBTRN02C.cbl} (POSTTRAN): posts the daily transaction file.
 *
 * <p>Each DALYTRAN record is validated against the card cross reference and the account master.
 * Valid transactions update the transaction category balance (TCATBAL) and the account, and are
 * written to the transaction master (TRANFILE) with a processing timestamp; invalid ones are
 * written to the daily rejects file (DALYREJS) with a reason code. Return code 4 if anything was
 * rejected, else 0.
 *
 * <p>Inputs ({@link BatchConfig#input}): {@code dailytran.txt}, {@code cardxref.txt},
 * {@code acctdata.txt}, {@code tcatbal.txt}. Outputs ({@link BatchConfig#output}):
 * {@value #TRANFILE}, {@value #DALYREJS}, and the updated {@code acctdata.txt} and
 * {@code tcatbal.txt}. Inputs are never overwritten.
 */
public final class Cbtrn02cJob {

    public static final String TRANFILE = "tranfile.txt";
    public static final String DALYREJS = "dalyrejs.txt";
    public static final String ACCTFILE = CardDemoFile.ACCTDATA.fileName();
    public static final String TCATBALF = CardDemoFile.TCATBAL.fileName();

    public static final int RC_OK = 0;
    public static final int RC_REJECTS = 4;

    static final int REASON_INVALID_CARD = 100;
    static final int REASON_ACCOUNT_NOT_FOUND = 101;
    static final int REASON_OVERLIMIT = 102;
    static final int REASON_EXPIRED = 103;
    static final int REASON_ACCOUNT_REWRITE_FAILED = 109;

    private final BatchConfig config;
    private final Clock clock;
    private final PrintStream sysout;

    // WORKING-STORAGE
    private long transactionCount;
    private long rejectCount;
    private int validationFailReason;
    private String validationFailReasonDesc;
    private CardXrefRecord cardXref;
    private AccountRecord account;
    /**
     * FILLER of the TRAN-CAT-BAL-RECORD work area. INITIALIZE does not touch FILLER, so a record
     * created by 2700-A-CREATE-TCATBAL-REC keeps whatever FILLER the last successful
     * READ ... INTO left there (spaces, the initial working-storage value, before any read).
     */
    private String tranCatBalFiller = "";

    // FILE SECTION
    private SequentialFileReader<byte[]> dalytranFile;
    private IndexedFileStore<String, TranRecord> transactFile;
    private IndexedFileStore<String, CardXrefRecord> xrefFile;
    private SequentialFileWriter<byte[]> dalyrejsFile;
    private IndexedFileStore<Long, AccountRecord> accountFile;
    private IndexedFileStore<String, TranCatBalRecord> tcatbalFile;

    public Cbtrn02cJob(BatchConfig config, Clock clock) {
        this(config, clock, System.out);
    }

    public Cbtrn02cJob(BatchConfig config, Clock clock, PrintStream sysout) {
        this.config = config;
        this.clock = clock;
        this.sysout = sysout;
    }

    public static void main(String[] args) {
        System.exit(new Cbtrn02cJob(BatchConfig.fromEnvironment(), Clock.systemDefaultZone()).run());
    }

    /** Runs the job and returns the COBOL RETURN-CODE. */
    public int run() {
        checkOutputsDoNotOverwriteInputs();
        display("START OF EXECUTION OF PROGRAM CBTRN02C");
        openFiles();
        try {
            boolean endOfFile = false;
            while (!endOfFile) {
                // 1000-DALYTRAN-GET-NEXT
                IoResult<byte[]> next = dalytranFile.read();
                if (next.isEndOfFile()) {
                    endOfFile = true;
                } else if (!next.isOk()) {
                    abend("ERROR READING DALYTRAN FILE", next.status());
                } else {
                    transactionCount++;
                    byte[] raw = next.record();
                    DailyTranRecord tran = DailyTranRecord.fromBytes(raw);
                    validationFailReason = 0;
                    validationFailReasonDesc = "";
                    validateTran(tran);
                    if (validationFailReason == 0) {
                        postTransaction(tran);
                    } else {
                        rejectCount++;
                        writeRejectRec(raw);
                    }
                }
            }
        } finally {
            dalytranFile.close();
            dalyrejsFile.close();
        }
        closeFiles();
        display("TRANSACTIONS PROCESSED :" + CobolDecimal.formatUnsigned(transactionCount, 9));
        display("TRANSACTIONS REJECTED  :" + CobolDecimal.formatUnsigned(rejectCount, 9));
        int returnCode = rejectCount > 0 ? RC_REJECTS : RC_OK;
        display("END OF EXECUTION OF PROGRAM CBTRN02C");
        return returnCode;
    }

    public long transactionCount() {
        return transactionCount;
    }

    public long rejectCount() {
        return rejectCount;
    }

    // 0000-DALYTRAN-OPEN .. 0500-TCATBALF-OPEN
    private void openFiles() {
        dalytranFile = new SequentialFileReader<>(
                config.input(CardDemoFile.DAILYTRAN), DailyTranRecord.LENGTH, bytes -> bytes);
        transactFile = new IndexedFileStore<>(TranRecord::id);
        xrefFile = IndexedFileStore.load(config.input(CardDemoFile.CARDXREF), CardXrefRecord.LENGTH,
                CardXrefRecord::fromBytes, CardXrefRecord::cardNum);
        accountFile = IndexedFileStore.load(config.input(CardDemoFile.ACCTDATA), AccountRecord.LENGTH,
                AccountRecord::fromBytes, AccountRecord::acctId);
        tcatbalFile = IndexedFileStore.load(config.input(CardDemoFile.TCATBAL), TranCatBalRecord.LENGTH,
                TranCatBalRecord::fromBytes, TranCatBalRecord::key);
        dalyrejsFile = new SequentialFileWriter<>(config.output(DALYREJS), RejectRecord.LENGTH, bytes -> bytes);
    }

    // 9100-TRANFILE-CLOSE, 9400-ACCTFILE-CLOSE, 9500-TCATBALF-CLOSE: unload the KSDS images.
    private void closeFiles() {
        transactFile.save(config.output(TRANFILE), TranRecord.LENGTH, TranRecord::toBytes);
        accountFile.save(config.output(ACCTFILE), AccountRecord.LENGTH, AccountRecord::toBytes);
        tcatbalFile.save(config.output(TCATBALF), TranCatBalRecord.LENGTH, TranCatBalRecord::toBytes);
    }

    // 1500-VALIDATE-TRAN
    private void validateTran(DailyTranRecord tran) {
        lookupXref(tran);
        if (validationFailReason == 0) {
            lookupAcct(tran);
        }
    }

    // 1500-A-LOOKUP-XREF
    private void lookupXref(DailyTranRecord tran) {
        IoResult<CardXrefRecord> result = xrefFile.read(tran.cardNum());
        if (result.isNotFound()) {
            validationFailReason = REASON_INVALID_CARD;
            validationFailReasonDesc = "INVALID CARD NUMBER FOUND";
        } else {
            cardXref = result.record();
        }
    }

    // 1500-B-LOOKUP-ACCT: the expiration check runs after the overlimit check and overwrites it,
    // so a transaction that is both over limit and expired is rejected with 103.
    private void lookupAcct(DailyTranRecord tran) {
        IoResult<AccountRecord> result = accountFile.read(cardXref.acctId());
        if (result.isNotFound()) {
            validationFailReason = REASON_ACCOUNT_NOT_FOUND;
            validationFailReasonDesc = "ACCOUNT RECORD NOT FOUND";
            return;
        }
        account = result.record();
        // COMPUTE WS-TEMP-BAL (PIC S9(09)V99) = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
        BigDecimal tempBal = CobolDecimal.toField(
                account.currCycCredit().subtract(account.currCycDebit()).add(tran.amt()), 9, 2);
        if (account.creditLimit().compareTo(tempBal) < 0) {
            validationFailReason = REASON_OVERLIMIT;
            validationFailReasonDesc = "OVERLIMIT TRANSACTION";
        }
        // ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10): alphanumeric compare (ASCII collating).
        if (account.expirationDate().compareTo(tran.origTs().substring(0, 10)) < 0) {
            validationFailReason = REASON_EXPIRED;
            validationFailReasonDesc = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION";
        }
    }

    // 2000-POST-TRANSACTION
    private void postTransaction(DailyTranRecord tran) {
        // TRAN-RECORD's FILLER is never moved to, so it keeps its initial value (spaces).
        TranRecord tranRecord = new TranRecord(
                tran.id(), tran.typeCd(), tran.catCd(), tran.source(), tran.desc(), tran.amt(),
                tran.merchantId(), tran.merchantName(), tran.merchantCity(), tran.merchantZip(),
                tran.cardNum(), tran.origTs(),
                CobolDecimal.db2Timestamp(clock), // Z-GET-DB2-FORMAT-TIMESTAMP
                "");
        updateTcatbal(tran);
        updateAccountRec(tran);
        writeTransactionFile(tranRecord);
    }

    // 2500-WRITE-REJECT-REC
    private void writeRejectRec(byte[] dalytranRecord) {
        byte[] reject = new RejectRecord(
                DailyTranRecord.fromBytes(dalytranRecord), validationFailReason, validationFailReasonDesc).toBytes();
        // MOVE DALYTRAN-RECORD TO REJECT-TRAN-DATA is a group move of the record as read.
        System.arraycopy(dalytranRecord, 0, reject, 0, DailyTranRecord.LENGTH);
        dalyrejsFile.write(reject);
    }

    // 2700-UPDATE-TCATBAL
    private void updateTcatbal(DailyTranRecord tran) {
        String key = TranCatBalRecord.key(cardXref.acctId(), tran.typeCd(), tran.catCd());
        IoResult<TranCatBalRecord> result = tcatbalFile.read(key);
        if (result.isNotFound()) {
            display("TCATBAL record not found for key : " + key + ".. Creating.");
            createTcatbalRec(tran);
        } else if (result.isOk()) {
            tranCatBalFiller = result.record().filler();
            updateTcatbalRec(result.record(), tran);
        } else {
            abend("ERROR READING TRANSACTION BALANCE FILE", result.status());
        }
    }

    // 2700-A-CREATE-TCATBAL-REC: INITIALIZE, move the key, ADD DALYTRAN-AMT TO TRAN-CAT-BAL.
    private void createTcatbalRec(DailyTranRecord tran) {
        TranCatBalRecord created = new TranCatBalRecord(
                cardXref.acctId(), tran.typeCd(), tran.catCd(), BigDecimal.ZERO.add(tran.amt()), tranCatBalFiller);
        IoResult<TranCatBalRecord> result = tcatbalFile.write(created);
        if (!result.isOk()) {
            abend("ERROR WRITING TRANSACTION BALANCE FILE", result.status());
        }
    }

    // 2700-B-UPDATE-TCATBAL-REC
    private void updateTcatbalRec(TranCatBalRecord current, DailyTranRecord tran) {
        IoResult<TranCatBalRecord> result = tcatbalFile.rewrite(current.withBalance(current.balance().add(tran.amt())));
        if (!result.isOk()) {
            abend("ERROR REWRITING TRANSACTION BALANCE FILE", result.status());
        }
    }

    // 2800-UPDATE-ACCOUNT-REC
    private void updateAccountRec(DailyTranRecord tran) {
        BigDecimal amt = tran.amt();
        account = account.withCurrBal(account.currBal().add(amt));
        if (amt.signum() >= 0) {
            account = account.withCurrCycCredit(account.currCycCredit().add(amt));
        } else {
            account = account.withCurrCycDebit(account.currCycDebit().add(amt));
        }
        if (accountFile.rewrite(account).isNotFound()) {
            // INVALID KEY: the COBOL sets the reason but the transaction is still posted.
            validationFailReason = REASON_ACCOUNT_REWRITE_FAILED;
            validationFailReasonDesc = "ACCOUNT RECORD NOT FOUND";
        }
    }

    // 2900-WRITE-TRANSACTION-FILE
    private void writeTransactionFile(TranRecord tranRecord) {
        IoResult<TranRecord> result = transactFile.write(tranRecord);
        if (!result.isOk()) {
            abend("ERROR WRITING TO TRANSACTION FILE", result.status());
        }
    }

    private void checkOutputsDoNotOverwriteInputs() {
        List<Path> inputs = List.of(
                config.input(CardDemoFile.DAILYTRAN), config.input(CardDemoFile.CARDXREF),
                config.input(CardDemoFile.ACCTDATA), config.input(CardDemoFile.TCATBAL));
        for (String name : List.of(TRANFILE, DALYREJS, ACCTFILE, TCATBALF)) {
            Path output = config.output(name);
            for (Path input : inputs) {
                if (sameFile(input, output)) {
                    throw new IllegalStateException(
                            "Output " + output + " would overwrite input " + input + "; use a separate output directory");
                }
            }
        }
    }

    private static boolean sameFile(Path a, Path b) {
        if (a.toAbsolutePath().normalize().equals(b.toAbsolutePath().normalize())) {
            return true;
        }
        try {
            return Files.exists(a) && Files.exists(b) && Files.isSameFile(a, b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void display(String line) {
        sysout.println(line);
    }

    // 9910-DISPLAY-IO-STATUS + 9999-ABEND-PROGRAM
    private void abend(String message, String fileStatus) {
        display(message);
        display("FILE STATUS IS: NNNN00" + fileStatus);
        display("ABENDING PROGRAM");
        throw new Cbtrn02cAbend(message + " (file status " + fileStatus + ")");
    }

    /** CEE3ABD with ABCODE 999. */
    public static final class Cbtrn02cAbend extends RuntimeException {

        public static final int ABCODE = 999;

        Cbtrn02cAbend(String message) {
            super(message);
        }
    }
}
