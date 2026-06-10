package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Maps to the inline 430-byte reject record (REJECT-RECORD) in CBTRN02C:
 * 350-byte raw transaction data + 4-digit fail reason code + 76-byte description.
 */
@Entity
@Table(name = "TRANSACTION_REJECT")
public class TransactionReject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Lob
    @Column(name = "RAW_TRANSACTION_DATA", length = 350)
    private String rawTransactionData;

    @Column(name = "FAIL_REASON_CODE")
    private int failReasonCode;

    @Column(name = "FAIL_REASON_DESC", length = 76)
    private String failReasonDesc;

    public TransactionReject() {
    }

    public TransactionReject(String rawTransactionData, int failReasonCode, String failReasonDesc) {
        this.rawTransactionData = rawTransactionData;
        this.failReasonCode = failReasonCode;
        this.failReasonDesc = failReasonDesc;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRawTransactionData() {
        return rawTransactionData;
    }

    public void setRawTransactionData(String rawTransactionData) {
        this.rawTransactionData = rawTransactionData;
    }

    public int getFailReasonCode() {
        return failReasonCode;
    }

    public void setFailReasonCode(int failReasonCode) {
        this.failReasonCode = failReasonCode;
    }

    public String getFailReasonDesc() {
        return failReasonDesc;
    }

    public void setFailReasonDesc(String failReasonDesc) {
        this.failReasonDesc = failReasonDesc;
    }
}
