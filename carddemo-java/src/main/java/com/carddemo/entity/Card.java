package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "card_acct_id")
    private Long cardAcctId;

    @Column(name = "card_cvv_cd")
    private Integer cardCvvCd;

    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    @Column(name = "card_expiration_date", length = 10)
    private String cardExpirationDate;

    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    public Card() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public Long getCardAcctId() { return cardAcctId; }
    public void setCardAcctId(Long cardAcctId) { this.cardAcctId = cardAcctId; }
    public Integer getCardCvvCd() { return cardCvvCd; }
    public void setCardCvvCd(Integer cardCvvCd) { this.cardCvvCd = cardCvvCd; }
    public String getCardEmbossedName() { return cardEmbossedName; }
    public void setCardEmbossedName(String cardEmbossedName) { this.cardEmbossedName = cardEmbossedName; }
    public String getCardExpirationDate() { return cardExpirationDate; }
    public void setCardExpirationDate(String cardExpirationDate) { this.cardExpirationDate = cardExpirationDate; }
    public String getCardActiveStatus() { return cardActiveStatus; }
    public void setCardActiveStatus(String cardActiveStatus) { this.cardActiveStatus = cardActiveStatus; }
}
