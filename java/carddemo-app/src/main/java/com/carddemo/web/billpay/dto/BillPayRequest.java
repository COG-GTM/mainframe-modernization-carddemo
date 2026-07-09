package com.carddemo.web.billpay.dto;

/**
 * Bill-payment request payload for {@code POST /api/billpay}.
 *
 * <p>Mirrors the two input fields of the {@code COBIL0A} map: {@code ACTIDINI} (the account
 * whose balance is being paid) and {@code CONFIRMI} (the {@code (Y/N)} confirmation).</p>
 *
 * @param accountId ACTIDINI — ACCT-ID PIC 9(11) (11-char id, never used in arithmetic)
 * @param confirm   CONFIRMI PIC X(01) — {@code Y}/{@code N} (blank/{@code null} = not yet
 *                  confirmed)
 */
public record BillPayRequest(String accountId, String confirm) {
}
