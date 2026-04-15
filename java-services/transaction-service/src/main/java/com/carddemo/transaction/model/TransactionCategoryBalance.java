package com.carddemo.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Transaction category balance entity mapped from COBOL TRAN-CAT-BAL-RECORD (CVTRA01Y.cpy, 50 bytes).
 *
 * COBOL layout:
 *   TRANCAT-ACCT-ID   PIC 9(11)       -> accountId
 *   TRANCAT-TYPE-CD   PIC X(02)       -> typeCode
 *   TRANCAT-CD        PIC 9(04)       -> categoryCode
 *   TRAN-CAT-BAL      PIC S9(09)V99   -> balance
 *   FILLER            PIC X(22)
 */
@Entity
@Table(name = "transaction_category_balances")
@IdClass(TransactionCategoryBalanceId.class)
public class TransactionCategoryBalance {

    @Id
    @Column(name = "account_id")
    private long accountId;

    @Id
    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Id
    @Column(name = "category_code")
    private int categoryCode;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    public TransactionCategoryBalance() {
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
