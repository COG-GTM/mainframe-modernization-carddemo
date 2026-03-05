package com.carddemo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for authorization summary (parent).
 * Migrated from IMS HIDAM segments in app/app-authorization-ims-db2-mq/ims/
 */
@Entity
@Table(name = "authorization_summary", indexes = {
    @Index(name = "idx_auth_summary_card_num", columnList = "card_num"),
    @Index(name = "idx_auth_summary_timestamp", columnList = "auth_timestamp")
})
public class AuthorizationSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long authId;

    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "auth_timestamp", nullable = false)
    private LocalDateTime authTimestamp;

    @Column(name = "auth_type", length = 10)
    private String authType;

    @Column(name = "transaction_amt", precision = 11, scale = 2)
    private BigDecimal transactionAmt;

    @Column(name = "approved_amt", precision = 11, scale = 2)
    private BigDecimal approvedAmt;

    @Column(name = "auth_status", length = 10)
    private String authStatus;

    @Column(name = "decline_reason", length = 50)
    private String declineReason;

    @Column(name = "merchant_id", length = 20)
    private String merchantId;

    @OneToMany(mappedBy = "authorizationSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthorizationDetail> details = new ArrayList<>();

    public AuthorizationSummary() {}

    public Long getAuthId() { return authId; }
    public void setAuthId(Long authId) { this.authId = authId; }
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
    public String getAuthStatus() { return authStatus; }
    public void setAuthStatus(String authStatus) { this.authStatus = authStatus; }
    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public List<AuthorizationDetail> getDetails() { return details; }
    public void setDetails(List<AuthorizationDetail> details) { this.details = details; }

    public void addDetail(AuthorizationDetail detail) {
        details.add(detail);
        detail.setAuthorizationSummary(this);
    }
}
