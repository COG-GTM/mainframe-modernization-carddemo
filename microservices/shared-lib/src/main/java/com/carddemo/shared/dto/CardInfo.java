package com.carddemo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps to CDEMO-CARD-INFO in COCOM01Y.cpy.
 *
 * <pre>
 * 05 CDEMO-CARD-INFO.
 *   10 CDEMO-CARD-NUM            PIC 9(16).
 * </pre>
 */
public class CardInfo {

    @JsonProperty("cardNum")
    private String cardNum;

    public CardInfo() {
    }

    public CardInfo(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }
}
