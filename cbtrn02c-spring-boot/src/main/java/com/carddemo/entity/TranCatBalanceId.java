package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for {@link TranCatBalance}: account id + transaction type code + category code.
 * Maps to TRAN-CAT-KEY in CVTRA01Y.cpy.
 */
@Embeddable
public class TranCatBalanceId implements Serializable {

    @Column(name = "ACCT_ID")
    private long acctId;

    @Column(name = "TYPE_CD", length = 2)
    private String typeCd;

    @Column(name = "CAT_CD")
    private int catCd;

    public TranCatBalanceId() {
    }

    public TranCatBalanceId(long acctId, String typeCd, int catCd) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
    }

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public int getCatCd() {
        return catCd;
    }

    public void setCatCd(int catCd) {
        this.catCd = catCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TranCatBalanceId that = (TranCatBalanceId) o;
        return acctId == that.acctId && catCd == that.catCd && Objects.equals(typeCd, that.typeCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, typeCd, catCd);
    }
}
