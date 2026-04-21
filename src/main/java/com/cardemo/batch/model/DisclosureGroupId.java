package com.cardemo.batch.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for DisclosureGroup (GROUP-ID + TYPE-CD + CAT-CD).
 */
public class DisclosureGroupId implements Serializable {

    private String acctGroupId;
    private String tranTypeCd;
    private String tranCatCd;

    public DisclosureGroupId() {
    }

    public DisclosureGroupId(String acctGroupId, String tranTypeCd, String tranCatCd) {
        this.acctGroupId = acctGroupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public String getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(String tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisclosureGroupId that = (DisclosureGroupId) o;
        return Objects.equals(acctGroupId, that.acctGroupId)
                && Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctGroupId, tranTypeCd, tranCatCd);
    }
}
