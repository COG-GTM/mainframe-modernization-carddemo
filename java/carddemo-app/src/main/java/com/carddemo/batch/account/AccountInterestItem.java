package com.carddemo.batch.account;

import com.carddemo.domain.TransactionCategoryBalance;
import java.util.List;

/**
 * One unit of work for {@code intcalcJob}: a single account together with every
 * transaction-category balance ({@code TCATBAL}) that belongs to it.
 *
 * <p>{@code CBACT04C} reads {@code TCATBAL} sequentially (it is keyed on
 * {@code acct-id + type-cd + cat-cd}, so all rows for one account are contiguous) and
 * accumulates interest until the account id changes, at which point it updates the account.
 * The reader reproduces that grouping and emits one {@code AccountInterestItem} per account so
 * the account-level aggregation fits the reader → processor → writer chunk model.</p>
 *
 * @param acctId   {@code TRANCAT-ACCT-ID} — the account these balances belong to
 * @param balances the account's {@code TCATBAL} rows, in COBOL key order
 */
public record AccountInterestItem(String acctId, List<TransactionCategoryBalance> balances) {
}
