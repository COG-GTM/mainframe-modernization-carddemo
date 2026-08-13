package com.carddemo.online.card;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDUPC — 3000-SEND-MAP of map CCRDUPA (mapset COCRDUP): the card
 * fields on display, INFOMSGO (3250-SETUP-INFOMSG), ERRMSGO (WS-RETURN-MSG) and the
 * state CCUP-CHANGE-ACTION that the next turn will act on.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateResponse {

    /** CCUP-CHANGE-ACTION. */
    private String changeAction;

    /** WS-INFO-MSG. */
    private String infoMessage;

    /** WS-RETURN-MSG. */
    private String errorMessage;

    /** The FLG-x-NOT-OK / FLG-x-BLANK flags, by map field. */
    private Map<String, String> fieldErrors;

    /** The card data on display: CCUP-NEW-DETAILS, or CCUP-OLD-DETAILS when just fetched. */
    private CardUpdateData data;

    private String nextProgram;
    private String nextTransaction;
    private String nextMapset;
    private String nextMap;
}
