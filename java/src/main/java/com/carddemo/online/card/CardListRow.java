package com.carddemo.online.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDLIC — one occurrence of WS-SCREEN-ROWS (WS-ROW-ACCTNO,
 * WS-ROW-CARD-NUM, WS-ROW-CARD-STATUS) as shown on map CCRDLIA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardListRow {

    /** 1 based row number on the screen. */
    private int rowNumber;

    private String accountId;
    private String cardNumber;
    private String activeStatus;

    /** CRDSELnI as keyed by the user. */
    private String selection;

    /** WS-ROW-CRDSELECT-ERROR: the action field of this row is in error. */
    private boolean selectionError;
}
