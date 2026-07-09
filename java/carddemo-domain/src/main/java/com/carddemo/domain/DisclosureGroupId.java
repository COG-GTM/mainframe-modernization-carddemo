package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for {@link DisclosureGroup} — maps {@code DIS-GROUP-KEY} of copybook
 * {@code CVTRA02Y}.
 */
@Embeddable
public class DisclosureGroupId implements Serializable {

    /** DIS-ACCT-GROUP-ID PIC X(10). */
    @Column(name = "dis_acct_group_id", length = 10, nullable = false)
    private String disAcctGroupId;

    /** DIS-TRAN-TYPE-CD PIC X(02). */
    @Column(name = "dis_tran_type_cd", length = 2, nullable = false)
    private String disTranTypeCd;

    /** DIS-TRAN-CAT-CD PIC 9(04). */
    @Column(name = "dis_tran_cat_cd", nullable = false)
    private Integer disTranCatCd;

    public DisclosureGroupId() {
    }

    public DisclosureGroupId(String disAcctGroupId, String disTranTypeCd, Integer disTranCatCd) {
        this.disAcctGroupId = disAcctGroupId;
        this.disTranTypeCd = disTranTypeCd;
        this.disTranCatCd = disTranCatCd;
    }

    public String getDisAcctGroupId() {
        return disAcctGroupId;
    }

    public void setDisAcctGroupId(String disAcctGroupId) {
        this.disAcctGroupId = disAcctGroupId;
    }

    public String getDisTranTypeCd() {
        return disTranTypeCd;
    }

    public void setDisTranTypeCd(String disTranTypeCd) {
        this.disTranTypeCd = disTranTypeCd;
    }

    public Integer getDisTranCatCd() {
        return disTranCatCd;
    }

    public void setDisTranCatCd(Integer disTranCatCd) {
        this.disTranCatCd = disTranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DisclosureGroupId that)) {
            return false;
        }
        return Objects.equals(disAcctGroupId, that.disAcctGroupId)
                && Objects.equals(disTranTypeCd, that.disTranTypeCd)
                && Objects.equals(disTranCatCd, that.disTranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disAcctGroupId, disTranTypeCd, disTranCatCd);
    }
}
