package com.carddemo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for TranCatBalance entity.
 *
 * Maps the VSAM KSDS composite key from CVTRA01Y.cpy:
 *   TRANCAT-ACCT-ID  PIC 9(11)
 *   TRANCAT-TYPE-CD  PIC X(02)
 *   TRANCAT-CD       PIC 9(04)
 *
 * Used by CBTRN02C (2700-UPDATE-TCATBAL) for category balance lookup/upsert.
 */
public class TranCatBalanceId implements Serializable {

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

    public Long getAcctId() {
        return acctId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranCatBalanceId that = (TranCatBalanceId) o;
        return Objects.equals(acctId, that.acctId)
                && Objects.equals(typeCd, that.typeCd)
                && Objects.equals(catCd, that.catCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, typeCd, catCd);
    }
}
