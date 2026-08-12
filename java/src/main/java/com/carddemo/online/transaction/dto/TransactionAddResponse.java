package com.carddemo.online.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COTRN02C — outcome of the CT02 screen (BMS map COTRN2A).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAddResponse {

    /** ERR-FLG-OFF and the record was written. */
    private boolean success;

    /** ERRMSG PIC X(78) — the error text, or the green "added successfully" confirmation. */
    private String message;

    /** TRAN-ID of the record written by ADD-TRANSACTION. */
    private String transactionId;

    /** ACTIDIN after VALIDATE-INPUT-KEY-FIELDS resolved it through the cross reference. */
    private String accountId;

    /** CARDNIN after VALIDATE-INPUT-KEY-FIELDS resolved it through the cross reference. */
    private String cardNumber;
}
