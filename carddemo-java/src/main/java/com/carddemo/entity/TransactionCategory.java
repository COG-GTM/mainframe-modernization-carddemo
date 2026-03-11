package com.carddemo.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategory.TransactionCategoryKey.class)
public class TransactionCategory {
    @Id @Column(name = "tran_type_cd", length = 2) private String tranTypeCd;
    @Id @Column(name = "tran_cat_cd") private Integer tranCatCd;
    @Column(name = "tran_cat_type_desc", length = 50) private String tranCatTypeDesc;
    public TransactionCategory() {}
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public String getTranCatTypeDesc() { return tranCatTypeDesc; }
    public void setTranCatTypeDesc(String v) { this.tranCatTypeDesc = v; }

    public static class TransactionCategoryKey implements Serializable {
        private String tranTypeCd;
        private Integer tranCatCd;
        public TransactionCategoryKey() {}
        public TransactionCategoryKey(String t, Integer c) { tranTypeCd=t; tranCatCd=c; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransactionCategoryKey k)) return false;
            return Objects.equals(tranTypeCd, k.tranTypeCd) && Objects.equals(tranCatCd, k.tranCatCd);
        }
        @Override public int hashCode() { return Objects.hash(tranTypeCd, tranCatCd); }
    }
}
