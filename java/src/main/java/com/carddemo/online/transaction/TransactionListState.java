package com.carddemo.online.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COTRN00C — CDEMO-CT00-INFO, the program specific extension of the COCOM01Y
 * COMMAREA that carries the browse position between pseudo-conversational invocations of CT00.
 *
 * <p>Held in the HTTP session next to {@link com.carddemo.model.dto.CardDemoCommarea}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListState {

    public static final String SESSION_KEY = "CARDDEMO_CT00_INFO";

    /** CDEMO-CT00-TRNID-FIRST PIC X(16). */
    private String firstTransactionId;

    /** CDEMO-CT00-TRNID-LAST PIC X(16). */
    private String lastTransactionId;

    /** CDEMO-CT00-PAGE-NUM PIC 9(08). */
    private int pageNumber;

    /** CDEMO-CT00-NEXT-PAGE-FLG PIC X(01): 'Y' / 'N'. */
    private boolean nextPageAvailable;
}
