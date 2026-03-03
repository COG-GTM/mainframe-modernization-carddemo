package com.carddemo.posttran.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to TRAN-CAT-BAL-RECORD (CVTRA01Y copybook).
 * Transaction category balance keyed by (acctId, tranTypeCd, tranCatCd).
 * COBOL record length: 50 bytes.
 */
@Entity
@Table(name = "transaction_category_balances")
@IdClass(TransactionCategoryBalanceId.class)
public class TransactionCategoryBalance {

    /** TRANCAT-ACCT-ID PIC 9(11) */
    @Id
    @Column(name = "trancat_acct_id", nullable = false)
    private long trancatAcctId;

    /** TRANCAT-TYPE-CD PIC X(02) */
    @Id
    @Column(name = "trancat_type_cd", length = 2, nullable = false)
    private String trancatTypeCd;

    /** TRANCAT-CD PIC 9(04) */
    @Id
    @Column(name = "trancat_cd", nullable = false)
    private int trancatCd;

    /** TRAN-CAT-BAL PIC S9(09)V99 */
    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    public TransactionCategoryBalance() {
    }

    public long getTrancatAcctId() {
        return trancatAcctId;
    }

    public void setTrancatAcctId(long trancatAcctId) {
        this.trancatAcctId = trancatAcctId;
    }

    public String getTrancatTypeCd() {
        return trancatTypeCd;
    }

    public void setTrancatTypeCd(String trancatTypeCd) {
        this.trancatTypeCd = trancatTypeCd;
    }

    public int getTrancatCd() {
        return trancatCd;
    }

    public void setTrancatCd(int trancatCd) {
        this.trancatCd = trancatCd;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }
}
