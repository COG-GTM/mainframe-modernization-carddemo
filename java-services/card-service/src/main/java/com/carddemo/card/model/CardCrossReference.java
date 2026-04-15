package com.carddemo.card.model;

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
 *   XREF-CARD-NUM   PIC X(16)  — card number (primary key)
 *   XREF-CUST-ID    PIC 9(09)  — customer ID
 *   XREF-ACCT-ID    PIC 9(11)  — account ID
 *   FILLER           PIC X(14)
 */
@Entity
@Table(name = "card_cross_references")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCrossReference {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "customer_id", length = 9, nullable = false)
    private String customerId;

    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;
}
