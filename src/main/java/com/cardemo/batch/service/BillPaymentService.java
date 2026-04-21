package com.cardemo.batch.service;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.Transaction;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Bill payment service implementing atomic balance update + transaction write.
 * Ported from COBOL paragraph 2000-POST-TRANSACTION and 2800-UPDATE-ACCOUNT-REC.
 * Account balance update and transaction record write must be atomic.
 */
@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentService(AccountRepository accountRepository,
                               TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Process a bill payment atomically.
     * Updates account balance and writes transaction record in a single transaction.
     * If either fails, both are rolled back.
     *
     * @param acctId the account ID
     * @param amount the payment amount (negative for payment)
     * @param transaction the transaction record to write
     * @throws IllegalStateException if account not found
     */
    @Transactional
    public void processBillPayment(long acctId, BigDecimal amount, Transaction transaction) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalStateException("Account not found: " + acctId));

        // Update account balance
        BigDecimal currentBal = account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
        account.setCurrentBalance(currentBal.add(amount));

        // Credit/debit separation per COBOL paragraph 2800
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal cycCredit = account.getCurrentCycleCredit() != null ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
            account.setCurrentCycleCredit(cycCredit.add(amount));
        } else {
            BigDecimal cycDebit = account.getCurrentCycleDebit() != null ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
            account.setCurrentCycleDebit(cycDebit.add(amount));
        }

        // Both operations must succeed or fail together
        accountRepository.save(account);
        transactionRepository.save(transaction);
    }
}
