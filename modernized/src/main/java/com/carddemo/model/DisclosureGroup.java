package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Disclosure group / interest-rate record.
 *
 * <p>Transpiled from copybook {@code CVTRA02Y} ({@code DIS-GROUP-RECORD}, 50 bytes), the record of
 * the {@code DISCGRP} VSAM KSDS. The composite key {@code DIS-GROUP-KEY} (account-group id +
 * transaction type code + transaction category code) becomes the {@link DisclosureGroupId}
 * {@code @IdClass}.
 *
 * <pre>
 * 05 DIS-GROUP-KEY.
 *    10 DIS-ACCT-GROUP-ID PIC X(10)      -> acctGroupId
 *    10 DIS-TRAN-TYPE-CD  PIC X(02)      -> tranTypeCd
 *    10 DIS-TRAN-CAT-CD   PIC 9(04)      -> tranCatCd
 * 05 DIS-INT-RATE         PIC S9(04)V99  -> intRate (annual %, scale 2)
 * </pre>
 */
@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
public class DisclosureGroup {

    /** Account-group id used by CBACT04C when no specific group matches. */
    public static final String DEFAULT_GROUP_ID = "DEFAULT";

    /** DIS-ACCT-GROUP-ID PIC X(10). */
    @Id
    @Column(name = "acct_group_id", length = 10, nullable = false)
    private String acctGroupId;

    /** DIS-TRAN-TYPE-CD PIC X(02). */
    @Id
    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    /** DIS-TRAN-CAT-CD PIC 9(04). */
    @Id
    @Column(name = "tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    /** DIS-INT-RATE PIC S9(04)V99 - annual interest rate (percent). */
    @Column(name = "int_rate", precision = 6, scale = 2, nullable = false)
    private BigDecimal intRate = BigDecimal.ZERO;

    public DisclosureGroup() {
    }

    public DisclosureGroup(String acctGroupId, String tranTypeCd, Integer tranCatCd, BigDecimal intRate) {
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

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public BigDecimal getIntRate() {
        return intRate;
    }

    public void setIntRate(BigDecimal intRate) {
        this.intRate = intRate;
    }

    /** Composite key mirroring the COBOL {@code DIS-GROUP-KEY}. */
    public static class DisclosureGroupId implements Serializable {
        private String acctGroupId;
        private String tranTypeCd;
        private Integer tranCatCd;

        public DisclosureGroupId() {
        }

        public DisclosureGroupId(String acctGroupId, String tranTypeCd, Integer tranCatCd) {
            this.acctGroupId = acctGroupId;
            this.tranTypeCd = tranTypeCd;
            this.tranCatCd = tranCatCd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DisclosureGroupId that)) {
                return false;
            }
            return Objects.equals(acctGroupId, that.acctGroupId)
                    && Objects.equals(tranTypeCd, that.tranTypeCd)
                    && Objects.equals(tranCatCd, that.tranCatCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(acctGroupId, tranTypeCd, tranCatCd);
        }
    }
}
