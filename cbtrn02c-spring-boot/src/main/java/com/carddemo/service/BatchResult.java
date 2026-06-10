package com.carddemo.service;

/**
 * Summary of a full batch run. Mirrors the COBOL counters and RETURN-CODE logic:
 * RETURN-CODE is set to 4 when any transactions were rejected, otherwise 0.
 */
public class BatchResult {

    private final long transactionCount;
    private final long rejectCount;

    public BatchResult(long transactionCount, long rejectCount) {
        this.transactionCount = transactionCount;
        this.rejectCount = rejectCount;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public long getRejectCount() {
        return rejectCount;
    }

    /**
     * @return 0 when all transactions posted cleanly, 4 when at least one was rejected.
     */
    public int getExitStatus() {
        return rejectCount > 0 ? 4 : 0;
    }
}
