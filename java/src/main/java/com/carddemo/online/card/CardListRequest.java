package com.carddemo.online.card;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDLIC — RECEIVE MAP of map CCRDLIA (mapset COCRDLI):
 * ACCTSIDI, CARDSIDI and the seven CRDSELnI action fields, plus the AID key.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardListRequest {

    /** CCARD-AID: ENTER, PF3 (exit), PF7 (page up) or PF8 (page down). */
    private String action;

    /** ACCTSIDI — CC-ACCT-ID. */
    private String accountIdFilter;

    /** CARDSIDI — CC-CARD-NUM. */
    private String cardNumberFilter;

    /** CRDSEL1I..CRDSEL7I: 'S' to view, 'U' to update, blank otherwise. */
    private List<String> selections;

    public static final String ACTION_ENTER = "ENTER";
    public static final String ACTION_PF3 = "PF3";
    public static final String ACTION_PF7 = "PF7";
    public static final String ACTION_PF8 = "PF8";
}
