package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Transaction-category balance.
 *
 * <p>Transpiled from copybook {@code CVTRA01Y} ({@code TRAN-CAT-BAL-RECORD}, 50 bytes), the record
 * of the {@code TCATBALF} VSAM KSDS that drives the CBACT04C interest calculation. The composite
 * key {@code TRAN-CAT-KEY} (account id + type code + category code) becomes the {@link
 * TranCatBalanceId} {@code @IdClass}.
 *
 * <pre>
 * 05 TRAN-CAT-KEY.
 *    10 TRANCAT-ACCT-ID  PIC 9(11)      -> acctId
 *    10 TRANCAT-TYPE-CD  PIC X(02)      -> typeCd
 *    10 TRANCAT-CD       PIC 9(04)      -> catCd
 * 05 TRAN-CAT-BAL        PIC S9(09)V99  -> tranCatBal (BigDecimal scale 2)
 * </pre>
 */
@Entity
@Table(name = "tran_cat_balance")
@IdClass(TranCatBalance.TranCatBalanceId.class)
public class TranCatBalance {

    /** TRANCAT-ACCT-ID PIC 9(11). */
    @Id
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    /** TRANCAT-TYPE-CD PIC X(02). */
    @Id
    @Column(name = "type_cd", length = 2, nullable = false)
    private String typeCd;

    /** TRANCAT-CD PIC 9(04). */
    @Id
    @Column(name = "cat_cd", nullable = false)
    private Integer catCd;

    /** TRAN-CAT-BAL PIC S9(09)V99 -> fixed-point money, scale 2. */
    @Column(name = "tran_cat_bal", precision = 11, scale = 2, nullable = false)
    private BigDecimal tranCatBal = BigDecimal.ZERO;

    public TranCatBalance() {
    }

    public TranCatBalance(Long acctId, String typeCd, Integer catCd, BigDecimal tranCatBal) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
        this.tranCatBal = tranCatBal;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public void setCatCd(Integer catCd) {
        this.catCd = catCd;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }

    /** Composite key mirroring the COBOL {@code TRAN-CAT-KEY}. */
    public static class TranCatBalanceId implements Serializable {
        private Long acctId;
        private String typeCd;
        private Integer catCd;

        public TranCatBalanceId() {
        }

        public TranCatBalanceId(Long acctId, String typeCd, Integer catCd) {
            this.acctId = acctId;
            this.typeCd = typeCd;
            this.catCd = catCd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TranCatBalanceId that)) {
                return false;
            }
            return Objects.equals(acctId, that.acctId)
                    && Objects.equals(typeCd, that.typeCd)
                    && Objects.equals(catCd, that.catCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(acctId, typeCd, catCd);
        }
    }
}
