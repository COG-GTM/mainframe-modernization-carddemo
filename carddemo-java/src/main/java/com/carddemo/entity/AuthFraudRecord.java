package com.carddemo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_fraud_records")
public class AuthFraudRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "card_num", length = 16) private String cardNum;
    @Column(name = "auth_ts") private LocalDateTime authTs;
    @Column(name = "auth_type", length = 4) private String authType;
    @Column(name = "card_expiry_date", length = 4) private String cardExpiryDate;
    @Column(name = "auth_resp_code", length = 2) private String authRespCode;
    @Column(name = "auth_resp_reason", length = 4) private String authRespReason;
    @Column(name = "transaction_amt", precision = 12, scale = 2) private BigDecimal transactionAmt;
    @Column(name = "merchant_id", length = 15) private String merchantId;
    @Column(name = "merchant_name", length = 22) private String merchantName;
    @Column(name = "match_status", length = 1) private String matchStatus;
    @Column(name = "auth_fraud", length = 1) private String authFraud;
    @Column(name = "fraud_rpt_date") private LocalDate fraudRptDate;
    @Column(name = "acct_id") private Long acctId;
    @Column(name = "cust_id") private Long custId;
    public AuthFraudRecord() {}
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { this.cardNum = v; }
    public LocalDateTime getAuthTs() { return authTs; }
    public void setAuthTs(LocalDateTime v) { this.authTs = v; }
    public String getAuthType() { return authType; }
    public void setAuthType(String v) { this.authType = v; }
    public String getCardExpiryDate() { return cardExpiryDate; }
    public void setCardExpiryDate(String v) { this.cardExpiryDate = v; }
    public String getAuthRespCode() { return authRespCode; }
    public void setAuthRespCode(String v) { this.authRespCode = v; }
    public String getAuthRespReason() { return authRespReason; }
    public void setAuthRespReason(String v) { this.authRespReason = v; }
    public BigDecimal getTransactionAmt() { return transactionAmt; }
    public void setTransactionAmt(BigDecimal v) { this.transactionAmt = v; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String v) { this.merchantId = v; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String v) { this.merchantName = v; }
    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String v) { this.matchStatus = v; }
    public String getAuthFraud() { return authFraud; }
    public void setAuthFraud(String v) { this.authFraud = v; }
    public LocalDate getFraudRptDate() { return fraudRptDate; }
    public void setFraudRptDate(LocalDate v) { this.fraudRptDate = v; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public Long getCustId() { return custId; }
    public void setCustId(Long v) { this.custId = v; }
}
