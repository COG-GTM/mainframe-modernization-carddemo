package com.carddemo.batch.account;

import com.carddemo.domain.Account;
import com.carddemo.domain.Transaction;
import java.util.List;

/**
 * Output of {@link AccountInterestProcessor} for one account: the account with its balance
 * updated ({@code 1050-UPDATE-ACCOUNT}) and the interest transactions to persist
 * ({@code 1300-B-WRITE-TX}). The writer saves both.
 *
 * @param account            the account after {@code ACCT-CURR-BAL += WS-TOTAL-INT} and the
 *                           cycle credit/debit reset to zero
 * @param interestTransactions the interest {@link Transaction} records generated for the account
 *                           (one per category whose disclosure-group rate was non-zero)
 */
public record AccountInterestResult(Account account, List<Transaction> interestTransactions) {
}
