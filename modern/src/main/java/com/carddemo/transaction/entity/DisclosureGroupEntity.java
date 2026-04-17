package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * JPA entity mapping the DISCGRP VSAM file record layout.
 *
 * COBOL Traceability: Maps CVTRA02Y.cpy DIS-GROUP-RECORD (RECLN = 50).
 * Used by CBACT04C for interest rate lookup during interest calculation.
 * <pre>
 *   05 DIS-GROUP-KEY.
 *      10 DIS-ACCT-GROUP-ID  PIC X(10)      -> accountGroupId VARCHAR(10)
 *      10 DIS-TRAN-TYPE-CD   PIC X(02)      -> transactionTypeCode VARCHAR(2)
 *      10 DIS-TRAN-CAT-CD    PIC 9(04)      -> transactionCategoryCode INT
 *   05 DIS-INT-RATE          PIC S9(04)V99  -> interestRate NUMERIC(6,2)
 * </pre>
 */
@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroupEntity.DisclosureGroupId.class)
public class DisclosureGroupEntity {

    @Id
    @Column(name = "account_group_id", length = 10, nullable = false)
    private String accountGroupId;

    @Id
    @Column(name = "transaction_type_code", length = 2, nullable = false)
    private String transactionTypeCode;

    @Id
    @Column(name = "transaction_category_code", nullable = false)
    private int transactionCategoryCode;

    @Column(name = "interest_rate", precision = 6, scale = 2, nullable = false)
    private BigDecimal interestRate;

    public DisclosureGroupEntity() {
    }

    public String getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(String accountGroupId) {
        this.accountGroupId = accountGroupId;
    }

    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }

    public int getTransactionCategoryCode() {
        return transactionCategoryCode;
    }

    public void setTransactionCategoryCode(int transactionCategoryCode) {
        this.transactionCategoryCode = transactionCategoryCode;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public static class DisclosureGroupId implements Serializable {
        private String accountGroupId;
        private String transactionTypeCode;
        private int transactionCategoryCode;

        public DisclosureGroupId() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DisclosureGroupId that = (DisclosureGroupId) o;
            return transactionCategoryCode == that.transactionCategoryCode
                    && Objects.equals(accountGroupId, that.accountGroupId)
                    && Objects.equals(transactionTypeCode, that.transactionTypeCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountGroupId, transactionTypeCode, transactionCategoryCode);
        }
    }
}
