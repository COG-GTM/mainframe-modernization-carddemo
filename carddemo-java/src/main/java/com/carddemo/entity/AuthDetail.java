package com.carddemo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_details")
public class AuthDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "auth_summary_id") private AuthSummary authSummary;
    @Column(name = "merchant_id", length = 15) private String merchantId;
    @Column(name = "merchant_name", length = 22) private String merchantName;
    @Column(name = "merchant_city", length = 13) private String merchantCity;
    @Column(name = "merchant_state", length = 2) private String merchantState;
    @Column(name = "merchant_zip", length = 9) private String merchantZip;
    @Column(name = "merchant_category_code", length = 4) private String merchantCategoryCode;
    @Column(name = "processing_code", length = 6) private String processingCode;
    @Column(name = "pos_entry_mode") private Integer posEntryMode;
    @Column(name = "transaction_id", length = 15) private String transactionId;
    @Column(name = "message_type", length = 6) private String messageType;
    @Column(name = "message_source", length = 6) private String messageSource;
    public AuthDetail() {}
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public AuthSummary getAuthSummary() { return authSummary; }
    public void setAuthSummary(AuthSummary v) { this.authSummary = v; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String v) { this.merchantId = v; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String v) { this.merchantName = v; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String v) { this.merchantCity = v; }
    public String getMerchantState() { return merchantState; }
    public void setMerchantState(String v) { this.merchantState = v; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String v) { this.merchantZip = v; }
    public String getMerchantCategoryCode() { return merchantCategoryCode; }
    public void setMerchantCategoryCode(String v) { this.merchantCategoryCode = v; }
    public String getProcessingCode() { return processingCode; }
    public void setProcessingCode(String v) { this.processingCode = v; }
    public Integer getPosEntryMode() { return posEntryMode; }
    public void setPosEntryMode(Integer v) { this.posEntryMode = v; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String v) { this.transactionId = v; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String v) { this.messageType = v; }
    public String getMessageSource() { return messageSource; }
    public void setMessageSource(String v) { this.messageSource = v; }
}
