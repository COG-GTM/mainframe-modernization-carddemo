package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD, RECLN 50).
 * Cross-reference linking card numbers to customer and account identifiers.
 */
@Entity
@Table(name = "card_xrefs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardXref {

    /** XREF-CARD-NUM — PIC X(16). Primary key, card number for cross-reference. */
    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    /** XREF-CUST-ID — PIC 9(09). Customer identifier linked to this card. */
    @Column(name = "xref_cust_id")
    private Long xrefCustId;

    /** XREF-ACCT-ID — PIC 9(11). Account identifier linked to this card. */
    @Column(name = "xref_acct_id")
    private Long xrefAcctId;
}
