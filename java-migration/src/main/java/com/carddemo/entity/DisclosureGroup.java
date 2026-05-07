package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA entity representing a Disclosure Group record.
 * <p>
 * Migrated from COBOL copybook CVTRA02Y.cpy (RECLN = 50):
 * <pre>
 * 01  DIS-GROUP-RECORD.
 *     05  DIS-GROUP-KEY.
 *        10 DIS-ACCT-GROUP-ID    PIC X(10).
 *        10 DIS-TRAN-TYPE-CD     PIC X(02).
 *        10 DIS-TRAN-CAT-CD      PIC 9(04).
 *     05  DIS-INT-RATE           PIC S9(04)V99.
 *     05  FILLER                 PIC X(28).
 * </pre>
 * <p>
 * The FILLER field (28 bytes of padding) is intentionally not mapped.
 *
 * @see DisclosureGroupId
 */
@Entity
@Table(name = "disclosure_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureGroup {

    @EmbeddedId
    private DisclosureGroupId id;

    /**
     * Interest rate for this disclosure group.
     * COBOL: DIS-INT-RATE PIC S9(04)V99 — signed with 2 implied decimal places.
     */
    @Column(name = "interest_rate", precision = 6, scale = 2, nullable = false)
    private BigDecimal interestRate;
}
