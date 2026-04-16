package com.cardemo.batch.config;

import com.cardemo.batch.model.TransactionPostingResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.RejectedTransactionRepository;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * Composite ItemWriter that persists both posted and rejected transactions.
 * Handles writes to TRANFILE, DALYREJS, TCATBALF, and ACCTFILE.
 */
public class TransactionPostingWriter implements ItemWriter<TransactionPostingResult> {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingWriter.class);

    private final TransactionRepository transactionRepository;
    private final RejectedTransactionRepository rejectedTransactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository categoryBalanceRepository;

    public TransactionPostingWriter(TransactionRepository transactionRepository,
                                     RejectedTransactionRepository rejectedTransactionRepository,
                                     AccountRepository accountRepository,
                                     TransactionCategoryBalanceRepository categoryBalanceRepository) {
        this.transactionRepository = transactionRepository;
        this.rejectedTransactionRepository = rejectedTransactionRepository;
        this.accountRepository = accountRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
    }

    @Override
    public void write(Chunk<? extends TransactionPostingResult> chunk) {
        for (TransactionPostingResult result : chunk) {
            if (result.isPosted()) {
                // 2900-WRITE-TRANSACTION-FILE
                transactionRepository.save(result.getPostedTransaction());
                // 2800-UPDATE-ACCOUNT-REC (REWRITE)
                accountRepository.save(result.getUpdatedAccount());
                // 2700-UPDATE-TCATBAL (WRITE or REWRITE)
                categoryBalanceRepository.save(result.getUpdatedCategoryBalance());
                log.debug("Persisted posted transaction: {}", result.getPostedTransaction().getId());
            } else {
                // 2500-WRITE-REJECT-REC
                rejectedTransactionRepository.save(result.getRejectedTransaction());
                log.debug("Persisted rejected transaction: {}",
                        result.getRejectedTransaction().getTransactionId());
            }
        }
    }
}
