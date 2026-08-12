package com.carddemo.online.billpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COBIL00C — input fields of BMS map COBIL0A (mapset COBIL00).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentRequest {

    /** ACTIDIN PIC X(11) — the account whose full balance is paid. */
    private String accountId;

    /** CONFIRM PIC X(01): 'Y' commits the payment, 'N' clears the screen, blank re-prompts. */
    private String confirm;
}
