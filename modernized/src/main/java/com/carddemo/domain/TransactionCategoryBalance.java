package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** CVTRA01Y - transaction category balance (50 bytes). */
@Entity
@Table(name = "transaction_category_balance")
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    public TransactionCategoryBalance() {
    }

    public TransactionCategoryBalance(TransactionCategoryBalanceId id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public TransactionCategoryBalanceId getId() {
        return id;
    }

    public void setId(TransactionCategoryBalanceId id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
