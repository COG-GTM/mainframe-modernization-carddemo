package com.carddemo.cardservice.dto;

import com.carddemo.cardservice.entity.Card;

/**
 * Response DTO for card data, mapped from CARD-RECORD layout.
 */
public record CardResponse(
        String cardNum,
        Long accountId,
        Integer cvvCode,
        String embossedName,
        String expirationDate,
        String activeStatus
) {

    public static CardResponse fromEntity(Card card) {
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
