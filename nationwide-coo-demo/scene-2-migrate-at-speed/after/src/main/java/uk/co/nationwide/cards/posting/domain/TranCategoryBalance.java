package uk.co.nationwide.cards.posting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Transaction-category balance — modernized form of the TCATBAL VSAM master
 * keyed by (acct-id, tran-type-cd, tran-cat-cd). Updated by CBTRN02C as
 * transactions are posted; aggregated by CBTRN03C end-of-cycle.
 */
@Entity
@Table(name = "tran_category_balance")
@IdClass(TranCategoryBalance.Key.class)
public class TranCategoryBalance {

    @Id @Column(name = "acct_id", nullable = false)
    private Long acctId;

    @Id @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    @Id @Column(name = "tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    @Column(name = "tran_cat_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal tranCatBal;

    public TranCategoryBalance() { }

    public TranCategoryBalance(Long acctId, String tranTypeCd, Integer tranCatCd, BigDecimal tranCatBal) {
        this.acctId = acctId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
        this.tranCatBal = tranCatBal;
    }

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer tranCatCd) { this.tranCatCd = tranCatCd; }
    public BigDecimal getTranCatBal() { return tranCatBal; }
    public void setTranCatBal(BigDecimal tranCatBal) { this.tranCatBal = tranCatBal; }

    public static class Key implements Serializable {
        private Long acctId;
        private String tranTypeCd;
        private Integer tranCatCd;

        public Key() { }
        public Key(Long acctId, String tranTypeCd, Integer tranCatCd) {
            this.acctId = acctId;
            this.tranTypeCd = tranTypeCd;
            this.tranCatCd = tranCatCd;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key k)) return false;
            return Objects.equals(acctId, k.acctId)
                && Objects.equals(tranTypeCd, k.tranTypeCd)
                && Objects.equals(tranCatCd, k.tranCatCd);
        }
        @Override public int hashCode() {
            return Objects.hash(acctId, tranTypeCd, tranCatCd);
        }
    }
}
