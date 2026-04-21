package com.cardemo.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to VSAM file DISCGRP and copybook CVTRA02Y (DIS-GROUP-RECORD, 50 bytes).
 * Composite key: GROUP-ID + TYPE-CD + CAT-CD.
 */
@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "acct_group_id", length = 10, nullable = false)
    private String acctGroupId;

    @Id
    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    @Id
    @Column(name = "tran_cat_cd", length = 4, nullable = false)
    private String tranCatCd;

    @Column(name = "int_rate", precision = 6, scale = 2)
    private BigDecimal intRate;

    public DisclosureGroup() {
    }

    public DisclosureGroup(String acctGroupId, String tranTypeCd, String tranCatCd, BigDecimal intRate) {
        this.acctGroupId = acctGroupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
        this.intRate = intRate;
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

    public BigDecimal getIntRate() {
        return intRate;
    }

    public void setIntRate(BigDecimal intRate) {
        this.intRate = intRate;
    }
}
