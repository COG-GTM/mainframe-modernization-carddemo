package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Transaction category balance (TCATBAL) — maps COBOL copybook {@code CVTRA01Y}
 * (TRAN-CAT-BAL-RECORD, RECLN 50). Seed file {@code tcatbal.txt}.
 *
 * <p>VSAM KSDS keyed on the composite {@code TRAN-CAT-KEY} (see {@code CBTRN02C}:
 * RECORD KEY IS FD-TRAN-CAT-KEY).</p>
 */
@Entity
@Table(name = "tran_cat_balance")
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    /** TRAN-CAT-BAL PIC S9(09)V99. */
    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    public TransactionCategoryBalance() {
    }

    public TransactionCategoryBalanceId getId() {
        return id;
    }

    public void setId(TransactionCategoryBalanceId id) {
        this.id = id;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }
}
