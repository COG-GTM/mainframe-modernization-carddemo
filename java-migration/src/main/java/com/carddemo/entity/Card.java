package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity representing a credit card record.
 * Migrated from COBOL copybook CVACT02Y.cpy (CARD-RECORD, RECLN 150).
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    /**
     * Card number (primary key).
     * COBOL: CARD-NUM PIC X(16)
     */
    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    /**
     * Account identifier linked to this card.
     * COBOL: CARD-ACCT-ID PIC 9(11)
     */
    @Column(name = "card_acct_id", nullable = false)
    private Long accountId;

    /**
     * Card verification value code.
     * COBOL: CARD-CVV-CD PIC 9(03)
     */
    @Column(name = "card_cvv_cd", nullable = false)
    private Integer cvvCode;

    /**
     * Name embossed on the physical card.
     * COBOL: CARD-EMBOSSED-NAME PIC X(50)
     */
    @Column(name = "card_embossed_name", length = 50, nullable = false)
    private String embossedName;

    /**
     * Expiration date of the card.
     * COBOL: CARD-EXPIRAION-DATE PIC X(10)
     */
    @Column(name = "card_expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * Active status flag (e.g., 'Y' for active).
     * COBOL: CARD-ACTIVE-STATUS PIC X(01)
     */
    @Column(name = "card_active_status", length = 1, nullable = false)
    private String activeStatus;
}
