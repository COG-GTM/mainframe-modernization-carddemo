package com.cardemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to CVTRA01Y.cpy TRAN-CAT-BAL-RECORD (50 bytes).
 * Transaction category balance record with composite key.
 */
@Entity
@Table(name = "tran_cat_bal")
@IdClass(TransactionCategoryBalanceId.class)
public class TransactionCategoryBalance {

    @Id
    @Column(name = "trancat_acct_id")
    private long acctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String typeCd;

    @Id
    @Column(name = "trancat_cd")
    private int catCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal balance;

    public TransactionCategoryBalance() {
    }

    public TransactionCategoryBalance(long acctId, String typeCd, int catCd, BigDecimal balance) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
        this.balance = balance;
    }

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public int getCatCd() {
        return catCd;
    }

    public void setCatCd(int catCd) {
        this.catCd = catCd;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
