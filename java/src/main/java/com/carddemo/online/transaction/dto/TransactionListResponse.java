package com.carddemo.online.transaction.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COTRN00C — output fields of BMS map COTRN0A (mapset COTRN00).
 *
 * <p>{@code errorMessage} carries ERRMSG verbatim; CT00 also shows informational text there (for
 * example the top/bottom of file notices), which is why a message can accompany a successful page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListResponse {

    /** ERR-FLG-OFF. */
    private boolean success;

    /** ERRMSG PIC X(78). */
    private String errorMessage;

    /** PAGENUM — CDEMO-CT00-PAGE-NUM. */
    private int pageNumber;

    /** CDEMO-CT00-NEXT-PAGE-FLG. */
    private boolean nextPageAvailable;

    /** The (at most ten) detail lines currently displayed. */
    private List<TransactionListRow> transactions;

    /** CDEMO-TO-PROGRAM when a row was selected with 'S' (XCTL to COTRN01C). */
    private String nextProgram;

    /** CDEMO-CT00-TRN-SELECTED handed over to COTRN01C. */
    private String selectedTransactionId;
}
