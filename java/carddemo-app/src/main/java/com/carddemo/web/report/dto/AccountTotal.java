package com.carddemo.web.report.dto;

import java.math.BigDecimal;

/**
 * A per-account subtotal — the JSON equivalent of the {@code REPORT-ACCOUNT-TOTALS} line
 * batch program {@code CBTRN03C} writes when the card number changes ({@code 1120-WRITE-
 * ACCOUNT-TOTALS}). Because {@code CBTRN03C} sorts the input by {@code TRAN-CARD-NUM}, each
 * contiguous card group is one account.
 *
 * @param accountId the account (resolved from the card via the xref), null if unresolved
 * @param cardNum   the {@code TRAN-CARD-NUM} the group was keyed on
 * @param total     sum of {@code TRAN-AMT} for the group (scale 2)
 */
public record AccountTotal(String accountId, String cardNum, BigDecimal total) {
}
