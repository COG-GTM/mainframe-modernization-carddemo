package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVACT03Y (CARD-XREF-RECORD), VSAM file CARDXREF, RECLN 50.
 */
@Entity
@Table(name = "card_xref")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardXref {

    /** XREF-CARD-NUM PIC X(16). */
    @Id
    @Column(name = "xref_card_num", length = 16, nullable = false)
    private String cardNumber;

    /** XREF-CUST-ID PIC 9(09). */
    @Column(name = "xref_cust_id")
    private Long customerId;

    /** XREF-ACCT-ID PIC 9(11). */
    @Column(name = "xref_acct_id")
    private Long accountId;
}
