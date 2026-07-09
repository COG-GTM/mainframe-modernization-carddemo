package com.carddemo.web.billpay.dto;

import java.math.BigDecimal;

/**
 * Bill-payment response for {@code POST /api/billpay}.
 *
 * <p>Carries the outcome the {@code COBIL00C} screen would have shown: the account's balance
 * ({@code CURBALI}), the {@code ERRMSGO} message, and — when a payment was made — the new
 * balance and the created transaction id.</p>
 *
 * @param accountId             the account the request targeted
 * @param message               the {@code ERRMSGO} message (success, prompt or error)
 * @param currentBalance        ACCT-CURR-BAL before the payment (null when not read)
 * @param newBalance            ACCT-CURR-BAL after the payment (null unless {@code paid})
 * @param transactionId         TRAN-ID of the created payment transaction (null unless
 *                              {@code paid})
 * @param paid                  whether a payment transaction was written and the balance
 *                              updated ({@code CONF-PAY-YES} happy path)
 * @param confirmationRequired  whether the caller must resubmit with {@code confirm=Y}
 *                              ({@code CONF-PAY-NO} — "Confirm to make a bill payment...")
 */
public record BillPayResponse(
        String accountId,
        String message,
        BigDecimal currentBalance,
        BigDecimal newBalance,
        String transactionId,
        boolean paid,
        boolean confirmationRequired) {

    /** Balance shown, awaiting a {@code Y}/{@code N} confirmation. */
    public static BillPayResponse confirmationRequired(String accountId, BigDecimal balance,
            String message) {
        return new BillPayResponse(accountId, message, balance, null, null, false, true);
    }

    /** Payment declined ({@code CONFIRMI = 'N'}) — the COBOL clear-screen branch. */
    public static BillPayResponse declined(String accountId, String message) {
        return new BillPayResponse(accountId, message, null, null, null, false, false);
    }

    /** Payment made: balance drawn down to zero and a transaction written. */
    public static BillPayResponse paid(String accountId, BigDecimal previousBalance,
            BigDecimal newBalance, String transactionId, String message) {
        return new BillPayResponse(accountId, message, previousBalance, newBalance,
                transactionId, true, false);
    }
}
