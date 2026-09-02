package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

/**
 * CARD-RECORD, copybook CVACT02Y, LRECL 150 (CARDDATA / carddata.txt).
 */
public class Card {

    public static final int RECORD_LENGTH = 150;

    private String cardNumber;
    private String accountId;
    private int cvvCode;
    private String embossedName;
    private String expirationDate;
    private char activeStatus;

    public static Card parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        Card card = new Card();
        card.cardNumber = cursor.fixed(16);
        card.accountId = cursor.fixed(11);
        card.cvvCode = cursor.integer(3);
        card.embossedName = cursor.text(50);
        card.expirationDate = cursor.text(10);
        card.activeStatus = cursor.flag();
        return card;
    }

    public String format() {
        return CobolCodec.encodeText(cardNumber, 16)
                + CobolCodec.encodeText(accountId, 11)
                + CobolCodec.encodeNumeric(cvvCode, 3)
                + CobolCodec.encodeText(embossedName, 50)
                + CobolCodec.encodeText(expirationDate, 10)
                + activeStatus
                + " ".repeat(59);
    }

    public boolean isActive() {
        return activeStatus == 'Y';
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getCvvCode() {
        return cvvCode;
    }

    public void setCvvCode(int cvvCode) {
        this.cvvCode = cvvCode;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public char getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(char activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public String toString() {
        return "Card[" + cardNumber + ", account=" + accountId + ", status=" + activeStatus + "]";
    }
}
