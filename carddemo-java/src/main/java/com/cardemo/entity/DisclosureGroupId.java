package com.cardemo.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for {@link DisclosureGroup}.
 * Maps the COBOL DIS-GROUP-KEY: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD.
 */
public class DisclosureGroupId implements Serializable {

    private String disAcctGroupId;
    private String disTranTypeCd;
    private Integer disTranCatCd;

    public DisclosureGroupId() {
    }

    public DisclosureGroupId(String disAcctGroupId, String disTranTypeCd, Integer disTranCatCd) {
        this.disAcctGroupId = disAcctGroupId;
        this.disTranTypeCd = disTranTypeCd;
        this.disTranCatCd = disTranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisclosureGroupId that = (DisclosureGroupId) o;
        return Objects.equals(disAcctGroupId, that.disAcctGroupId)
                && Objects.equals(disTranTypeCd, that.disTranTypeCd)
                && Objects.equals(disTranCatCd, that.disTranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disAcctGroupId, disTranTypeCd, disTranCatCd);
    }
}
