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
 * Card entity mapped from COBOL copybook CVACT02Y.cpy (150-byte record).
 *
 * COBOL layout:
 *   CARD-NUM              PIC X(16)  — primary key
 *   CARD-ACCT-ID          PIC 9(11)  — linked account
 *   CARD-CVV-CD           PIC 9(03)  — CVV code
 *   CARD-EMBOSSED-NAME    PIC X(50)  — name on card
 *   CARD-EXPIRAION-DATE   PIC X(10)  — expiration date (YYYY-MM-DD)
 *   CARD-ACTIVE-STATUS    PIC X(01)  — Y or N
 *   FILLER                PIC X(59)
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;

    @Column(name = "cvv_code", length = 3)
    private String cvvCode;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;
}
