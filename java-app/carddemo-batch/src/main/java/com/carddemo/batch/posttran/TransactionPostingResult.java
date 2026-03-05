package com.carddemo.batch.posttran;

import com.carddemo.entity.DailyTransactionReject;
import com.carddemo.entity.Transaction;

/**
 * Result of processing a daily transaction.
 * Contains either a posted Transaction or a DailyTransactionReject.
 */
public class TransactionPostingResult {

    private final Transaction postedTransaction;
    private final DailyTransactionReject reject;
    private final boolean valid;

    public TransactionPostingResult(Transaction postedTransaction, DailyTransactionReject reject, boolean valid) {
        this.postedTransaction = postedTransaction;
        this.reject = reject;
        this.valid = valid;
    }

    public Transaction getPostedTransaction() { return postedTransaction; }
    public DailyTransactionReject getReject() { return reject; }
    public boolean isValid() { return valid; }
}
