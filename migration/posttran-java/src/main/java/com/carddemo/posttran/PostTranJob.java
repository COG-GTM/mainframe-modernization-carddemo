package com.carddemo.posttran;

import com.carddemo.posttran.Layouts.Account;
import com.carddemo.posttran.Layouts.DalyTran;
import com.carddemo.posttran.Layouts.Reject;
import com.carddemo.posttran.Layouts.Tran;
import com.carddemo.posttran.Layouts.TranCatBal;
import com.carddemo.posttran.Layouts.Xref;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Java migration of CBTRN02C — "Post the records from daily transaction file"
 * (app/cbl/CBTRN02C.cbl:1-5). Method names mirror the COBOL paragraph names so every behaviour is
 * traceable to a cited line; the legacy source is the reference and is not modified.
 */
public final class PostTranJob {

    /** app/cbl/CBTRN02C.cbl:707-711 — CALL 'CEE3ABD' terminates the job. */
    public static class AbendException extends RuntimeException {
        public AbendException(String message) {
            super(message);
        }
    }

    public static final class Result {
        public final long transactionCount;
        public final long rejectCount;
        public final int returnCode;

        Result(long transactionCount, long rejectCount, int returnCode) {
            this.transactionCount = transactionCount;
            this.rejectCount = rejectCount;
            this.returnCode = returnCode;
        }
    }

    private final Files.SeqInput dalytran;      // DD DALYTRAN  (POSTTRAN.jcl:30)
    private final Files.KeyedFile tranfile;     // DD TRANFILE  (POSTTRAN.jcl:28)
    private final Files.KeyedFile xreffile;     // DD XREFFILE  (POSTTRAN.jcl:32)
    private final Files.SeqOutput dalyrejs;     // DD DALYREJS  (POSTTRAN.jcl:34)
    private final Files.KeyedFile acctfile;     // DD ACCTFILE  (POSTTRAN.jcl:39)
    private final Files.KeyedFile tcatbalf;     // DD TCATBALF  (POSTTRAN.jcl:41)
    private final Supplier<LocalDateTime> clock;
    private final Appendable sysout;

    // WORKING-STORAGE record areas (app/cbl/CBTRN02C.cbl:102-126, 176-191).
    private final Rec dalytranRec = new Rec(DalyTran.LEN);
    private final Rec tranRec = new Rec(Tran.LEN);
    private final Rec xrefRec = new Rec(Xref.LEN);
    private final Rec acctRec = new Rec(Account.LEN);
    private final Rec tcatRec = new Rec(TranCatBal.LEN);
    private final Rec rejectRec = new Rec(Reject.LEN);
    private final Rec fdTcatKey = new Rec(TranCatBal.KEY_LEN);

    private int failReason;
    private String failReasonDesc = "";
    private long transactionCount;
    private long rejectCount;
    private boolean createTrancatRec;

    public PostTranJob(Files.SeqInput dalytran,
                       Files.KeyedFile tranfile,
                       Files.KeyedFile xreffile,
                       Files.SeqOutput dalyrejs,
                       Files.KeyedFile acctfile,
                       Files.KeyedFile tcatbalf,
                       Supplier<LocalDateTime> clock,
                       Appendable sysout) {
        this.dalytran = dalytran;
        this.tranfile = tranfile;
        this.xreffile = xreffile;
        this.dalyrejs = dalyrejs;
        this.acctfile = acctfile;
        this.tcatbalf = tcatbalf;
        this.clock = clock;
        this.sysout = sysout;
    }

    /** PROCEDURE DIVISION main loop — app/cbl/CBTRN02C.cbl:193-233. */
    public Result run() throws IOException {
        display("START OF EXECUTION OF PROGRAM CBTRN02C");
        byte[] rec;
        while ((rec = dalytran.read()) != null) {   // 1000-DALYTRAN-GET-NEXT (:345)
            dalytranRec.replace(rec);
            transactionCount++;
            failReason = 0;
            failReasonDesc = "";
            validateTran();                          // 1500-VALIDATE-TRAN (:370)
            if (failReason == 0) {
                postTransaction();                   // 2000-POST-TRANSACTION (:424)
            } else {
                rejectCount++;
                writeRejectRec();                    // 2500-WRITE-REJECT-REC (:446)
            }
        }
        display("TRANSACTIONS PROCESSED :" + pad9(transactionCount));
        display("TRANSACTIONS REJECTED  :" + pad9(rejectCount));
        int returnCode = rejectCount > 0 ? 4 : 0;    // :227-231
        display("END OF EXECUTION OF PROGRAM CBTRN02C");
        return new Result(transactionCount, rejectCount, returnCode);
    }

    /** 1500-VALIDATE-TRAN — app/cbl/CBTRN02C.cbl:370-378. */
    private void validateTran() {
        lookupXref();
        if (failReason == 0) {
            lookupAcct();
        }
    }

    /** 1500-A-LOOKUP-XREF — app/cbl/CBTRN02C.cbl:380-391. */
    private void lookupXref() {
        String key = dalytranRec.str(DalyTran.CARD_NUM, DalyTran.CARD_NUM_LEN);
        byte[] found = xreffile.read(key);
        if (found == null) {
            failReason = 100;
            failReasonDesc = "INVALID CARD NUMBER FOUND";
        } else {
            xrefRec.replace(found);
        }
    }

    /** 1500-B-LOOKUP-ACCT — app/cbl/CBTRN02C.cbl:393-421. */
    private void lookupAcct() {
        BigDecimal acctId = xrefRec.unsigned(Xref.ACCT_ID, Xref.ACCT_ID_LEN, 0);
        Rec key = new Rec(Account.KEY_LEN);
        key.setUnsigned(0, Account.KEY_LEN, 0, acctId);
        byte[] found = acctfile.read(key.str(0, Account.KEY_LEN));
        if (found == null) {
            failReason = 101;
            failReasonDesc = "ACCOUNT RECORD NOT FOUND";
            return;
        }
        acctRec.replace(found);

        // COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT (:403)
        BigDecimal tempBal = acctRec.signed(Account.CURR_CYC_CREDIT, Account.CURR_CYC_CREDIT_LEN, 2)
                .subtract(acctRec.signed(Account.CURR_CYC_DEBIT, Account.CURR_CYC_DEBIT_LEN, 2))
                .add(dalytranRec.signed(DalyTran.AMT, DalyTran.AMT_LEN, 2));
        tempBal = truncate(tempBal, 9, 2);   // WS-TEMP-BAL PIC S9(09)V99 (:187)

        if (acctRec.signed(Account.CREDIT_LIMIT, Account.CREDIT_LIMIT_LEN, 2).compareTo(tempBal) < 0) {
            failReason = 102;
            failReasonDesc = "OVERLIMIT TRANSACTION";
        }
        // ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10) — alphanumeric compare (:414)
        String expiry = acctRec.str(Account.EXPIRAION_DATE, Account.EXPIRAION_DATE_LEN);
        String origDate = dalytranRec.str(DalyTran.ORIG_TS, 10);
        if (expiry.compareTo(origDate) < 0) {
            failReason = 103;
            failReasonDesc = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION";
        }
    }

    /** 2000-POST-TRANSACTION — app/cbl/CBTRN02C.cbl:424-444. */
    private void postTransaction() throws IOException {
        tranRec.moveAlpha(Tran.ID, Tran.ID_LEN, dalytranRec, DalyTran.ID);
        tranRec.moveAlpha(Tran.TYPE_CD, Tran.TYPE_CD_LEN, dalytranRec, DalyTran.TYPE_CD);
        tranRec.setUnsigned(Tran.CAT_CD, Tran.CAT_CD_LEN, 0,
                dalytranRec.unsigned(DalyTran.CAT_CD, DalyTran.CAT_CD_LEN, 0));
        tranRec.moveAlpha(Tran.SOURCE, Tran.SOURCE_LEN, dalytranRec, DalyTran.SOURCE);
        tranRec.moveAlpha(Tran.DESC, Tran.DESC_LEN, dalytranRec, DalyTran.DESC);
        tranRec.setSigned(Tran.AMT, Tran.AMT_LEN, 2,
                dalytranRec.signed(DalyTran.AMT, DalyTran.AMT_LEN, 2));
        tranRec.setUnsigned(Tran.MERCHANT_ID, Tran.MERCHANT_ID_LEN, 0,
                dalytranRec.unsigned(DalyTran.MERCHANT_ID, DalyTran.MERCHANT_ID_LEN, 0));
        tranRec.moveAlpha(Tran.MERCHANT_NAME, Tran.MERCHANT_NAME_LEN, dalytranRec, DalyTran.MERCHANT_NAME);
        tranRec.moveAlpha(Tran.MERCHANT_CITY, Tran.MERCHANT_CITY_LEN, dalytranRec, DalyTran.MERCHANT_CITY);
        tranRec.moveAlpha(Tran.MERCHANT_ZIP, Tran.MERCHANT_ZIP_LEN, dalytranRec, DalyTran.MERCHANT_ZIP);
        tranRec.moveAlpha(Tran.CARD_NUM, Tran.CARD_NUM_LEN, dalytranRec, DalyTran.CARD_NUM);
        tranRec.moveAlpha(Tran.ORIG_TS, Tran.ORIG_TS_LEN, dalytranRec, DalyTran.ORIG_TS);
        tranRec.setStr(Tran.PROC_TS, Tran.PROC_TS_LEN, db2FormatTimestamp());

        updateTcatbal();        // 2700 (:590)
        updateAccountRec();     // 2800 (:668)
        writeTransactionFile(); // 2900 (:685)
    }

    /** 2500-WRITE-REJECT-REC — app/cbl/CBTRN02C.cbl:446-465. */
    private void writeRejectRec() throws IOException {
        rejectRec.moveAlpha(Reject.TRAN_DATA, Reject.TRAN_DATA_LEN, dalytranRec, 0);
        rejectRec.setUnsigned(Reject.FAIL_REASON, Reject.FAIL_REASON_LEN, 0, BigDecimal.valueOf(failReason));
        rejectRec.setStr(Reject.FAIL_REASON_DESC, Reject.FAIL_REASON_DESC_LEN, failReasonDesc);
        dalyrejs.write(rejectRec.bytes());
    }

    /** 2700-UPDATE-TCATBAL — app/cbl/CBTRN02C.cbl:467-501. */
    private void updateTcatbal() {
        fdTcatKey.setUnsigned(TranCatBal.ACCT_ID, TranCatBal.ACCT_ID_LEN, 0,
                xrefRec.unsigned(Xref.ACCT_ID, Xref.ACCT_ID_LEN, 0));
        fdTcatKey.moveAlpha(TranCatBal.TYPE_CD, TranCatBal.TYPE_CD_LEN, dalytranRec, DalyTran.TYPE_CD);
        fdTcatKey.setUnsigned(TranCatBal.CD, TranCatBal.CD_LEN, 0,
                dalytranRec.unsigned(DalyTran.CAT_CD, DalyTran.CAT_CD_LEN, 0));

        createTrancatRec = false;
        byte[] found = tcatbalf.read(fdTcatKey.str(0, TranCatBal.KEY_LEN));
        if (found == null) {
            display("TCATBAL record not found for key : "
                    + fdTcatKey.str(0, TranCatBal.KEY_LEN) + ".. Creating.");
            createTrancatRec = true;
        } else {
            tcatRec.replace(found);
        }

        if (createTrancatRec) {
            createTcatbalRec();
        } else {
            updateTcatbalRec();
        }
    }

    /** 2700-A-CREATE-TCATBAL-REC — app/cbl/CBTRN02C.cbl:503-524. INITIALIZE leaves FILLER as-is. */
    private void createTcatbalRec() {
        tcatRec.setUnsigned(TranCatBal.ACCT_ID, TranCatBal.ACCT_ID_LEN, 0, BigDecimal.ZERO);
        tcatRec.fill(TranCatBal.TYPE_CD, TranCatBal.TYPE_CD_LEN, ' ');
        tcatRec.setUnsigned(TranCatBal.CD, TranCatBal.CD_LEN, 0, BigDecimal.ZERO);
        tcatRec.setSigned(TranCatBal.BAL, TranCatBal.BAL_LEN, 2, BigDecimal.ZERO);

        tcatRec.setUnsigned(TranCatBal.ACCT_ID, TranCatBal.ACCT_ID_LEN, 0,
                xrefRec.unsigned(Xref.ACCT_ID, Xref.ACCT_ID_LEN, 0));
        tcatRec.moveAlpha(TranCatBal.TYPE_CD, TranCatBal.TYPE_CD_LEN, dalytranRec, DalyTran.TYPE_CD);
        tcatRec.setUnsigned(TranCatBal.CD, TranCatBal.CD_LEN, 0,
                dalytranRec.unsigned(DalyTran.CAT_CD, DalyTran.CAT_CD_LEN, 0));
        addToBal();

        if (!tcatbalf.write(tcatRec.bytes())) {
            display("ERROR WRITING TRANSACTION BALANCE FILE");
            throw new AbendException("WRITE TCATBALF failed");
        }
    }

    /** 2700-B-UPDATE-TCATBAL-REC — app/cbl/CBTRN02C.cbl:526-543. */
    private void updateTcatbalRec() {
        addToBal();
        if (!tcatbalf.rewrite(tcatRec.bytes())) {
            display("ERROR REWRITING TRANSACTION BALANCE FILE");
            throw new AbendException("REWRITE TCATBALF failed");
        }
    }

    private void addToBal() {
        BigDecimal bal = tcatRec.signed(TranCatBal.BAL, TranCatBal.BAL_LEN, 2)
                .add(dalytranRec.signed(DalyTran.AMT, DalyTran.AMT_LEN, 2));
        tcatRec.setSigned(TranCatBal.BAL, TranCatBal.BAL_LEN, 2, truncate(bal, 9, 2));
    }

    /** 2800-UPDATE-ACCOUNT-REC — app/cbl/CBTRN02C.cbl:545-560. */
    private void updateAccountRec() {
        BigDecimal amt = dalytranRec.signed(DalyTran.AMT, DalyTran.AMT_LEN, 2);
        acctRec.setSigned(Account.CURR_BAL, Account.CURR_BAL_LEN, 2,
                truncate(acctRec.signed(Account.CURR_BAL, Account.CURR_BAL_LEN, 2).add(amt), 10, 2));
        if (amt.signum() >= 0) {
            acctRec.setSigned(Account.CURR_CYC_CREDIT, Account.CURR_CYC_CREDIT_LEN, 2,
                    truncate(acctRec.signed(Account.CURR_CYC_CREDIT, Account.CURR_CYC_CREDIT_LEN, 2)
                            .add(amt), 10, 2));
        } else {
            acctRec.setSigned(Account.CURR_CYC_DEBIT, Account.CURR_CYC_DEBIT_LEN, 2,
                    truncate(acctRec.signed(Account.CURR_CYC_DEBIT, Account.CURR_CYC_DEBIT_LEN, 2)
                            .add(amt), 10, 2));
        }
        if (!acctfile.rewrite(acctRec.bytes())) {
            failReason = 109;                       // :679-682
            failReasonDesc = "ACCOUNT RECORD NOT FOUND";
        }
    }

    /** 2900-WRITE-TRANSACTION-FILE — app/cbl/CBTRN02C.cbl:562-579. */
    private void writeTransactionFile() {
        if (!tranfile.write(tranRec.bytes())) {
            display("ERROR WRITING TO TRANSACTION FILE");
            throw new AbendException("WRITE TRANFILE failed");
        }
    }

    /** Z-GET-DB2-FORMAT-TIMESTAMP — app/cbl/CBTRN02C.cbl:692-705: EEEE-MM-DD-UU.MM.SS.HH0000. */
    private String db2FormatTimestamp() {
        LocalDateTime now = clock.get();
        int hundredths = now.getNano() / 10_000_000;
        return String.format("%04d-%02d-%02d-%02d.%02d.%02d.%02d0000",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond(), hundredths);
    }

    /** COBOL truncates to the PICTURE's digit capacity when there is no ON SIZE ERROR. */
    private static BigDecimal truncate(BigDecimal value, int intDigits, int scale) {
        BigDecimal scaled = value.setScale(scale, java.math.RoundingMode.DOWN);
        BigDecimal modulus = BigDecimal.TEN.pow(intDigits);
        BigDecimal abs = scaled.abs().remainder(modulus);
        return scaled.signum() < 0 ? abs.negate() : abs;
    }

    private static String pad9(long v) {
        return String.format("%09d", v);
    }

    private void display(String line) {
        try {
            sysout.append(line).append(System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedDisplayException(e);
        }
    }

    static final class UncheckedDisplayException extends RuntimeException {
        UncheckedDisplayException(Throwable cause) {
            super(cause);
        }
    }
}
