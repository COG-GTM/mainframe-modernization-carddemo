package com.carddemo.posttran.model;

/**
 * Intermediate result object carrying the original daily transaction
 * along with validation outcome. Used to route records between the
 * valid-transaction writer and the reject writer.
 */
public class ProcessedTransaction {

    private final DailyTransaction dailyTransaction;
    private final boolean valid;
    private final int validationFailReason;
    private final String validationFailReasonDesc;

    /** Account ID resolved from the XREF lookup (needed for posting). */
    private final long accountId;

    private ProcessedTransaction(DailyTransaction dailyTransaction,
                                 boolean valid,
                                 int validationFailReason,
                                 String validationFailReasonDesc,
                                 long accountId) {
        this.dailyTransaction = dailyTransaction;
        this.valid = valid;
        this.validationFailReason = validationFailReason;
        this.validationFailReasonDesc = validationFailReasonDesc;
        this.accountId = accountId;
    }

    /** Factory for a valid transaction that passed all checks. */
    public static ProcessedTransaction validResult(DailyTransaction tx, long accountId) {
        return new ProcessedTransaction(tx, true, 0, "", accountId);
    }

    /** Factory for a rejected transaction. */
    public static ProcessedTransaction rejectedResult(DailyTransaction tx,
                                                       int failReason,
                                                       String failReasonDesc) {
        return new ProcessedTransaction(tx, false, failReason, failReasonDesc, 0);
    }

    public DailyTransaction getDailyTransaction() {
        return dailyTransaction;
    }

    public boolean isValid() {
        return valid;
    }

    public int getValidationFailReason() {
        return validationFailReason;
    }

    public String getValidationFailReasonDesc() {
        return validationFailReasonDesc;
    }

    public long getAccountId() {
        return accountId;
    }
}
