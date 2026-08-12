package com.carddemo.online.billpay;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COBIL00C — output fields of BMS map COBIL0A (mapset COBIL00).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentResponse {

    /** ERR-FLG-OFF and the payment transaction was written. */
    private boolean success;

    /** ERRMSG PIC X(78) — the error text, the confirm prompt or the green success message. */
    private String message;

    /** CURBAL — WS-CURR-BAL PIC +9999999999.99 as shown on the screen. */
    private String currentBalanceDisplay;

    /** ACCT-CURR-BAL after UPDATE-ACCTDAT-FILE. */
    private BigDecimal currentBalance;

    /** TRAN-ID of the bill payment transaction written to TRANSACT. */
    private String transactionId;

    /** TRAN-AMT of the bill payment transaction (the balance that was paid). */
    private BigDecimal paidAmount;
}
