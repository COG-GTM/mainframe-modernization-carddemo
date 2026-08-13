package com.carddemo.online.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDSLC — RECEIVE MAP of map CCRDSLA (mapset COCRDSL): ACCTSIDI and
 * CARDSIDI, which feed CC-ACCT-ID and CC-CARD-NUM of copybook CVCRD01Y.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailRequest {

    /** CCARD-AID: ENTER or PF3. */
    private String action;

    private String accountId;
    private String cardNumber;

    public static final String ACTION_ENTER = "ENTER";
    public static final String ACTION_PF3 = "PF3";
}
