/**
 * CS-11 batch transaction posting (POSTTRAN) — the Java port of the COBOL batch programs
 * {@code CBTRN01C} (read/validate daily transactions), {@code CBTRN02C} (post transactions,
 * update account and category balances, reject failures) and {@code CBTRN03C} (transaction
 * detail report).
 *
 * <p>All CS-11 code is confined to this package to avoid conflicts with sibling migration
 * waves; the only shared additions are the Flyway migration {@code V1110__cs11_transaction_reject.sql}
 * and this job's beans. See {@code java/docs/mapping/CS-11-batch-posting.md}.</p>
 */
package com.carddemo.batch.posting;
