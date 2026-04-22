package com.carddemo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a generated transaction detail report.
 * Mirrors the output of CBTRN03C which groups transactions by card number
 * with subtotals per card and a grand total.
 */
public class TransactionReportDto {

    private String reportId;
    private String reportName;
    private String startDate;
    private String endDate;
    private LocalDateTime generatedAt;
    private List<CardGroup> cardGroups = new ArrayList<>();
    private BigDecimal grandTotal = BigDecimal.ZERO;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<CardGroup> getCardGroups() {
        return cardGroups;
    }

    public void setCardGroups(List<CardGroup> cardGroups) {
        this.cardGroups = cardGroups;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    /**
     * Represents a group of transactions for a single card number,
     * analogous to CBTRN03C's account-level grouping with subtotals.
     */
    public static class CardGroup {

        private String cardNumber;
        private String accountId;
        private List<ReportLine> transactions = new ArrayList<>();
        private BigDecimal subtotal = BigDecimal.ZERO;

        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public List<ReportLine> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<ReportLine> transactions) {
            this.transactions = transactions;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }

    /**
     * Individual report line, matching the detail fields from CBTRN03C:
     * TRAN-ID, TRAN-TYPE-CD, TRAN-CAT-CD, TRAN-SOURCE, TRAN-DESC, TRAN-AMT.
     */
    public static class ReportLine {

        private String transactionId;
        private String date;
        private String description;
        private String merchantName;
        private String merchantCity;
        private BigDecimal amount;
        private String typeCd;
        private String source;

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public String getMerchantCity() {
            return merchantCity;
        }

        public void setMerchantCity(String merchantCity) {
            this.merchantCity = merchantCity;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getTypeCd() {
            return typeCd;
        }

        public void setTypeCd(String typeCd) {
            this.typeCd = typeCd;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}
