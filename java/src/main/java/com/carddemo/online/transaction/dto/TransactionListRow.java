package com.carddemo.online.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COTRN00C — one of the ten detail lines of BMS map COTRN0A
 * (TRNID0n / TDATE0n / TDESC0n / TAMT00n), populated from copybook CVTRA05Y.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListRow {

    /** TRNID0n — TRAN-ID. */
    private String transactionId;

    /** TDATE0n — WS-TRAN-DATE, MM/DD/YY derived from TRAN-ORIG-TS. */
    private String date;

    /** TDESC0n — TRAN-DESC. */
    private String description;

    /** TAMT00n — WS-TRAN-AMT PIC +99999999.99. */
    private String amount;
}
