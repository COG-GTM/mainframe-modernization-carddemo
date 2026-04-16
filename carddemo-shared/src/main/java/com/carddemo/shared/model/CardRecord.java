package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVACT02Y — Card Record (RECLN 150).
 * <pre>
 * 01 CARD-RECORD.
 *   05 CARD-NUM                   PIC X(16)
 *   05 CARD-ACCT-ID               PIC 9(11)
 *   05 CARD-CVV-CD                PIC 9(03)
 *   05 CARD-EMBOSSED-NAME         PIC X(50)
 *   05 CARD-EXPIRAION-DATE        PIC X(10)
 *   05 CARD-ACTIVE-STATUS         PIC X(01)
 *   05 FILLER                     PIC X(59)
 * </pre>
 */
public class CardRecord {

    @JsonProperty("cardNum")
    private String cardNum;

    @JsonProperty("cardAcctId")
    private long cardAcctId;

    @JsonProperty("cardCvvCd")
    private int cardCvvCd;

    @JsonProperty("cardEmbossedName")
    private String cardEmbossedName;

    @JsonProperty("cardExpirationDate")
    private String cardExpirationDate;

    @JsonProperty("cardActiveStatus")
    private String cardActiveStatus;

    public CardRecord() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public long getCardAcctId() {
        return cardAcctId;
    }

    public void setCardAcctId(long cardAcctId) {
        this.cardAcctId = cardAcctId;
    }

    public int getCardCvvCd() {
        return cardCvvCd;
    }

    public void setCardCvvCd(int cardCvvCd) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardRecord that = (CardRecord) o;
        return cardAcctId == that.cardAcctId
                && cardCvvCd == that.cardCvvCd
                && Objects.equals(cardNum, that.cardNum)
                && Objects.equals(cardEmbossedName, that.cardEmbossedName)
                && Objects.equals(cardExpirationDate, that.cardExpirationDate)
                && Objects.equals(cardActiveStatus, that.cardActiveStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNum, cardAcctId, cardCvvCd, cardEmbossedName,
                cardExpirationDate, cardActiveStatus);
    }

    @Override
    public String toString() {
        return "CardRecord{" +
                "cardNum='" + cardNum + '\'' +
                ", cardAcctId=" + cardAcctId +
                ", cardCvvCd=" + cardCvvCd +
                ", cardEmbossedName='" + cardEmbossedName + '\'' +
                ", cardExpirationDate='" + cardExpirationDate + '\'' +
                ", cardActiveStatus='" + cardActiveStatus + '\'' +
                '}';
    }
}
