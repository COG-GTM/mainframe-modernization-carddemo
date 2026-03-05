package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * JPA entity for transaction category balance.
 * Migrated from COBOL copybook: CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD)
 * VSAM file: TCATBALF (RECLN 50)
 */
@Entity
@Table(name = "transaction_category_balance")
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    public TransactionCategoryBalance() {}

    public TransactionCategoryBalanceId getId() { return id; }
    public void setId(TransactionCategoryBalanceId id) { this.id = id; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    @Embeddable
    public static class TransactionCategoryBalanceId implements Serializable {
        @Column(name = "acct_id")
        private Long acctId;

        @Column(name = "tran_type_cd", length = 2)
        private String tranTypeCd;

        @Column(name = "tran_cat_cd")
        private Integer tranCatCd;

        public TransactionCategoryBalanceId() {}

        public TransactionCategoryBalanceId(Long acctId, String tranTypeCd, Integer tranCatCd) {
            this.acctId = acctId;
            this.tranTypeCd = tranTypeCd;
            this.tranCatCd = tranCatCd;
        }

        public Long getAcctId() { return acctId; }
        public void setAcctId(Long acctId) { this.acctId = acctId; }
        public String getTranTypeCd() { return tranTypeCd; }
        public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }
        public Integer getTranCatCd() { return tranCatCd; }
        public void setTranCatCd(Integer tranCatCd) { this.tranCatCd = tranCatCd; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionCategoryBalanceId that = (TransactionCategoryBalanceId) o;
            return Objects.equals(acctId, that.acctId) &&
                   Objects.equals(tranTypeCd, that.tranTypeCd) &&
                   Objects.equals(tranCatCd, that.tranCatCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(acctId, tranTypeCd, tranCatCd);
        }
    }
}
