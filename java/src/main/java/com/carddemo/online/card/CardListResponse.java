package com.carddemo.online.card;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDLIC — SEND MAP of map CCRDLIA (mapset COCRDLI): the seven rows,
 * the page number, INFOMSGO (WS-INFO-MSG), ERRMSGO (WS-ERROR-MSG) and the navigation
 * fields CCARD-NEXT-PROG / CCARD-NEXT-MAPSET / CCARD-NEXT-MAP of CVCRD01Y.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardListResponse {

    private List<CardListRow> rows;

    /** WS-CA-SCREEN-NUM. */
    private int pageNumber;

    /** CA-NEXT-PAGE-EXISTS. */
    private boolean nextPageExists;

    /** WS-INFO-MSG (TYPE S FOR DETAIL, U TO UPDATE ANY RECORD). */
    private String infoMessage;

    /** WS-ERROR-MSG. */
    private String errorMessage;

    /** FLG-PROTECT-SELECT-ROWS-YES: the action fields are protected after a filter error. */
    private boolean selectRowsProtected;

    private String nextProgram;
    private String nextTransaction;
    private String nextMapset;
    private String nextMap;

    /** CDEMO-ACCT-ID / CDEMO-CARD-NUM handed to COCRDSLC or COCRDUPC. */
    private String selectedAccountId;
    private String selectedCardNumber;
}
