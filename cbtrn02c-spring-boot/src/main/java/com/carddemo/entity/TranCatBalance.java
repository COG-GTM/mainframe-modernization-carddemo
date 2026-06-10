package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to CVTRA01Y.cpy (50-byte record). Per-account/type/category running balance.
 */
@Entity
@Table(name = "TRAN_CAT_BALANCE")
public class TranCatBalance {

    @EmbeddedId
    private TranCatBalanceId id;

    @Column(name = "BALANCE", precision = 11, scale = 2)
    private BigDecimal balance;

    public TranCatBalance() {
    }

    public TranCatBalance(TranCatBalanceId id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public TranCatBalanceId getId() {
        return id;
    }

    public void setId(TranCatBalanceId id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
