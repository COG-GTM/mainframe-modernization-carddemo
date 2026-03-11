package com.carddemo.service;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthorizationDecisionService {
    private final CardAccountXrefRepository xrefRepository;
    private final AccountRepository accountRepository;
    private final AuthSummaryRepository authSummaryRepository;
    private final AuthDetailRepository authDetailRepository;

    public AuthorizationDecisionService(CardAccountXrefRepository xrefRepository,
                                         AccountRepository accountRepository,
                                         AuthSummaryRepository authSummaryRepository,
                                         AuthDetailRepository authDetailRepository) {
        this.xrefRepository = xrefRepository;
        this.accountRepository = accountRepository;
        this.authSummaryRepository = authSummaryRepository;
        this.authDetailRepository = authDetailRepository;
    }

    @Transactional
    public AuthorizationResult processAuthorization(AuthorizationRequest request) {
        // Validate transaction amount is present
        if (request.getTransactionAmt() == null) {
            return decline(request, "05", "6100"); // Missing transaction amount
        }

        // Validate card exists in cross-reference
        Optional<CardAccountXref> xrefOpt = xrefRepository.findByCardNum(request.getCardNum());
        if (xrefOpt.isEmpty()) {
            return decline(request, "05", "3100"); // Card not found
        }

        CardAccountXref xref = xrefOpt.get();
        Optional<Account> acctOpt = accountRepository.findById(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            return decline(request, "05", "4100"); // Account not found
        }

        Account account = acctOpt.get();
        if (!"Y".equals(account.getActiveStatus())) {
            return decline(request, "05", "4200"); // Account not active
        }

        // Check credit limit
        if (account.getCurrBal().add(request.getTransactionAmt()).compareTo(account.getCreditLimit()) > 0) {
            return decline(request, "05", "4300"); // Exceeds credit limit
        }

        // Check card expiry
        if (request.getCardExpiryDate() != null && isExpired(request.getCardExpiryDate())) {
            return decline(request, "05", "5100"); // Card expired
        }

        // Approve
        return approve(request, account, xref);
    }

    private boolean isExpired(String expiryDate) {
        try {
            int month = Integer.parseInt(expiryDate.substring(0, 2));
            int year = 2000 + Integer.parseInt(expiryDate.substring(2, 4));
            LocalDateTime expiry = LocalDateTime.of(year, month, 1, 0, 0).plusMonths(1);
            return LocalDateTime.now().isAfter(expiry);
        } catch (Exception e) {
            return true; // Invalid date format = expired
        }
    }

    private AuthorizationResult approve(AuthorizationRequest request, Account account, CardAccountXref xref) {
        AuthSummary summary = createSummary(request, "00", "0000", request.getTransactionAmt(), xref);
        createDetail(request, summary);
        return new AuthorizationResult("00", "0000", request.getTransactionAmt(), summary.getId());
    }

    private AuthorizationResult decline(AuthorizationRequest request, String respCode, String reason) {
        AuthSummary summary = createSummary(request, respCode, reason, BigDecimal.ZERO, null);
        createDetail(request, summary);
        return new AuthorizationResult(respCode, reason, BigDecimal.ZERO, summary.getId());
    }

    private AuthSummary createSummary(AuthorizationRequest request, String respCode, String reason, BigDecimal approved, CardAccountXref xref) {
        AuthSummary summary = new AuthSummary();
        summary.setCardNum(request.getCardNum());
        summary.setAuthTs(LocalDateTime.now());
        summary.setAuthType(request.getAuthType());
        summary.setTransactionAmt(request.getTransactionAmt());
        summary.setApprovedAmt(approved);
        summary.setAuthRespCode(respCode);
        summary.setAuthRespReason(reason);
        if (xref != null) {
            summary.setAcctId(xref.getAcctId());
            summary.setCustId(xref.getCustId());
        }
        return authSummaryRepository.save(summary);
    }

    private void createDetail(AuthorizationRequest request, AuthSummary summary) {
        AuthDetail detail = new AuthDetail();
        detail.setAuthSummary(summary);
        detail.setMerchantId(request.getMerchantId());
        detail.setMerchantName(request.getMerchantName());
        detail.setMerchantCity(request.getMerchantCity());
        detail.setMerchantCategoryCode(request.getMerchantCategoryCode());
        detail.setProcessingCode(request.getProcessingCode());
        detail.setPosEntryMode(request.getPosEntryMode());
        detail.setTransactionId(request.getTransactionId());
        detail.setMessageType(request.getMessageType());
        detail.setMessageSource(request.getMessageSource());
        authDetailRepository.save(detail);
    }

    // Inner classes for request/result
    public static class AuthorizationRequest {
        private String cardNum;
        private String authType;
        private String cardExpiryDate;
        private BigDecimal transactionAmt;
        private String merchantId;
        private String merchantName;
        private String merchantCity;
        private String merchantCategoryCode;
        private String processingCode;
        private Integer posEntryMode;
        private String transactionId;
        private String messageType;
        private String messageSource;

        public String getCardNum() { return cardNum; }
        public void setCardNum(String v) { this.cardNum = v; }
        public String getAuthType() { return authType; }
        public void setAuthType(String v) { this.authType = v; }
        public String getCardExpiryDate() { return cardExpiryDate; }
        public void setCardExpiryDate(String v) { this.cardExpiryDate = v; }
        public BigDecimal getTransactionAmt() { return transactionAmt; }
        public void setTransactionAmt(BigDecimal v) { this.transactionAmt = v; }
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String v) { this.merchantId = v; }
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String v) { this.merchantName = v; }
        public String getMerchantCity() { return merchantCity; }
        public void setMerchantCity(String v) { this.merchantCity = v; }
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

    public static class AuthorizationResult {
        private final String respCode;
        private final String respReason;
        private final BigDecimal approvedAmt;
        private final Long summaryId;

        public AuthorizationResult(String respCode, String respReason, BigDecimal approvedAmt, Long summaryId) {
            this.respCode = respCode; this.respReason = respReason; this.approvedAmt = approvedAmt; this.summaryId = summaryId;
        }
        public String getRespCode() { return respCode; }
        public String getRespReason() { return respReason; }
        public BigDecimal getApprovedAmt() { return approvedAmt; }
        public Long getSummaryId() { return summaryId; }
        public boolean isApproved() { return "00".equals(respCode); }
    }
}
