package com.carddemo.web.card.dto;

import com.carddemo.domain.Card;

/**
 * One row of the card-list screen ({@code COCRDLI} / {@code COCRDLIC}).
 *
 * <p>The BMS map ({@code app/bms/COCRDLI.bms}) shows only the account number, card number
 * and active-status flag per line (fields {@code ACCTNOnn}, {@code CRDNUMnn},
 * {@code CRDSTSnn}); the selection field is an input, not part of the row data.</p>
 *
 * @param accountId    CARD-ACCT-ID PIC 9(11) — 11-char zero-padded id.
 * @param cardNumber   CARD-NUM PIC X(16) — 16-char card number.
 * @param activeStatus CARD-ACTIVE-STATUS PIC X(01) — {@code Y}/{@code N}.
 */
public record CardSummaryResponse(String accountId, String cardNumber, String activeStatus) {

    public static CardSummaryResponse from(Card card) {
        return new CardSummaryResponse(
            card.getCardAcctId(), card.getCardNum(), card.getCardActiveStatus());
    }
}
