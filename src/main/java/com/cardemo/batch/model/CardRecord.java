package com.cardemo.batch.model;

/**
 * Domain model corresponding to CVACT02Y.cpy CARD-RECORD (150 bytes).
 */
public class CardRecord {

    private String cardNum;             // PIC X(16)
    private String cardAcctId;          // PIC 9(11)
    private String cardCvvCd;           // PIC 9(03)
    private String cardEmbossedName;    // PIC X(50)
    private String cardExpiraionDate;   // PIC X(10) - typo preserved from COBOL
    private String cardActiveStatus;    // PIC X(01)

    public CardRecord() {
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

    public String getCardExpiraionDate() {
        return cardExpiraionDate;
    }

    public void setCardExpiraionDate(String cardExpiraionDate) {
        this.cardExpiraionDate = cardExpiraionDate;
    }

    public String getCardActiveStatus() {
        return cardActiveStatus;
    }

    public void setCardActiveStatus(String cardActiveStatus) {
        this.cardActiveStatus = cardActiveStatus;
    }

    @Override
    public String toString() {
        return "CardRecord{" +
                "cardNum='" + cardNum + '\'' +
                ", cardAcctId='" + cardAcctId + '\'' +
                ", cardCvvCd='" + cardCvvCd + '\'' +
                ", cardEmbossedName='" + cardEmbossedName + '\'' +
                ", cardExpiraionDate='" + cardExpiraionDate + '\'' +
                ", cardActiveStatus='" + cardActiveStatus + '\'' +
                '}';
    }
}
