package com.carddemo.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Transaction category balance entity.
 *
 * Maps: TRAN-CAT-BAL-RECORD from CVTRA01Y.cpy (RECLN 50)
 * VSAM KSDS composite key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD
 *
 * Updated by CBTRN02C (2700-UPDATE-TCATBAL):
 *   - 2700-A-CREATE-TCATBAL-REC: creates new record if key doesn't exist
 *   - 2700-B-UPDATE-TCATBAL-REC: adds transaction amount to existing balance
 *
 * Read by CBACT04C (INTCALC) for interest calculation on category balances.
 */
@Entity
@Table(name = "tran_cat_balance")
@IdClass(TranCatBalanceId.class)
public class TranCatBalance {

    /** TRANCAT-ACCT-ID PIC 9(11) — part of composite key */
    @Id
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    /** TRANCAT-TYPE-CD PIC X(02) — part of composite key */
    @Id
    @Column(name = "type_cd", length = 2, nullable = false)
    private String typeCd;

    /** TRANCAT-CD PIC 9(04) — part of composite key */
    @Id
    @Column(name = "cat_cd", nullable = false)
    private Integer catCd;

    /** TRAN-CAT-BAL PIC S9(09)V99 — accumulated balance for this category */
    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    protected TranCatBalance() {
    }

    public TranCatBalance(Long acctId, String typeCd, Integer catCd, BigDecimal balance) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
        this.balance = balance;
    }

    public Long getAcctId() {
        return acctId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
