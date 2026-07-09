package com.carddemo.batch.posting;

import com.carddemo.domain.Account;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalance;

/**
 * Outcome of validating and posting a single {@link DailyTransaction}, produced by
 * {@link TransactionPostingProcessor} and consumed by {@link PostingItemWriter}.
 *
 * <p>Mirrors the two branches of the {@code CBTRN02C} main loop: a posted transaction
 * (paragraph {@code 2000-POST-TRANSACTION}) carries the new {@link Transaction} plus the
 * mutated {@link Account} and {@link TransactionCategoryBalance} to persist; a rejected
 * transaction (paragraph {@code 2500-WRITE-REJECT-REC}) carries the offending daily record
 * and its {@link RejectReason}.</p>
 */
public final class PostingResult {

    private final boolean posted;

    // Posted branch
    private final Transaction transaction;
    private final Account account;
    private final TransactionCategoryBalance categoryBalance;

    // Rejected branch
    private final DailyTransaction rejected;
    private final RejectReason reason;

    private PostingResult(boolean posted, Transaction transaction, Account account,
                          TransactionCategoryBalance categoryBalance,
                          DailyTransaction rejected, RejectReason reason) {
        this.posted = posted;
        this.transaction = transaction;
        this.account = account;
        this.categoryBalance = categoryBalance;
        this.rejected = rejected;
        this.reason = reason;
    }

    public static PostingResult posted(Transaction transaction, Account account,
                                       TransactionCategoryBalance categoryBalance) {
        return new PostingResult(true, transaction, account, categoryBalance, null, null);
    }

    public static PostingResult rejected(DailyTransaction rejected, RejectReason reason) {
        return new PostingResult(false, null, null, null, rejected, reason);
    }

    public boolean isPosted() {
        return posted;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Account getAccount() {
        return account;
    }

    public TransactionCategoryBalance getCategoryBalance() {
        return categoryBalance;
    }

    public DailyTransaction getRejected() {
        return rejected;
    }

    public RejectReason getReason() {
        return reason;
    }
}
