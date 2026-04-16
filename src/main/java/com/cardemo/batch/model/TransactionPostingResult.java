package com.cardemo.batch.model;

/**
 * Wraps the outcome of processing a single DailyTransaction.
 * Either contains a posted Transaction (success) or a RejectedTransaction (failure).
 */
public class TransactionPostingResult {

    private final Transaction postedTransaction;
    private final RejectedTransaction rejectedTransaction;
    private final Account updatedAccount;
    private final TransactionCategoryBalance updatedCategoryBalance;
    private final boolean newCategoryBalance;

    private TransactionPostingResult(Transaction postedTransaction,
                                     RejectedTransaction rejectedTransaction,
                                     Account updatedAccount,
                                     TransactionCategoryBalance updatedCategoryBalance,
                                     boolean newCategoryBalance) {
        this.postedTransaction = postedTransaction;
        this.rejectedTransaction = rejectedTransaction;
        this.updatedAccount = updatedAccount;
        this.updatedCategoryBalance = updatedCategoryBalance;
        this.newCategoryBalance = newCategoryBalance;
    }

    public static TransactionPostingResult posted(Transaction transaction,
                                                   Account updatedAccount,
                                                   TransactionCategoryBalance catBal,
                                                   boolean isNew) {
        return new TransactionPostingResult(transaction, null, updatedAccount, catBal, isNew);
    }

    public static TransactionPostingResult rejected(RejectedTransaction rejected) {
        return new TransactionPostingResult(null, rejected, null, null, false);
    }

    public boolean isPosted() {
        return postedTransaction != null;
    }

    public Transaction getPostedTransaction() {
        return postedTransaction;
    }

    public RejectedTransaction getRejectedTransaction() {
        return rejectedTransaction;
    }

    public Account getUpdatedAccount() {
        return updatedAccount;
    }

    public TransactionCategoryBalance getUpdatedCategoryBalance() {
        return updatedCategoryBalance;
    }

    public boolean isNewCategoryBalance() {
        return newCategoryBalance;
    }
}
