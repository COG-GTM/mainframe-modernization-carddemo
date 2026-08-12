package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVTRA02Y (DIS-GROUP-RECORD), VSAM file DISCGRP, RECLN 50.
 * Holds the annual interest rate used by the interest calculation batch (CBACT04C).
 */
@Entity
@Table(name = "disclosure_group")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureGroup {

    @EmbeddedId
    private DisclosureGroupId id;

    /** DIS-INT-RATE PIC S9(04)V99. */
    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;
}
