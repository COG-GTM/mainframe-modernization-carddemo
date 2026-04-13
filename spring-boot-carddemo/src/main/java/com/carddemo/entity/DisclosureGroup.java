package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA02Y.cpy (DIS-GROUP-RECORD, RECLN 50).
 * Disclosure group interest rate configuration.
 * Composite key: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD.
 */
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureGroup {

    /** DIS-ACCT-GROUP-ID — PIC X(10). Account group identifier (part of composite key). */
    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String disAcctGroupId;

    /** DIS-TRAN-TYPE-CD — PIC X(02). Transaction type code (part of composite key). */
    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String disTranTypeCd;

    /** DIS-TRAN-CAT-CD — PIC 9(04). Transaction category code (part of composite key). */
    @Id
    @Column(name = "dis_tran_cat_cd")
    private Integer disTranCatCd;

    /** DIS-INT-RATE — PIC S9(04)V99. Interest rate (zoned decimal, 6 bytes). */
    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal disIntRate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisclosureGroupId implements Serializable {
        private String disAcctGroupId;
        private String disTranTypeCd;
        private Integer disTranCatCd;
    }
}
