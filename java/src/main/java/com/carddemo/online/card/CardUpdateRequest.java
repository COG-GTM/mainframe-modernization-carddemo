package com.carddemo.online.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDUPC — 1100-RECEIVE-MAP of map CCRDUPA (mapset COCRDUP):
 * ACCTSIDI, CARDSIDI, CRDNAMEI, CRDSTCDI, EXPMONI, EXPYEARI plus the AID key.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateRequest {

    /** ENTER, PF3 (exit), PF5 (save) or PF12 (cancel changes). */
    private String action;

    private String accountId;
    private String cardNumber;
    /** CRDNAMEI. */
    private String cardName;
    /** CRDSTCDI. */
    private String activeStatus;
    /** EXPMONI. */
    private String expiryMonth;
    /** EXPYEARI. */
    private String expiryYear;

    public static final String ACTION_ENTER = "ENTER";
    public static final String ACTION_PF3 = "PF3";
    public static final String ACTION_PF5 = "PF5";
    public static final String ACTION_PF12 = "PF12";
}
