package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y.cpy (CARD-RECORD, RECLN 150).
 * Represents a credit card in the CardDemo system.
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    /** CARD-NUM — PIC X(16). Primary key, 16-character card number. */
    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    /** CARD-ACCT-ID — PIC 9(11). Associated account identifier. */
    @Column(name = "card_acct_id")
    private Long cardAcctId;

    /** CARD-CVV-CD — PIC 9(03). Card verification value (3 digits). */
    @Column(name = "card_cvv_cd")
    private Integer cardCvvCd;

    /** CARD-EMBOSSED-NAME — PIC X(50). Name embossed on card. */
    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    /** CARD-EXPIRAION-DATE — PIC X(10). Expiration date in YYYY-MM-DD format. */
    @Column(name = "card_expiration_date")
    private LocalDate cardExpirationDate;

    /** CARD-ACTIVE-STATUS — PIC X(01). Card active status flag. */
    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;
}
