package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

/**
 * CARD-XREF-RECORD, copybook CVACT03Y, LRECL 50 (CARDXREF / cardxref.txt).
 */
public class CardXref {

    public static final int RECORD_LENGTH = 50;

    private String cardNumber;
    private String customerId;
    private String accountId;

    public CardXref() {
    }

    public CardXref(String cardNumber, String customerId, String accountId) {
        this.cardNumber = cardNumber;
        this.customerId = customerId;
        this.accountId = accountId;
    }

    public static CardXref parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        CardXref xref = new CardXref();
        xref.cardNumber = cursor.fixed(16);
        xref.customerId = cursor.fixed(9);
        xref.accountId = cursor.fixed(11);
        return xref;
    }

    public String format() {
        return CobolCodec.encodeText(cardNumber, 16)
                + CobolCodec.encodeText(customerId, 9)
                + CobolCodec.encodeText(accountId, 11)
                + " ".repeat(14);
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "CardXref[" + cardNumber + " -> customer=" + customerId + ", account=" + accountId + "]";
    }
}
