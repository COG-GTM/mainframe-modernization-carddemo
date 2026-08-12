package com.carddemo.online.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COTRN00C — input fields of BMS map COTRN0A (mapset COTRN00).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListRequest {

    /** AID key: ENTER, PF7 (page up) or PF8 (page down). */
    public enum Action {
        ENTER,
        PF7,
        PF8
    }

    /** EIBAID. Defaults to ENTER, as an empty request maps to pressing enter on an empty screen. */
    private Action action;

    /** TRNIDIN PIC X(16) — the transaction id the browse should be positioned on. */
    private String transactionIdFilter;

    /** SEL0001..SEL0010 PIC X(01) — the selection flag typed next to a listed transaction. */
    private String selectionFlag;

    /** TRNID01..TRNID10 — the transaction id of the row the selection flag was typed on. */
    private String selectedTransactionId;

    public Action actionOrEnter() {
        return action == null ? Action.ENTER : action;
    }
}
