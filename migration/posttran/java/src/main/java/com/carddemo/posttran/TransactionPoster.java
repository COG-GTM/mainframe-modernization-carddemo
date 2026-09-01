package com.carddemo.posttran;

import com.carddemo.posttran.domain.AccountRecord;
import com.carddemo.posttran.domain.CardXrefRecord;
import com.carddemo.posttran.domain.DalytranRecord;
import com.carddemo.posttran.domain.RejectRecord;
import com.carddemo.posttran.domain.TranCatBalRecord;
import com.carddemo.posttran.domain.TranRecord;
import com.carddemo.posttran.io.FixedWidthFiles;
import com.carddemo.posttran.io.KeyedStore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * POSTTRAN STEP15 — the business logic of CBTRN02C.
 *
 * <p>Method names follow the COBOL paragraph names so the two sides stay diffable by eye, and
 * every behavioural decision below carries the line it was read from. The class holds mutable
 * state on purpose: the COBOL keeps {@code CARD-XREF-RECORD}, {@code ACCOUNT-RECORD},
 * {@code TRAN-RECORD} and {@code TRAN-CAT-BAL-RECORD} in WORKING-STORAGE across the record loop,
 * and at least one behaviour (the inherited FILLER of a created category balance) depends on that.
 */
public class TransactionPoster {

    private final DdPaths dd;
    private final Db2Timestamp timestamp;
    private final List<String> sysout = new ArrayList<>();

    private KeyedStore xrefFile;
    private KeyedStore accountFile;
    private KeyedStore tcatbalFile;
    private KeyedStore transactFile;
    private final List<String> rejects = new ArrayList<>();

    // WORKING-STORAGE groups, reused across records exactly as the COBOL does.
    private final TranRecord tranRecord = new TranRecord();
    private final TranCatBalRecord tranCatBalRecord = new TranCatBalRecord();
    private CardXrefRecord cardXrefRecord;
    private AccountRecord accountRecord;

    private long transactionCount;
    private long rejectCount;

    public TransactionPoster(DdPaths dd, Db2Timestamp timestamp) {
        this.dd = dd;
        this.timestamp = timestamp;
    }

    /** CBTRN02C.cbl:237-243 — the six OPENs. TRANFILE is OPEN OUTPUT (:256), so it starts empty. */
    public void openFiles() {
        display("START OF EXECUTION OF PROGRAM CBTRN02C");
        xrefFile = KeyedStore.load(dd.xreffile(), CardXrefRecord.LENGTH, 16);
        accountFile = KeyedStore.load(dd.acctfile(), AccountRecord.LENGTH, 11);
        tcatbalFile = KeyedStore.load(dd.tcatbalf(), TranCatBalRecord.LENGTH, 17);
        transactFile = KeyedStore.empty(TranRecord.LENGTH, 16);
    }

    /** CBTRN02C.cbl:202-221 — the body of the record loop, for one daily transaction. */
    public void processTransaction(DalytranRecord tran) {
        transactionCount++;
        ValidationOutcome outcome = validateTran(tran);
        if (outcome.rejected()) {
            rejectCount++;
            writeRejectRec(tran, outcome);
        } else {
            postTransaction(tran);
        }
    }

    /** CBTRN02C.cbl:370-378 */
    ValidationOutcome validateTran(DalytranRecord tran) {
        ValidationOutcome outcome = lookupXref(tran);
        if (!outcome.rejected()) {
            outcome = lookupAcct(tran);
        }
        return outcome;
    }

    /** CBTRN02C.cbl:380-392 */
    private ValidationOutcome lookupXref(DalytranRecord tran) {
        Optional<String> found = xrefFile.read(tran.cardNum());
        if (found.isEmpty()) {
            return ValidationOutcome.INVALID_CARD;
        }
        cardXrefRecord = new CardXrefRecord(found.get());
        return ValidationOutcome.OK;
    }

    /** CBTRN02C.cbl:393-421 */
    private ValidationOutcome lookupAcct(DalytranRecord tran) {
        Optional<String> found = accountFile.read(cardXrefRecord.acctId());
        if (found.isEmpty()) {
            return ValidationOutcome.ACCOUNT_NOT_FOUND;
        }
        accountRecord = new AccountRecord(found.get());

        BigDecimal tempBal = accountRecord.currCycCredit()
                .subtract(accountRecord.currCycDebit())
                .add(tran.amount());

        ValidationOutcome outcome = ValidationOutcome.OK;
        if (accountRecord.creditLimit().compareTo(tempBal) < 0) {
            outcome = ValidationOutcome.OVERLIMIT;
        }
        // Evaluated after the limit check and overwrites it: an expired, overlimit transaction is
        // rejected as 103, not 102 (CBTRN02C.cbl:414-419).
        if (accountRecord.expirationDate().compareTo(tran.origDate()) < 0) {
            outcome = ValidationOutcome.EXPIRED;
        }
        return outcome;
    }

    /** CBTRN02C.cbl:424-444 */
    private void postTransaction(DalytranRecord tran) {
        tranRecord.postFrom(tran, timestamp.now());
        updateTcatbal(tran);
        updateAccountRec(tran);
        writeTransactionFile();
    }

    /** CBTRN02C.cbl:467-500 */
    private void updateTcatbal(DalytranRecord tran) {
        String key = cardXrefRecord.acctId() + tran.typeCd() + tran.catCd();
        Optional<String> existing = tcatbalFile.read(key);
        if (existing.isEmpty()) {
            display("TCATBAL record not found for key : " + key + ".. Creating.");
            createTcatbalRec(tran);
        } else {
            tranCatBalRecord.readInto(existing.get());
            updateTcatbalRec(tran);
        }
    }

    /** CBTRN02C.cbl:502-524 */
    private void createTcatbalRec(DalytranRecord tran) {
        tranCatBalRecord.initialize();
        tranCatBalRecord.acctId(cardXrefRecord.acctId());
        tranCatBalRecord.typeCd(tran.typeCd());
        tranCatBalRecord.catCd(tran.catCd());
        tranCatBalRecord.addToBalance(tran.amount());
        tcatbalFile.put(tranCatBalRecord.toString());
    }

    /** CBTRN02C.cbl:526-542 */
    private void updateTcatbalRec(DalytranRecord tran) {
        tranCatBalRecord.addToBalance(tran.amount());
        tcatbalFile.put(tranCatBalRecord.toString());
    }

    /** CBTRN02C.cbl:545-560 */
    private void updateAccountRec(DalytranRecord tran) {
        BigDecimal amount = tran.amount();
        accountRecord.currBal(accountRecord.currBal().add(amount));
        if (amount.signum() >= 0) {
            accountRecord.currCycCredit(accountRecord.currCycCredit().add(amount));
        } else {
            accountRecord.currCycDebit(accountRecord.currCycDebit().add(amount));
        }
        accountFile.put(accountRecord.toString());
    }

    /** CBTRN02C.cbl:562-579 */
    private void writeTransactionFile() {
        transactFile.put(tranRecord.toString());
    }

    /** CBTRN02C.cbl:446-465 */
    private void writeRejectRec(DalytranRecord tran, ValidationOutcome outcome) {
        rejects.add(new RejectRecord(tran, outcome.reason(), outcome.description()).toString());
    }

    /**
     * CBTRN02C.cbl:222-233 — the closes, the two counter DISPLAYs and the return code.
     *
     * @return the step return code: 4 when anything was rejected, else 0
     */
    public int closeFiles() {
        transactFile.save(dd.tranfile());
        accountFile.save(dd.acctfile());
        tcatbalFile.save(dd.tcatbalf());
        FixedWidthFiles.write(dd.dalyrejs(), rejects);

        display("TRANSACTIONS PROCESSED :" + "%09d".formatted(transactionCount));
        display("TRANSACTIONS REJECTED  :" + "%09d".formatted(rejectCount));
        int returnCode = rejectCount > 0 ? 4 : 0;
        display("END OF EXECUTION OF PROGRAM CBTRN02C");
        return returnCode;
    }

    private void display(String line) {
        sysout.add(line);
        System.out.println(line);
    }

    /** The step's SYSOUT, in order, for the parity diff. */
    public List<String> sysout() {
        return List.copyOf(sysout);
    }

    public long transactionCount() {
        return transactionCount;
    }

    public long rejectCount() {
        return rejectCount;
    }
}
