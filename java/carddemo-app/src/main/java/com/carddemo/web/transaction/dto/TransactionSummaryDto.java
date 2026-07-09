package com.carddemo.web.transaction.dto;

import java.math.BigDecimal;

/**
 * One row of the {@code COTRN00} (Transaction List) map — the browse line built in
 * {@code POPULATE-TRAN-DATA}: the transaction id, its date (derived from {@code TRAN-ORIG-TS}),
 * description and amount.
 *
 * @param tranId      TRNIDxxO — TRAN-ID PIC X(16)
 * @param date        TDATExxO — MM/DD/YY slice of TRAN-ORIG-TS (YYYY-MM-DD)
 * @param description TDESCxxO — TRAN-DESC PIC X(100) (truncated on the 3270 map)
 * @param amount      TAMTxxxO — TRAN-AMT PIC S9(09)V99
 */
public record TransactionSummaryDto(
        String tranId,
        String date,
        String description,
        BigDecimal amount) {
}
