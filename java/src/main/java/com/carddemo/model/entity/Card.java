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
 * COBOL copybook: CVACT02Y (CARD-RECORD), VSAM file CARDDATA, RECLN 150.
 */
@Entity
@Table(name = "card")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    /** CARD-NUM PIC X(16). */
    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNumber;

    /** CARD-ACCT-ID PIC 9(11). */
    @Column(name = "card_acct_id")
    private Long accountId;

    /** CARD-CVV-CD PIC 9(03). */
    @Column(name = "card_cvv_cd")
    private Integer cvvCode;

    /** CARD-EMBOSSED-NAME PIC X(50). */
    @Column(name = "card_embossed_name", length = 50)
    private String embossedName;

    /** CARD-EXPIRAION-DATE PIC X(10) (spelling as in the copybook). */
    @Column(name = "card_expiration_date", length = 10)
    private String expirationDate;

    /** CARD-ACTIVE-STATUS PIC X(01). */
    @Column(name = "card_active_status", length = 1)
    private String activeStatus;
}
