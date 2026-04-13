package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD).
 * Total COBOL record length: 50 bytes.
 * Seed data file: cardxref.txt (50 records).
 * Cross-reference linking cards to customers and accounts.
 */
@Entity
@Table(name = "card_xref")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardXref {

    /** XREF-CARD-NUM — PIC X(16), bytes [0:16]. Card number (primary key). */
    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    /** XREF-CUST-ID — PIC 9(09), bytes [16:25]. Customer ID. */
    @Column(name = "xref_cust_id")
    private Long xrefCustId;

    /** XREF-ACCT-ID — PIC 9(11), bytes [25:36]. Account ID. */
    @Column(name = "xref_acct_id")
    private Long xrefAcctId;

    // FILLER — PIC X(14), bytes [36:50]. Padding — not mapped.
}
