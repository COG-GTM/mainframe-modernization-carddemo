package com.carddemo.online.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDLIC — WS-THIS-PROGCOMMAREA: the first and last card key of the
 * page on display, the screen (page) number, whether the last page has been shown and
 * whether a next page exists. It travels on the COMMAREA between turns of CCLI.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardListState {

    public static final String SESSION_KEY = "COCRDLIC_PROGRAM_COMMAREA";

    /** WS-CA-LAST-CARD-NUM. */
    private String lastCardNumber = "";
    /** WS-CA-LAST-CARD-ACCT-ID. */
    private String lastCardAccountId = "";
    /** WS-CA-FIRST-CARD-NUM. */
    private String firstCardNumber = "";
    /** WS-CA-FIRST-CARD-ACCT-ID. */
    private String firstCardAccountId = "";
    /**
     * WS-CA-SCREEN-NUM; 88 CA-FIRST-PAGE VALUE 1. A task entering CCLI from the menu
     * runs SET CA-FIRST-PAGE TO TRUE, so a fresh state starts on page one.
     */
    private int screenNumber = 1;
    /** WS-CA-LAST-PAGE-DISPLAYED; 88 CA-LAST-PAGE-SHOWN VALUE 0, NOT-SHOWN VALUE 9. */
    private int lastPageDisplayed = 9;
    /** WS-CA-NEXT-PAGE-IND; 88 CA-NEXT-PAGE-EXISTS VALUE 'Y'. */
    private boolean nextPageExists;

    public boolean isFirstPage() {
        return screenNumber == 1;
    }

    public boolean isLastPageShown() {
        return lastPageDisplayed == 0;
    }
}
