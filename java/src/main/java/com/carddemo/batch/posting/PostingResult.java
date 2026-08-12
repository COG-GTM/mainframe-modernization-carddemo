package com.carddemo.batch.posting;

import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.model.entity.Transaction;
import java.util.Optional;

/**
 * COBOL program: CBTRN02C — daily transaction posting (JCL POSTTRAN).
 *
 * <p>Outcome of one iteration of the main {@code PERFORM UNTIL END-OF-FILE} loop: either the
 * transaction passed {@code 1500-VALIDATE-TRAN} and was posted by {@code 2000-POST-TRANSACTION},
 * or it failed validation and a DALYREJS record was produced by {@code 2500-WRITE-REJECT-REC}.
 */
public final class PostingResult {

    private final DailyTransaction dailyTransaction;
    private final Transaction postedTransaction;
    private final TransactionRejectRecord rejectRecord;
    private final PostingRejectReason accountUpdateFailure;

    private PostingResult(DailyTransaction dailyTransaction,
                          Transaction postedTransaction,
                          TransactionRejectRecord rejectRecord,
                          PostingRejectReason accountUpdateFailure) {
        this.dailyTransaction = dailyTransaction;
        this.postedTransaction = postedTransaction;
        this.rejectRecord = rejectRecord;
        this.accountUpdateFailure = accountUpdateFailure;
    }

    static PostingResult posted(DailyTransaction dailyTransaction,
                                Transaction postedTransaction,
                                PostingRejectReason accountUpdateFailure) {
        return new PostingResult(dailyTransaction, postedTransaction, null, accountUpdateFailure);
    }

    static PostingResult rejected(DailyTransaction dailyTransaction, PostingRejectReason reason) {
        return new PostingResult(dailyTransaction, null,
                new TransactionRejectRecord(dailyTransaction, reason), null);
    }

    public DailyTransaction getDailyTransaction() {
        return dailyTransaction;
    }

    public boolean isPosted() {
        return rejectRecord == null;
    }

    /** The TRANSACT record written by {@code 2900-WRITE-TRANSACTION-FILE}, when posted. */
    public Optional<Transaction> getPostedTransaction() {
        return Optional.ofNullable(postedTransaction);
    }

    /** The DALYREJS record, when the transaction failed validation. */
    public Optional<TransactionRejectRecord> getRejectRecord() {
        return Optional.ofNullable(rejectRecord);
    }

    /**
     * Reason 109, set by {@code 2800-UPDATE-ACCOUNT-REC} when the account REWRITE fails. As in the
     * COBOL, the transaction is still written to TRANSACT and no reject record is produced.
     */
    public Optional<PostingRejectReason> getAccountUpdateFailure() {
        return Optional.ofNullable(accountUpdateFailure);
    }
}
