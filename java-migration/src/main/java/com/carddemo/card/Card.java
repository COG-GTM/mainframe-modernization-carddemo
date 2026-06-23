package com.carddemo.card;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping the COBOL {@code CARD-RECORD} (copybook
 * {@code app/cpy/CVACT02Y.cpy}, VSAM CARDDAT KSDS, record length 150, key =
 * CARD-NUM).
 *
 * <p>Field mappings preserve the original COBOL field names and PIC clauses for
 * traceability. The 59-byte trailing {@code FILLER} is intentionally not
 * mapped. {@code CARD-NUM} is alphanumeric ({@code PIC X(16)}) and is therefore
 * modeled as a {@link String} primary key rather than a numeric type.</p>
 */
@Entity
@Table(name = "card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    /** COBOL: {@code CARD-NUM PIC X(16)} — VSAM primary key (offset 0, len 16). */
    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    /** COBOL: {@code CARD-ACCT-ID PIC 9(11)} — owning account id (offset 16, len 11). */
    @Column(name = "card_acct_id")
    private Long cardAcctId;

    /** COBOL: {@code CARD-CVV-CD PIC 9(03)} — card verification value (offset 27, len 3). */
    @Column(name = "card_cvv_cd")
    private Integer cardCvvCd;

    /** COBOL: {@code CARD-EMBOSSED-NAME PIC X(50)} — embossed name (offset 30, len 50). */
    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    /** COBOL: {@code CARD-EXPIRAION-DATE PIC X(10)} — YYYY-MM-DD (offset 80, len 10). */
    @Column(name = "card_expiration_date")
    private LocalDate cardExpirationDate;

    /** COBOL: {@code CARD-ACTIVE-STATUS PIC X(01)} — Y/N flag (offset 90, len 1). */
    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;
}
