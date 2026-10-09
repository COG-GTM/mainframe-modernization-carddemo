package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** DALYREJS - rejected daily transaction written by CBTRN02C (350 byte record + 80 byte trailer). */
@Entity
@Table(name = "daily_transaction_reject")
public class DailyTransactionReject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dalytran_id", length = 16)
    private String transactionId;

    @Column(name = "dalytran_card_num", length = 16)
    private String cardNumber;

    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "validation_trailer", length = 80)
    private String validationTrailer;

    @Column(name = "reason_code", nullable = false)
    private Integer reasonCode;

    @Column(name = "reason_desc", length = 76, nullable = false)
    private String reasonDescription;

    @Column(name = "rejected_at", nullable = false)
    private Instant rejectedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getValidationTrailer() {
        return validationTrailer;
    }

    public void setValidationTrailer(String validationTrailer) {
        this.validationTrailer = validationTrailer;
    }

    public Integer getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(Integer reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
}
