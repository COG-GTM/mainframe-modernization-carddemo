package com.carddemo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_summary")
public class AuthSummary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "card_num", length = 16) private String cardNum;
    @Column(name = "auth_ts") private LocalDateTime authTs;
    @Column(name = "auth_type", length = 4) private String authType;
    @Column(name = "transaction_amt", precision = 12, scale = 2) private BigDecimal transactionAmt;
    @Column(name = "approved_amt", precision = 12, scale = 2) private BigDecimal approvedAmt;
    @Column(name = "auth_resp_code", length = 2) private String authRespCode;
    @Column(name = "auth_resp_reason", length = 4) private String authRespReason;
    @Column(name = "acct_id") private Long acctId;
    @Column(name = "cust_id") private Long custId;
    public AuthSummary() {}
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { this.cardNum = v; }
    public LocalDateTime getAuthTs() { return authTs; }
    public void setAuthTs(LocalDateTime v) { this.authTs = v; }
    public String getAuthType() { return authType; }
    public void setAuthType(String v) { this.authType = v; }
    public BigDecimal getTransactionAmt() { return transactionAmt; }
    public void setTransactionAmt(BigDecimal v) { this.transactionAmt = v; }
    public BigDecimal getApprovedAmt() { return approvedAmt; }
    public void setApprovedAmt(BigDecimal v) { this.approvedAmt = v; }
    public String getAuthRespCode() { return authRespCode; }
    public void setAuthRespCode(String v) { this.authRespCode = v; }
    public String getAuthRespReason() { return authRespReason; }
    public void setAuthRespReason(String v) { this.authRespReason = v; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public Long getCustId() { return custId; }
    public void setCustId(Long v) { this.custId = v; }
}
