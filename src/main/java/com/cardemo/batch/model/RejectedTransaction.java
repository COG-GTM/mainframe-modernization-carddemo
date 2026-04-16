package com.cardemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Dead-letter record: rejected transaction with 80-byte validation trailer.
 * Trailer contains a 4-digit fail reason code and 76-char description.
 */
@Entity
@Table(name = "rejected_transaction")
public class RejectedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqId;

    @Column(name = "tran_id", length = 16)
    private String transactionId;

    @Column(name = "validation_fail_reason")
    private int failReasonCode;

    @Column(name = "validation_fail_desc", length = 76)
    private String failReasonDescription;

    @Column(name = "original_record", length = 1000)
    private String originalRecord;

    public RejectedTransaction() {
    }

    public Long getSeqId() {
        return seqId;
    }

    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getFailReasonCode() {
        return failReasonCode;
    }

    public void setFailReasonCode(int failReasonCode) {
        this.failReasonCode = failReasonCode;
    }

    public String getFailReasonDescription() {
        return failReasonDescription;
    }

    public void setFailReasonDescription(String failReasonDescription) {
        this.failReasonDescription = failReasonDescription;
    }

    public String getOriginalRecord() {
        return originalRecord;
    }

    public void setOriginalRecord(String originalRecord) {
        this.originalRecord = originalRecord;
    }
}
