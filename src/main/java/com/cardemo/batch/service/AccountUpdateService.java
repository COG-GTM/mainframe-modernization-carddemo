package com.cardemo.batch.service;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.DailyTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implements paragraph 2800-UPDATE-ACCOUNT-REC from CBTRN02C.
 * Updates account balances based on transaction amounts.
 */
@Service
public class AccountUpdateService {

    /**
     * Updates account balances based on the transaction amount.
     *
     * Credit/debit separation:
     * - If DALYTRAN-AMT >= 0: ADD DALYTRAN-AMT TO ACCT-CURR-CYC-CREDIT
     * - Else: ADD DALYTRAN-AMT TO ACCT-CURR-CYC-DEBIT
     * - Always: ADD DALYTRAN-AMT TO ACCT-CURR-BAL
     */
    public void updateAccountBalances(Account account, DailyTransaction dailyTran) {
        BigDecimal amount = dailyTran.getAmount();

        // Always update current balance
        account.setCurrentBalance(account.getCurrentBalance().add(amount));

        // Credit/debit separation
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            account.setCurrentCycleCredit(account.getCurrentCycleCredit().add(amount));
        } else {
            account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(amount));
        }
    }
}
