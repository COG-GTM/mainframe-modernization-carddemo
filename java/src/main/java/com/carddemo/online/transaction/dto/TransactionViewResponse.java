package com.carddemo.online.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COTRN01C — output fields of BMS map COTRN1A (mapset COTRN01), populated from
 * copybook CVTRA05Y (TRAN-RECORD on the TRANSACT file).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionViewResponse {

    /** ERR-FLG-OFF. */
    private boolean success;

    /** ERRMSG PIC X(78). */
    private String errorMessage;

    /** TRNID — TRAN-ID. */
    private String transactionId;

    /** CARDNUM — TRAN-CARD-NUM. */
    private String cardNumber;

    /** TTYPCD — TRAN-TYPE-CD. */
    private String typeCode;

    /** TCATCD — TRAN-CAT-CD. */
    private Integer categoryCode;

    /** TRNSRC — TRAN-SOURCE. */
    private String source;

    /** TRNAMT — WS-TRAN-AMT PIC +99999999.99. */
    private String amount;

    /** TDESC — TRAN-DESC. */
    private String description;

    /** TORIGDT — TRAN-ORIG-TS. */
    private String originTimestamp;

    /** TPROCDT — TRAN-PROC-TS. */
    private String processTimestamp;

    /** MID — TRAN-MERCHANT-ID. */
    private Long merchantId;

    /** MNAME — TRAN-MERCHANT-NAME. */
    private String merchantName;

    /** MCITY — TRAN-MERCHANT-CITY. */
    private String merchantCity;

    /** MZIP — TRAN-MERCHANT-ZIP. */
    private String merchantZip;
}
