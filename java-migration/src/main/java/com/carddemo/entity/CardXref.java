package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing the Card Cross-Reference record.
 * <p>
 * Migrated from COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD, RECLN 50):
 * <ul>
 *   <li>XREF-CARD-NUM  PIC X(16) — primary key</li>
 *   <li>XREF-CUST-ID   PIC 9(09) — customer identifier</li>
 *   <li>XREF-ACCT-ID   PIC 9(11) — account identifier</li>
 *   <li>FILLER          PIC X(14) — not mapped</li>
 * </ul>
 */
@Entity
@Table(name = "card_xrefs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardXref {

    /**
     * Card number (XREF-CARD-NUM, PIC X(16)).
     */
    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    /**
     * Customer identifier (XREF-CUST-ID, PIC 9(09)).
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Account identifier (XREF-ACCT-ID, PIC 9(11)).
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;
}
