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
 * JPA entity for authorization fraud records.
 * Migrated from DB2 DDL: AUTHFRDS.dcl
 */
@Entity
@Table(name = "authorization_fraud")
public class AuthorizationFraud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fraud_id")
    private Long fraudId;

    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "auth_timestamp")
    private LocalDateTime authTimestamp;

    @Column(name = "auth_type", length = 10)
    private String authType;

    @Column(name = "transaction_amt", precision = 11, scale = 2)
    private BigDecimal transactionAmt;

    @Column(name = "approved_amt", precision = 11, scale = 2)
    private BigDecimal approvedAmt;

    @Column(name = "merchant_id", length = 20)
    private String merchantId;

    @Column(name = "auth_fraud_flag", length = 1)
    private String authFraudFlag;

    public AuthorizationFraud() {}

    public Long getFraudId() { return fraudId; }
    public void setFraudId(Long fraudId) { this.fraudId = fraudId; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public LocalDateTime getAuthTimestamp() { return authTimestamp; }
    public void setAuthTimestamp(LocalDateTime authTimestamp) { this.authTimestamp = authTimestamp; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public BigDecimal getTransactionAmt() { return transactionAmt; }
    public void setTransactionAmt(BigDecimal transactionAmt) { this.transactionAmt = transactionAmt; }
    public BigDecimal getApprovedAmt() { return approvedAmt; }
    public void setApprovedAmt(BigDecimal approvedAmt) { this.approvedAmt = approvedAmt; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getAuthFraudFlag() { return authFraudFlag; }
    public void setAuthFraudFlag(String authFraudFlag) { this.authFraudFlag = authFraudFlag; }
}
