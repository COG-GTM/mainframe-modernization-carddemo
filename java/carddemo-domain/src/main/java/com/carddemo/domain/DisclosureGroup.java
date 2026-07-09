package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Disclosure group (DISCGRP) — maps COBOL copybook {@code CVTRA02Y}
 * (DIS-GROUP-RECORD, RECLN 50). Seed file {@code discgrp.txt}.
 *
 * <p>Holds the interest rate applied per (account group, transaction type, category).</p>
 */
@Entity
@Table(name = "disclosure_group")
public class DisclosureGroup {

    @EmbeddedId
    private DisclosureGroupId id;

    /** DIS-INT-RATE PIC S9(04)V99. */
    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal disIntRate;

    public DisclosureGroup() {
    }

    public DisclosureGroupId getId() {
        return id;
    }

    public void setId(DisclosureGroupId id) {
        this.id = id;
    }

    public BigDecimal getDisIntRate() {
        return disIntRate;
    }

    public void setDisIntRate(BigDecimal disIntRate) {
        this.disIntRate = disIntRate;
    }
}
