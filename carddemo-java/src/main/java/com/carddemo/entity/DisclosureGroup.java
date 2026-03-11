package com.carddemo.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroup.DisclosureGroupKey.class)
public class DisclosureGroup {
    @Id @Column(name = "acct_group_id", length = 10) private String acctGroupId;
    @Id @Column(name = "tran_type_cd", length = 2) private String tranTypeCd;
    @Id @Column(name = "tran_cat_cd") private Integer tranCatCd;
    @Column(name = "int_rate", precision = 6, scale = 2) private BigDecimal intRate;

    public DisclosureGroup() {}
    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String v) { this.acctGroupId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public BigDecimal getIntRate() { return intRate; }
    public void setIntRate(BigDecimal v) { this.intRate = v; }

    public static class DisclosureGroupKey implements Serializable {
        private String acctGroupId;
        private String tranTypeCd;
        private Integer tranCatCd;
        public DisclosureGroupKey() {}
        public DisclosureGroupKey(String a, String t, Integer c) { acctGroupId=a; tranTypeCd=t; tranCatCd=c; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DisclosureGroupKey k)) return false;
            return Objects.equals(acctGroupId, k.acctGroupId) && Objects.equals(tranTypeCd, k.tranTypeCd) && Objects.equals(tranCatCd, k.tranCatCd);
        }
        @Override public int hashCode() { return Objects.hash(acctGroupId, tranTypeCd, tranCatCd); }
    }
}
