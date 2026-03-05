package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * JPA entity for disclosure group (interest rate configuration).
 * Migrated from COBOL copybook: CVTRA02Y.cpy (DIS-GROUP-RECORD)
 * VSAM file: DISCGRP (RECLN 50)
 */
@Entity
@Table(name = "disclosure_group")
public class DisclosureGroup {

    @EmbeddedId
    private DisclosureGroupId id;

    @Column(name = "interest_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;

    public DisclosureGroup() {}

    public DisclosureGroupId getId() { return id; }
    public void setId(DisclosureGroupId id) { this.id = id; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    @Embeddable
    public static class DisclosureGroupId implements Serializable {
        @Column(name = "group_id", length = 10)
        private String groupId;

        @Column(name = "tran_type_cd", length = 2)
        private String tranTypeCd;

        @Column(name = "tran_cat_cd")
        private Integer tranCatCd;

        public DisclosureGroupId() {}

        public DisclosureGroupId(String groupId, String tranTypeCd, Integer tranCatCd) {
            this.groupId = groupId;
            this.tranTypeCd = tranTypeCd;
            this.tranCatCd = tranCatCd;
        }

        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public String getTranTypeCd() { return tranTypeCd; }
        public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }
        public Integer getTranCatCd() { return tranCatCd; }
        public void setTranCatCd(Integer tranCatCd) { this.tranCatCd = tranCatCd; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DisclosureGroupId that = (DisclosureGroupId) o;
            return Objects.equals(groupId, that.groupId) &&
                   Objects.equals(tranTypeCd, that.tranTypeCd) &&
                   Objects.equals(tranCatCd, that.tranCatCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, tranTypeCd, tranCatCd);
        }
    }
}
