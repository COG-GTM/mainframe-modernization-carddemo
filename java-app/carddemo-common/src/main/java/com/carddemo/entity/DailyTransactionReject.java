package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for rejected daily transactions.
 * Replaces DALYREJS sequential output file from CBTRN02C.
 */
@Entity
@Table(name = "daily_transaction_reject")
public class DailyTransactionReject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reject_id")
    private Long rejectId;

    @Column(name = "transaction_id", length = 16)
    private String transactionId;

    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "reject_reason_code")
    private Integer rejectReasonCode;

    @Column(name = "reject_reason_description", length = 76)
    private String rejectReasonDescription;

    @Column(name = "reject_timestamp")
    private LocalDateTime rejectTimestamp;

    public DailyTransactionReject() {}

    public Long getRejectId() { return rejectId; }
    public void setRejectId(Long rejectId) { this.rejectId = rejectId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getRejectReasonCode() { return rejectReasonCode; }
    public void setRejectReasonCode(Integer rejectReasonCode) { this.rejectReasonCode = rejectReasonCode; }
    public String getRejectReasonDescription() { return rejectReasonDescription; }
    public void setRejectReasonDescription(String rejectReasonDescription) { this.rejectReasonDescription = rejectReasonDescription; }
    public LocalDateTime getRejectTimestamp() { return rejectTimestamp; }
    public void setRejectTimestamp(LocalDateTime rejectTimestamp) { this.rejectTimestamp = rejectTimestamp; }
}
