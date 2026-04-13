package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA02Y.cpy (DIS-GROUP-RECORD).
 * Total COBOL record length: 50 bytes.
 * Seed data file: discgrp.txt.
 * Composite key: (disAcctGroupId, disTranTypeCd, disTranCatCd).
 */
@Entity
@Table(name = "disclosure_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    /** DIS-ACCT-GROUP-ID — PIC X(10), bytes [0:10]. Account group ID (part of composite key). */
    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String disAcctGroupId;

    /** DIS-TRAN-TYPE-CD — PIC X(02), bytes [10:12]. Transaction type code (part of composite key). */
    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String disTranTypeCd;

    /** DIS-TRAN-CAT-CD — PIC 9(04), bytes [12:16]. Transaction category code (part of composite key). */
    @Id
    @Column(name = "dis_tran_cat_cd")
    private Integer disTranCatCd;

    /** DIS-INT-RATE — PIC S9(04)V99, bytes [16:22]. Interest rate (zoned decimal). */
    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal disIntRate;

    // FILLER — PIC X(28), bytes [22:50]. Padding — not mapped.
}
