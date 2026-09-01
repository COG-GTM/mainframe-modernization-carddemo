package com.carddemo.posttran;

import com.carddemo.posttran.Layouts.Account;
import com.carddemo.posttran.Layouts.DalyTran;
import com.carddemo.posttran.Layouts.Reject;
import com.carddemo.posttran.Layouts.Tran;
import com.carddemo.posttran.Layouts.TranCatBal;
import com.carddemo.posttran.Layouts.Xref;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Functional tests derived from the cited COBOL. They cover the validation and error branches that
 * the delivered sample data (app/data/EBCDIC/AWS.M2.CARDDEMO.DALYTRAN.PS) never reaches: it
 * produces reject reason 102 only, so 100/101/103/109 and the abend paths have no baseline and are
 * proved against app/cbl/CBTRN02C.cbl instead of against a baseline run.
 */
class PostTranJobTest {

    private static final LocalDateTime FIXED = LocalDateTime.of(2025, 3, 4, 5, 6, 7, 890_000_000);

    @TempDir
    Path tmp;

    private Files.KeyedFile xref;
    private Files.KeyedFile acct;
    private Files.KeyedFile tcat;
    private Files.KeyedFile tran;
    private StringBuilder sysout;

    private PostTranJob.Result run(byte[]... dalytranRecords) throws IOException {
        Path in = tmp.resolve("dalytran-" + System.nanoTime() + ".dat");
        byte[] all = new byte[DalyTran.LEN * dalytranRecords.length];
        for (int i = 0; i < dalytranRecords.length; i++) {
            System.arraycopy(dalytranRecords[i], 0, all, i * DalyTran.LEN, DalyTran.LEN);
        }
        java.nio.file.Files.write(in, all);
        sysout = new StringBuilder();
        try (Files.SeqInput input = new Files.SeqInput(in, DalyTran.LEN);
             Files.SeqOutput rejects = new Files.SeqOutput(rejectPath())) {
            return new PostTranJob(input, tran, xref, rejects, acct, tcat, () -> FIXED, sysout).run();
        }
    }

    private Path rejectPath() {
        return tmp.resolve("rejects.dat");
    }

    private byte[] rejects() throws IOException {
        return java.nio.file.Files.readAllBytes(rejectPath());
    }

    private void seed(String cardNum, String acctId, String creditLimit, String expiry) throws IOException {
        Rec x = new Rec(Xref.LEN);
        x.setStr(Xref.CARD_NUM, Xref.CARD_NUM_LEN, cardNum);
        x.setUnsigned(Xref.CUST_ID, Xref.CUST_ID_LEN, 0, BigDecimal.ONE);
        x.setUnsigned(Xref.ACCT_ID, Xref.ACCT_ID_LEN, 0, new BigDecimal(acctId));
        xref = keyed(Xref.LEN, Xref.KEY_LEN, x);

        Rec a = new Rec(Account.LEN);
        a.setUnsigned(Account.ID, Account.ID_LEN, 0, new BigDecimal(acctId));
        a.setStr(Account.ACTIVE_STATUS, 1, "Y");
        a.setSigned(Account.CURR_BAL, Account.CURR_BAL_LEN, 2, BigDecimal.ZERO);
        a.setSigned(Account.CREDIT_LIMIT, Account.CREDIT_LIMIT_LEN, 2, new BigDecimal(creditLimit));
        a.setSigned(Account.CASH_CREDIT_LIMIT, Account.CASH_CREDIT_LIMIT_LEN, 2, BigDecimal.ZERO);
        a.setStr(Account.OPEN_DATE, Account.OPEN_DATE_LEN, "2020-01-01");
        a.setStr(Account.EXPIRAION_DATE, Account.EXPIRAION_DATE_LEN, expiry);
        a.setStr(Account.REISSUE_DATE, Account.REISSUE_DATE_LEN, "2020-01-01");
        a.setSigned(Account.CURR_CYC_CREDIT, Account.CURR_CYC_CREDIT_LEN, 2, BigDecimal.ZERO);
        a.setSigned(Account.CURR_CYC_DEBIT, Account.CURR_CYC_DEBIT_LEN, 2, BigDecimal.ZERO);
        acct = keyed(Account.LEN, Account.KEY_LEN, a);

        tcat = Files.KeyedFile.empty(TranCatBal.LEN, TranCatBal.KEY_LEN);
        tran = Files.KeyedFile.empty(Tran.LEN, Tran.ID_LEN);
    }

    private Files.KeyedFile keyed(int recLen, int keyLen, Rec... recs) throws IOException {
        Path p = tmp.resolve("k" + System.nanoTime() + ".dat");
        byte[] all = new byte[recLen * recs.length];
        for (int i = 0; i < recs.length; i++) {
            System.arraycopy(recs[i].bytes(), 0, all, i * recLen, recLen);
        }
        java.nio.file.Files.write(p, all);
        return Files.KeyedFile.open(p, recLen, keyLen);
    }

    private byte[] dalytran(String id, String cardNum, String amount, String origTs) {
        Rec d = new Rec(DalyTran.LEN);
        d.setStr(DalyTran.ID, DalyTran.ID_LEN, id);
        d.setStr(DalyTran.TYPE_CD, DalyTran.TYPE_CD_LEN, "01");
        d.setUnsigned(DalyTran.CAT_CD, DalyTran.CAT_CD_LEN, 0, new BigDecimal("5000"));
        d.setStr(DalyTran.SOURCE, DalyTran.SOURCE_LEN, "POS TERM");
        d.setStr(DalyTran.DESC, DalyTran.DESC_LEN, "TEST TRANSACTION");
        d.setSigned(DalyTran.AMT, DalyTran.AMT_LEN, 2, new BigDecimal(amount));
        d.setUnsigned(DalyTran.MERCHANT_ID, DalyTran.MERCHANT_ID_LEN, 0, new BigDecimal("123456789"));
        d.setStr(DalyTran.MERCHANT_NAME, DalyTran.MERCHANT_NAME_LEN, "MERCHANT");
        d.setStr(DalyTran.MERCHANT_CITY, DalyTran.MERCHANT_CITY_LEN, "CITY");
        d.setStr(DalyTran.MERCHANT_ZIP, DalyTran.MERCHANT_ZIP_LEN, "12345");
        d.setStr(DalyTran.CARD_NUM, DalyTran.CARD_NUM_LEN, cardNum);
        d.setStr(DalyTran.ORIG_TS, DalyTran.ORIG_TS_LEN, origTs);
        d.setStr(DalyTran.PROC_TS, DalyTran.PROC_TS_LEN, "");
        return d.bytes();
    }

    private String rejectReason(byte[] rejects, int index) {
        int off = index * Reject.LEN;
        return new String(rejects, off + Reject.FAIL_REASON, Reject.FAIL_REASON_LEN, StandardCharsets.ISO_8859_1);
    }

    private String rejectDesc(byte[] rejects, int index) {
        int off = index * Reject.LEN;
        return new String(rejects, off + Reject.FAIL_REASON_DESC, Reject.FAIL_REASON_DESC_LEN,
                StandardCharsets.ISO_8859_1).trim();
    }

    /** 2000-POST-TRANSACTION and 2700-A/2800/2900 — app/cbl/CBTRN02C.cbl:424-444, 503-579. */
    @Test
    void cleanTransactionPostsToAllThreeFiles() throws IOException {
        seed("4111111111111111", "00000000001", "1000000", "2099-12-31");
        PostTranJob.Result r = run(dalytran("TXN0000000000001", "4111111111111111", "100.50",
                "2025-01-01 10:00:00.000000"));

        assertEquals(1, r.transactionCount);
        assertEquals(0, r.rejectCount);
        assertEquals(0, r.returnCode);          // :227-231 RC stays 0 with no rejects
        assertEquals(0, rejects().length);

        byte[] posted = tran.read("TXN0000000000001");
        assertNotNull(posted);
        Rec p = new Rec(posted);
        assertEquals(new BigDecimal("100.50"), p.signed(Tran.AMT, Tran.AMT_LEN, 2));
        assertEquals("2025-03-04-05.06.07.890000", p.str(Tran.PROC_TS, Tran.PROC_TS_LEN));
        assertEquals("                    ", p.str(Tran.FILLER, Tran.FILLER_LEN));

        Rec a = new Rec(acct.read("00000000001"));
        assertEquals(new BigDecimal("100.50"), a.signed(Account.CURR_BAL, Account.CURR_BAL_LEN, 2));
        assertEquals(new BigDecimal("100.50"),
                a.signed(Account.CURR_CYC_CREDIT, Account.CURR_CYC_CREDIT_LEN, 2));
        assertEquals(BigDecimal.ZERO.setScale(2),
                a.signed(Account.CURR_CYC_DEBIT, Account.CURR_CYC_DEBIT_LEN, 2));

        Rec t = new Rec(tcat.read("00000000001015000"));
        assertEquals(new BigDecimal("100.50"), t.signed(TranCatBal.BAL, TranCatBal.BAL_LEN, 2));
    }

    /** 2700-B-UPDATE-TCATBAL-REC — app/cbl/CBTRN02C.cbl:526-543: second transaction accumulates. */
    @Test
    void secondTransactionAccumulatesCategoryBalance() throws IOException {
        seed("4111111111111111", "00000000001", "1000000", "2099-12-31");
        run(dalytran("TXN0000000000001", "4111111111111111", "10.00", "2025-01-01 10:00:00.000000"),
            dalytran("TXN0000000000002", "4111111111111111", "15.25", "2025-01-01 10:00:00.000000"));

        Rec t = new Rec(tcat.read("00000000001015000"));
        assertEquals(new BigDecimal("25.25"), t.signed(TranCatBal.BAL, TranCatBal.BAL_LEN, 2));
        assertEquals(1, tcat.size());
        assertEquals(2, tran.size());
    }

    /** 1500-A-LOOKUP-XREF INVALID KEY — app/cbl/CBTRN02C.cbl:383-388. */
    @Test
    void unknownCardIsRejectedWithReason100() throws IOException {
        seed("4111111111111111", "00000000001", "1000000", "2099-12-31");
        PostTranJob.Result r = run(dalytran("TXN0000000000001", "9999999999999999", "10.00",
                "2025-01-01 10:00:00.000000"));

        assertEquals(1, r.rejectCount);
        assertEquals(4, r.returnCode);
        assertEquals("0100", rejectReason(rejects(), 0));
        assertEquals("INVALID CARD NUMBER FOUND", rejectDesc(rejects(), 0));
        assertEquals(0, tran.size());
        assertEquals(0, tcat.size());
    }

    /** 1500-B-LOOKUP-ACCT INVALID KEY — app/cbl/CBTRN02C.cbl:396-401. */
    @Test
    void missingAccountIsRejectedWithReason101() throws IOException {
        seed("4111111111111111", "00000000001", "1000000", "2099-12-31");
        acct = Files.KeyedFile.empty(Account.LEN, Account.KEY_LEN);   // XREF points at no account
        PostTranJob.Result r = run(dalytran("TXN0000000000001", "4111111111111111", "10.00",
                "2025-01-01 10:00:00.000000"));

        assertEquals(1, r.rejectCount);
        assertEquals("0101", rejectReason(rejects(), 0));
        assertEquals("ACCOUNT RECORD NOT FOUND", rejectDesc(rejects(), 0));
        assertNull(tran.read("TXN0000000000001"));
    }

    /** Credit-limit check — app/cbl/CBTRN02C.cbl:403-412. */
    @Test
    void overlimitTransactionIsRejectedWithReason102() throws IOException {
        seed("4111111111111111", "00000000001", "100.00", "2099-12-31");
        PostTranJob.Result r = run(dalytran("TXN0000000000001", "4111111111111111", "100.01",
                "2025-01-01 10:00:00.000000"));

        assertEquals(1, r.rejectCount);
        assertEquals("0102", rejectReason(rejects(), 0));
        assertEquals("OVERLIMIT TRANSACTION", rejectDesc(rejects(), 0));
    }

    /** Boundary: the check is >=, so a transaction exactly at the limit posts (:536). */
    @Test
    void transactionExactlyAtCreditLimitPosts() throws IOException {
        seed("4111111111111111", "00000000001", "100.00", "2099-12-31");
        PostTranJob.Result r = run(dalytran("TXN0000000000001", "4111111111111111", "100.00",
                "2025-01-01 10:00:00.000000"));

        assertEquals(0, r.rejectCount);
        assertNotNull(tran.read("TXN0000000000001"));
    }

    /** Expiry check — app/cbl/CBTRN02C.cbl:414-420. */
    @Test
    void transactionAfterExpiryIsRejectedWithReason103() throws IOException {
        seed("4111111111111111", "00000000001", "1000000", "2024-12-31");
        PostTranJob.Result r = run(dalytran("TXN0000000000001", "4111111111111111", "10.00",
                "2025-01-01 10:00:00.000000"));

        assertEquals(1, r.rejectCount);
        assertEquals("0103", rejectReason(rejects(), 0));
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", rejectDesc(rejects(), 0));
    }

    /** Both 102 and 103 apply: the expiry check runs last and overwrites the reason (:539). */
    @Test
    void expiryReasonOverwritesOverlimitReason() throws IOException {
        seed("4111111111111111", "00000000001", "1.00", "2024-12-31");
        run(dalytran("TXN0000000000001", "4111111111111111", "500.00", "2025-01-01 10:00:00.000000"));

        assertEquals("0103", rejectReason(rejects(), 0));
    }

    /** Negative amounts go to ACCT-CURR-CYC-DEBIT — app/cbl/CBTRN02C.cbl:548-553. */
    @Test
    void negativeAmountUpdatesCycleDebit() throws IOException {
        seed("4111111111111111", "00000000001", "1000000", "2099-12-31");
        run(dalytran("TXN0000000000001", "4111111111111111", "-40.00", "2025-01-01 10:00:00.000000"));

        Rec a = new Rec(acct.read("00000000001"));
        assertEquals(new BigDecimal("-40.00"), a.signed(Account.CURR_BAL, Account.CURR_BAL_LEN, 2));
        assertEquals(BigDecimal.ZERO.setScale(2),
                a.signed(Account.CURR_CYC_CREDIT, Account.CURR_CYC_CREDIT_LEN, 2));
        assertEquals(new BigDecimal("-40.00"),
                a.signed(Account.CURR_CYC_DEBIT, Account.CURR_CYC_DEBIT_LEN, 2));
    }

    /** 2900-WRITE-TRANSACTION-FILE non-'00' status aborts — app/cbl/CBTRN02C.cbl:562-579, 707-711. */
    @Test
    void duplicateTransactionIdAbends() throws IOException {
        seed("4111111111111111", "00000000001", "1000000", "2099-12-31");
        byte[] rec = dalytran("TXN0000000000001", "4111111111111111", "10.00", "2025-01-01 10:00:00.000000");
        assertThrows(PostTranJob.AbendException.class, () -> run(rec, Arrays.copyOf(rec, rec.length)));
    }

    /** DISPLAY trailer counts — app/cbl/CBTRN02C.cbl:227-231. */
    @Test
    void sysoutReportsCountsInCobolFormat() throws IOException {
        seed("4111111111111111", "00000000001", "100.00", "2099-12-31");
        run(dalytran("TXN0000000000001", "4111111111111111", "10.00", "2025-01-01 10:00:00.000000"),
            dalytran("TXN0000000000002", "9999999999999999", "10.00", "2025-01-01 10:00:00.000000"));

        String out = sysout.toString();
        org.junit.jupiter.api.Assertions.assertTrue(out.contains("TRANSACTIONS PROCESSED :000000002"), out);
        org.junit.jupiter.api.Assertions.assertTrue(out.contains("TRANSACTIONS REJECTED  :000000001"), out);
        org.junit.jupiter.api.Assertions.assertTrue(
                out.startsWith("START OF EXECUTION OF PROGRAM CBTRN02C"), out);
    }
}
