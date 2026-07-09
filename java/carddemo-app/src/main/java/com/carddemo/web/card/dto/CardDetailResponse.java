package com.carddemo.web.card.dto;

import com.carddemo.domain.Card;

/**
 * Card-detail screen payload ({@code COCRDSL} / {@code COCRDSLC}).
 *
 * <p>Mirrors the fields the detail map presents after {@code 9100-GETCARD-BYACCTCARD}
 * reads {@code CARD-RECORD} (copybook {@code CVACT02Y}). The stored expiry date is
 * {@code CARD-EXPIRAION-DATE} PIC X(10) in {@code YYYY-MM-DD} form; the individual
 * year/month/day parts (as the {@code COCRDUP} map splits them) are exposed for
 * convenience.</p>
 *
 * @param accountId      CARD-ACCT-ID PIC 9(11).
 * @param cardNumber     CARD-NUM PIC X(16).
 * @param cvvCode        CARD-CVV-CD PIC 9(03).
 * @param embossedName   CARD-EMBOSSED-NAME PIC X(50).
 * @param expirationDate CARD-EXPIRAION-DATE PIC X(10) — {@code YYYY-MM-DD}.
 * @param expiryYear     year part of the expiry date (chars 1-4), or {@code null}.
 * @param expiryMonth    month part of the expiry date (chars 6-7), or {@code null}.
 * @param expiryDay      day part of the expiry date (chars 9-10), or {@code null}.
 * @param activeStatus   CARD-ACTIVE-STATUS PIC X(01).
 */
public record CardDetailResponse(
        String accountId,
        String cardNumber,
        String cvvCode,
        String embossedName,
        String expirationDate,
        String expiryYear,
        String expiryMonth,
        String expiryDay,
        String activeStatus) {

    public static CardDetailResponse from(Card card) {
        String date = card.getCardExpirationDate();
        String year = null;
        String month = null;
        String day = null;
        if (date != null && date.length() >= 10) {
            year = date.substring(0, 4);
            month = date.substring(5, 7);
            day = date.substring(8, 10);
        }
        return new CardDetailResponse(
            card.getCardAcctId(),
            card.getCardNum(),
            card.getCardCvvCd(),
            card.getCardEmbossedName(),
            date,
            year,
            month,
            day,
            card.getCardActiveStatus());
    }
}
