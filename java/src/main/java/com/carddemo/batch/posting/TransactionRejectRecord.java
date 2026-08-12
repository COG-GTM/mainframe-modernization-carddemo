package com.carddemo.batch.posting;

import com.carddemo.model.entity.DailyTransaction;

/**
 * COBOL program: CBTRN02C — daily transaction posting (JCL POSTTRAN, GDG defined by DALYREJS.jcl).
 *
 * <p>The {@code REJECT-RECORD} written by {@code 2500-WRITE-REJECT-REC} to the DALYREJS file
 * (RECFM=F, LRECL=430): the 350-byte DALYTRAN record (copybook CVTRA06Y) in
 * {@code REJECT-TRAN-DATA} followed by {@code WS-VALIDATION-TRAILER}, i.e.
 * {@code WS-VALIDATION-FAIL-REASON PIC 9(04)} plus
 * {@code WS-VALIDATION-FAIL-REASON-DESC PIC X(76)}. Reporting copybook CVTRA07Y describes the
 * downstream daily transaction report over the same records.
 */
public final class TransactionRejectRecord {

    /** Length of REJECT-TRAN-DATA (a full DALYTRAN-RECORD). */
    public static final int TRAN_DATA_LENGTH = 350;
    /** Length of VALIDATION-TRAILER. */
    public static final int TRAILER_LENGTH = 80;

    private final DailyTransaction dailyTransaction;
    private final PostingRejectReason reason;

    public TransactionRejectRecord(DailyTransaction dailyTransaction, PostingRejectReason reason) {
        this.dailyTransaction = dailyTransaction;
        this.reason = reason;
    }

    public DailyTransaction getDailyTransaction() {
        return dailyTransaction;
    }

    public PostingRejectReason getReason() {
        return reason;
    }

    /** WS-VALIDATION-FAIL-REASON PIC 9(04). */
    public int getFailReason() {
        return reason.getCode();
    }

    /** WS-VALIDATION-FAIL-REASON-DESC PIC X(76). */
    public String getFailReasonDescription() {
        return reason.getDescription();
    }

    /** REJECT-TRAN-DATA: the DALYTRAN record re-rendered in its CVTRA06Y layout. */
    public String getTransactionData() {
        DailyTransaction t = dailyTransaction;
        return CobolRecordFormat.alpha(t.getTransactionId(), 16)
                + CobolRecordFormat.alpha(t.getTypeCode(), 2)
                + CobolRecordFormat.unsigned(t.getCategoryCode(), 4)
                + CobolRecordFormat.alpha(t.getSource(), 10)
                + CobolRecordFormat.alpha(t.getDescription(), 100)
                + CobolRecordFormat.zoned(t.getAmount(), 11, 2)
                + CobolRecordFormat.unsigned(t.getMerchantId(), 9)
                + CobolRecordFormat.alpha(t.getMerchantName(), 50)
                + CobolRecordFormat.alpha(t.getMerchantCity(), 50)
                + CobolRecordFormat.alpha(t.getMerchantZip(), 10)
                + CobolRecordFormat.alpha(t.getCardNumber(), 16)
                + CobolRecordFormat.alpha(t.getOriginTimestamp(), 26)
                + CobolRecordFormat.alpha(t.getProcessTimestamp(), 26)
                + " ".repeat(20);
    }

    /** VALIDATION-TRAILER: PIC 9(04) reason code followed by the PIC X(76) description. */
    public String getValidationTrailer() {
        return CobolRecordFormat.unsigned(reason.getCode(), 4)
                + CobolRecordFormat.alpha(reason.getDescription(), 76);
    }

    /** The complete 430-byte DALYREJS record. */
    public String toRecordLine() {
        return getTransactionData() + getValidationTrailer();
    }
}
