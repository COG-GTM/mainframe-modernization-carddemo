package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity derived from COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD).
 * Central lookup table that links cards → accounts → customers.
 *
 * <pre>
 * 01 CARD-XREF-RECORD.
 *     05  XREF-CARD-NUM   PIC X(16).
 *     05  XREF-CUST-ID    PIC 9(09).
 *     05  XREF-ACCT-ID    PIC 9(11).
 * </pre>
 */
@Entity
@Table(name = "card_xref")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCrossReference {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    @Size(max = 16)
    @NotNull
    private String cardNumber;

    @Column(name = "account_id", nullable = false, insertable = false, updatable = false)
    private Long accountId;

    @Column(name = "customer_id", nullable = false, insertable = false, updatable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
