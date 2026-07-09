package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card entity — maps COBOL copybook {@code CVACT02Y} (CARD-RECORD, RECLN 150).
 *
 * <p>VSAM KSDS keyed on {@code CARD-NUM} (see {@code CBACT02C}: RECORD KEY IS FD-CARD-NUM).
 * {@code CARD-ACCT-ID} is a foreign key to {@link Account}.</p>
 */
@Entity
@Table(name = "card")
public class Card {

    /** CARD-NUM PIC X(16) — 16-char card number id. */
    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    /** CARD-ACCT-ID PIC 9(11) — FK to account.acct_id. */
    @Column(name = "card_acct_id", length = 11)
    private String cardAcctId;

    /** CARD-CVV-CD PIC 9(03) — kept as String to preserve leading zeros. */
    @Column(name = "card_cvv_cd", length = 3)
    private String cardCvvCd;

    /** CARD-EMBOSSED-NAME PIC X(50). */
    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    /** CARD-EXPIRAION-DATE PIC X(10) (copybook spelling). */
    @Column(name = "card_expiration_date", length = 10)
    private String cardExpirationDate;

    /** CARD-ACTIVE-STATUS PIC X(01). */
    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    public Card() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getCardAcctId() {
        return cardAcctId;
    }

    public void setCardAcctId(String cardAcctId) {
        this.cardAcctId = cardAcctId;
    }

    public String getCardCvvCd() {
        return cardCvvCd;
    }

    public void setCardCvvCd(String cardCvvCd) {
        this.cardCvvCd = cardCvvCd;
    }

    public String getCardEmbossedName() {
        return cardEmbossedName;
    }

    public void setCardEmbossedName(String cardEmbossedName) {
        this.cardEmbossedName = cardEmbossedName;
    }

    public String getCardExpirationDate() {
        return cardExpirationDate;
    }

    public void setCardExpirationDate(String cardExpirationDate) {
        this.cardExpirationDate = cardExpirationDate;
    }

    public String getCardActiveStatus() {
        return cardActiveStatus;
    }

    public void setCardActiveStatus(String cardActiveStatus) {
        this.cardActiveStatus = cardActiveStatus;
    }
}
