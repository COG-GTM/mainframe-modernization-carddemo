package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** CVTRA02Y - disclosure group interest rates (50 bytes). */
@Entity
@Table(name = "disclosure_group")
public class DisclosureGroup {

    /** Group used by CBACT04C when an account group has no specific rate. */
    public static final String DEFAULT_GROUP_ID = "DEFAULT";

    @EmbeddedId
    private DisclosureGroupId id;

    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO;

    public DisclosureGroupId getId() {
        return id;
    }

    public void setId(DisclosureGroupId id) {
        this.id = id;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
}
