package com.carddemo.batch.model;

/**
 * DTO representing a rejected transaction (430 bytes in output file).
 * Contains the original 350-byte transaction data + 4-digit reason code + 76-char reason description.
 */
public class RejectedTransaction {

    private DailyTransaction originalTransaction;
    private int reasonCode;
    private String reasonDescription;

    public RejectedTransaction() {
    }

    public RejectedTransaction(DailyTransaction originalTransaction, int reasonCode, String reasonDescription) {
        this.originalTransaction = originalTransaction;
        this.reasonCode = reasonCode;
        this.reasonDescription = reasonDescription;
    }

    public DailyTransaction getOriginalTransaction() {
        return originalTransaction;
    }

    public void setOriginalTransaction(DailyTransaction originalTransaction) {
        this.originalTransaction = originalTransaction;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }
}
