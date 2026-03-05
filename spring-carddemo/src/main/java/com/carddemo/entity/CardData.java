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
 * JPA entity derived from COBOL copybook CVACT02Y.cpy (CARD-RECORD).
 *
 * <pre>
 * 01  CARD-RECORD.
 *     05  CARD-NUM              PIC X(16).
 *     05  CARD-ACCT-ID          PIC 9(11).
 *     05  CARD-CVV-CD           PIC 9(03).
 *     05  CARD-EMBOSSED-NAME    PIC X(50).
 *     05  CARD-EXPIRAION-DATE   PIC X(10).
 *     05  CARD-ACTIVE-STATUS    PIC X(01).
 * </pre>
 */
@Entity
@Table(name = "card_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardData {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    @Size(max = 16)
    @NotNull
    private String cardNumber;

    @Column(name = "account_id", nullable = false, insertable = false, updatable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "cvv_code")
    private Integer cvvCode;

    @Column(name = "embossed_name", length = 50)
    @Size(max = 50)
    private String embossedName;

    @Column(name = "expiration_date", length = 10)
    @Size(max = 10)
    private String expirationDate;

    @Column(name = "card_status", length = 1)
    @Size(max = 1)
    private String cardStatus;
}
