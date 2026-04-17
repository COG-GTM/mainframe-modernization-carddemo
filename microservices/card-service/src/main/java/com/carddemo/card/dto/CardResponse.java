package com.carddemo.card.dto;

import com.carddemo.card.entity.Card;

/**
 * Response DTO for card data. Maps from Card entity.
 */
public record CardResponse(
        String cardNum,
        String cardAcctId,
        String cardCvvCd,
        String cardEmbossedName,
        String cardExpirationDate,
        String cardActiveStatus
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getCardNum(),
                card.getCardAcctId(),
                card.getCardCvvCd(),
                card.getCardEmbossedName(),
                card.getCardExpirationDate(),
                card.getCardActiveStatus()
        );
    }
}
