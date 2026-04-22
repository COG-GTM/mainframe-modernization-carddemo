package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVACT03Y.cpy — CARD-XREF-RECORD (50 bytes).
 */
@Entity
@Table(name = "card_cross_references")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCrossReference {

    @Id
    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "acct_id")
    private Long accountId;
}
