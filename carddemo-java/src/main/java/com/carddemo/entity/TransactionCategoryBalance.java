package com.carddemo.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "transaction_category_balances")
@IdClass(TransactionCategoryBalance.TranCatBalKey.class)
public class TransactionCategoryBalance {
    @Id @Column(name = "acct_id") private Long acctId;
    @Id @Column(name = "tran_type_cd", length = 2) private String tranTypeCd;
    @Id @Column(name = "tran_cat_cd") private Integer tranCatCd;
    @Column(name = "tran_cat_bal", precision = 11, scale = 2) private BigDecimal tranCatBal;
    public TransactionCategoryBalance() {}
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public BigDecimal getTranCatBal() { return tranCatBal; }
    public void setTranCatBal(BigDecimal v) { this.tranCatBal = v; }

    public static class TranCatBalKey implements Serializable {
        private Long acctId; private String tranTypeCd; private Integer tranCatCd;
        public TranCatBalKey() {}
        public TranCatBalKey(Long a, String t, Integer c) { acctId=a; tranTypeCd=t; tranCatCd=c; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TranCatBalKey k)) return false;
            return Objects.equals(acctId, k.acctId) && Objects.equals(tranTypeCd, k.tranTypeCd) && Objects.equals(tranCatCd, k.tranCatCd);
        }
        @Override public int hashCode() { return Objects.hash(acctId, tranTypeCd, tranCatCd); }
    }
}
