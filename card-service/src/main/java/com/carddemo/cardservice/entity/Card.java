package com.carddemo.cardservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y.cpy — CARD-RECORD (150 bytes).
 *
 * <pre>
 * 01 CARD-RECORD.
 *   05 CARD-NUM              PIC X(16).       -- bytes 1-16
 *   05 CARD-ACCT-ID          PIC 9(11).       -- bytes 17-27
 *   05 CARD-CVV-CD           PIC 9(03).       -- bytes 28-30
 *   05 CARD-EMBOSSED-NAME    PIC X(50).       -- bytes 31-80
 *   05 CARD-EXPIRAION-DATE   PIC X(10).       -- bytes 81-90
 *   05 CARD-ACTIVE-STATUS    PIC X(01).       -- byte 91 (Y/N)
 *   05 FILLER                PIC X(59).       -- bytes 92-150
 * </pre>
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "card_acct_id", nullable = false)
    private Long cardAcctId;

    @Column(name = "card_cvv_cd", nullable = false)
    private Integer cardCvvCd;

    @Column(name = "card_embossed_name", length = 50, nullable = false)
    private String cardEmbossedName;

    @Column(name = "card_expiration_date", length = 10, nullable = false)
    private String cardExpirationDate;

    @Column(name = "card_active_status", length = 1, nullable = false)
    private String cardActiveStatus;

    protected Card() {
    }

    public Card(String cardNum, Long cardAcctId, Integer cardCvvCd,
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

    public Long getCardAcctId() {
        return cardAcctId;
    }

    public void setCardAcctId(Long cardAcctId) {
        this.cardAcctId = cardAcctId;
    }

    public Integer getCardCvvCd() {
        return cardCvvCd;
    }

    public void setCardCvvCd(Integer cardCvvCd) {
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
