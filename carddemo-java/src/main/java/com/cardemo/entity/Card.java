package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y.cpy (CARD-RECORD).
 * Total COBOL record length: 150 bytes.
 * Seed data file: carddata.txt (50 records).
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    /** CARD-NUM — PIC X(16), bytes [0:16]. Card number (primary key). */
    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    /** CARD-ACCT-ID — PIC 9(11), bytes [16:27]. Associated account ID. */
    @Column(name = "card_acct_id")
    private Long cardAcctId;

    /** CARD-CVV-CD — PIC 9(03), bytes [27:30]. CVV code. */
    @Column(name = "card_cvv_cd")
    private Integer cardCvvCd;

    /** CARD-EMBOSSED-NAME — PIC X(50), bytes [30:80]. Name embossed on card. */
    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    /** CARD-EXPIRAION-DATE — PIC X(10), bytes [80:90]. Expiration date (YYYY-MM-DD). */
    @Column(name = "card_expiration_date")
    private LocalDate cardExpirationDate;

    /** CARD-ACTIVE-STATUS — PIC X(01), bytes [90:91]. Active flag (Y/N). */
    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    // FILLER — PIC X(59), bytes [91:150]. Padding — not mapped.
}
