package com.carddemo.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y (CARD-RECORD, RECLN 150).
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "card_acct_id", length = 11, nullable = false)
    private String cardAcctId;

    @Column(name = "card_cvv_cd", length = 3)
    private String cardCvvCd;

    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    @Column(name = "card_expiration_date", length = 10)
    private String cardExpirationDate;

    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    public Card() {
    }

    public Card(String cardNum, String cardAcctId, String cardCvvCd,
                String cardEmbossedName, String cardExpirationDate,
                String cardActiveStatus) {
        this.cardNum = cardNum;
        this.cardAcctId = cardAcctId;
        this.cardCvvCd = cardCvvCd;
        this.cardEmbossedName = cardEmbossedName;
        this.cardExpirationDate = cardExpirationDate;
        this.cardActiveStatus = cardActiveStatus;
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
