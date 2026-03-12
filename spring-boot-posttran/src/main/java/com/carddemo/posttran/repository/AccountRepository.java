package com.carddemo.posttran.repository;

import com.carddemo.posttran.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Repository for the accounts table (maps to ACCTFILE / CVACT01Y).
 * Provides account lookups and balance updates used during transaction posting.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Update account balances after posting a transaction.
     * Maps to COBOL section 2800-UPDATE-ACCOUNT-REC:
     * <pre>
     *   ADD DALYTRAN-AMT TO ACCT-CURR-BAL
     *   IF DALYTRAN-AMT >= 0
     *      ADD DALYTRAN-AMT TO ACCT-CURR-CYC-CREDIT
     *   ELSE
     *      ADD DALYTRAN-AMT TO ACCT-CURR-CYC-DEBIT
     *   END-IF
     * </pre>
     *
     * For positive amounts (credits): updates curr_bal and curr_cyc_credit.
     * For negative amounts (debits): updates curr_bal and curr_cyc_debit.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Account a SET " +
           "a.acctCurrBal = COALESCE(a.acctCurrBal, 0) + :amount, " +
           "a.acctCurrCycCredit = COALESCE(a.acctCurrCycCredit, 0) + CASE WHEN :amount >= 0 THEN :amount ELSE CAST(0 AS java.math.BigDecimal) END, " +
           "a.acctCurrCycDebit = COALESCE(a.acctCurrCycDebit, 0) + CASE WHEN :amount < 0 THEN :amount ELSE CAST(0 AS java.math.BigDecimal) END " +
           "WHERE a.acctId = :acctId")
    int updateBalance(@Param("acctId") long acctId, @Param("amount") BigDecimal amount);
}
