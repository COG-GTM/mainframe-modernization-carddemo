package com.carddemo.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Card cross-reference entity mapped from COBOL copybook CVACT03Y.cpy (50-byte record).
 *
 * COBOL layout:
 *   XREF-CARD-NUM   PIC X(16)
 *   XREF-CUST-ID    PIC 9(09)
 *   XREF-ACCT-ID    PIC 9(11)
 */
@Entity
@Table(name = "card_xref")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardXref {

    @Id
    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "account_id")
    private Long accountId;
}
