package com.carddemo.cardxref;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping the COBOL {@code CARD-XREF-RECORD} (copybook
 * {@code app/cpy/CVACT03Y.cpy}, VSAM CARDXREF KSDS, record length 50,
 * key = {@code XREF-CARD-NUM}).
 *
 * <p>Field mappings preserve the original COBOL field names and PIC clauses for
 * traceability. The 14-byte trailing {@code FILLER} (offset 36, len 14) is
 * intentionally not mapped; it is also absent from the ASCII seed extract, whose
 * lines are 36 characters long.</p>
 */
@Entity
@Table(name = "card_xref")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardXref {

    /** COBOL: {@code XREF-CARD-NUM PIC X(16)} — VSAM primary key (offset 0, len 16). */
    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    /** COBOL: {@code XREF-CUST-ID PIC 9(09)} — customer id (offset 16, len 9). */
    @Column(name = "xref_cust_id")
    private Long xrefCustId;

    /** COBOL: {@code XREF-ACCT-ID PIC 9(11)} — account id (offset 25, len 11). */
    @Column(name = "xref_acct_id")
    private Long xrefAcctId;
}
