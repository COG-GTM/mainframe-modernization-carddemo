package com.carddemo.batch.service;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.CardXref;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.model.TransactionCategoryBalance;
import com.carddemo.batch.model.TransactionCategoryBalanceId;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import com.carddemo.batch.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Posts validated transactions: updates TCATBAL, Account, and writes Transaction.
 * Implements the business logic from COBOL paragraphs 2000-POST-TRANSACTION,
 * 2700-UPDATE-TCATBAL, 2800-UPDATE-ACCOUNT-REC, and 2900-WRITE-TRANSACTION-FILE
 * in CBTRN02C.cbl (lines 424-579).
 */
@Service
public class TransactionPostingService {

    private final TransactionCategoryBalanceRepository tcatbalRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionPostingService(TransactionCategoryBalanceRepository tcatbalRepository,
                                     AccountRepository accountRepository,
                                     TransactionRepository transactionRepository) {
        this.tcatbalRepository = tcatbalRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Posts a validated daily transaction.
     * Performs three operations atomically:
     * 1. Upsert TCATBAL record
     * 2. Update Account balances
     * 3. Write Transaction record
     */
    @Transactional
    public void post(DailyTransaction txn, CardXref xref, Account account) {
        upsertTcatbal(txn, xref);
        updateAccount(txn, account);
        writeTransaction(txn);
    }

    /**
     * Upserts the transaction category balance record.
     * COBOL: 2700-UPDATE-TCATBAL (lines 467-501)
     * If record exists, add amount to balance. If not, create with amount as balance.
     */
    private void upsertTcatbal(DailyTransaction txn, CardXref xref) {
        TransactionCategoryBalanceId key = new TransactionCategoryBalanceId(
                xref.getAccountId(), txn.getTypeCode(), txn.getCategoryCode());

        Optional<TransactionCategoryBalance> existing = tcatbalRepository.findById(key);

        if (existing.isPresent()) {
            TransactionCategoryBalance tcatbal = existing.get();
            tcatbal.setBalance(tcatbal.getBalance().add(txn.getAmount()));
            tcatbalRepository.save(tcatbal);
        } else {
            TransactionCategoryBalance tcatbal = new TransactionCategoryBalance();
            tcatbal.setId(key);
            tcatbal.setBalance(txn.getAmount());
            tcatbalRepository.save(tcatbal);
        }
    }

    /**
     * Updates the account balances to reflect the posted transaction.
     * COBOL: 2800-UPDATE-ACCOUNT-REC (lines 545-560)
     * - currentBalance += amount
     * - if amount >= 0: currentCycleCredit += amount
     * - if amount < 0: currentCycleDebit += amount
     */
    private void updateAccount(DailyTransaction txn, Account account) {
        account.setCurrentBalance(account.getCurrentBalance().add(txn.getAmount()));

        if (txn.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
            account.setCurrentCycleCredit(account.getCurrentCycleCredit().add(txn.getAmount()));
        } else {
            account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(txn.getAmount()));
        }

        accountRepository.save(account);
    }

    /**
     * Maps DailyTransaction to Transaction entity and saves it.
     * COBOL: 2000-POST-TRANSACTION + 2900-WRITE-TRANSACTION-FILE (lines 424-579)
     * Sets processedTimestamp to current time (matching Z-GET-DB2-FORMAT-TIMESTAMP).
     */
    private void writeTransaction(DailyTransaction txn) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(txn.getTransactionId());
        transaction.setTypeCode(txn.getTypeCode());
        transaction.setCategoryCode(txn.getCategoryCode());
        transaction.setSource(txn.getSource());
        transaction.setDescription(txn.getDescription());
        transaction.setAmount(txn.getAmount());
        transaction.setMerchantId(txn.getMerchantId());
        transaction.setMerchantName(txn.getMerchantName());
        transaction.setMerchantCity(txn.getMerchantCity());
        transaction.setMerchantZip(txn.getMerchantZip());
        transaction.setCardNumber(txn.getCardNumber());
        transaction.setOriginTimestamp(txn.getOriginTimestamp());
        transaction.setProcessedTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
}
