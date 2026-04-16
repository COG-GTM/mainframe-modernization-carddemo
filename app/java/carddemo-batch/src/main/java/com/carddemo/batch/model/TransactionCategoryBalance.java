package com.carddemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA entity representing a transaction category balance record.
 * Maps to COBOL copybook CVTRA01Y (TRAN-CAT-BAL-RECORD, 50 bytes).
 */
@Entity
@Table(name = "transaction_category_balance")
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    public TransactionCategoryBalance() {
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
