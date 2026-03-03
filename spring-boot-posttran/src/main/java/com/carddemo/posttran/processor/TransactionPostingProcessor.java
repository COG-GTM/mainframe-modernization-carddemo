package com.carddemo.posttran.processor;

import com.carddemo.posttran.model.DailyTransaction;
import com.carddemo.posttran.model.ProcessedTransaction;
import com.carddemo.posttran.model.Transaction;
import com.carddemo.posttran.model.TransactionCategoryBalance;
import com.carddemo.posttran.model.TransactionCategoryBalanceId;
import com.carddemo.posttran.repository.AccountRepository;
import com.carddemo.posttran.repository.CategoryBalanceRepository;
import com.carddemo.posttran.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Posts valid transactions: writes to transactions table, upserts category balance,
 * and updates the account record.
 * Maps to COBOL section 2000-POST-TRANSACTION (lines 424-444) and sub-sections:
 * <ul>
 *   <li>2700-UPDATE-TCATBAL — upsert transaction category balance</li>
 *   <li>2800-UPDATE-ACCOUNT-REC — update account balances</li>
 *   <li>2900-WRITE-TRANSACTION-FILE — write to transactions table</li>
 * </ul>
 */
public class TransactionPostingProcessor {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingProcessor.class);

    /** DB2-style timestamp format: YYYY-MM-DD-HH.MM.SS.NN0000 */
    private static final DateTimeFormatter DB2_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS'0000'");

    private final TransactionRepository transactionRepository;
    private final CategoryBalanceRepository categoryBalanceRepository;
    private final AccountRepository accountRepository;

    public TransactionPostingProcessor(TransactionRepository transactionRepository,
                                       CategoryBalanceRepository categoryBalanceRepository,
                                       AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Post a validated transaction.
     * This method is called for each valid ProcessedTransaction.
     */
    @Transactional
    public void post(ProcessedTransaction processed) {
        DailyTransaction dt = processed.getDailyTransaction();
        long accountId = processed.getAccountId();

        // Build the Transaction entity from the DailyTransaction fields
        // Maps to COBOL 2000-POST-TRANSACTION MOVE statements (lines 425-438)
        Transaction tx = new Transaction();
        tx.setTranId(dt.getTranId());
        tx.setTranTypeCd(dt.getTranTypeCd());
        tx.setTranCatCd(dt.getTranCatCd());
        tx.setTranSource(dt.getTranSource());
        tx.setTranDesc(dt.getTranDesc());
        tx.setTranAmt(dt.getTranAmt());
        tx.setTranMerchantId(dt.getTranMerchantId());
        tx.setTranMerchantName(dt.getTranMerchantName());
        tx.setTranMerchantCity(dt.getTranMerchantCity());
        tx.setTranMerchantZip(dt.getTranMerchantZip());
        tx.setTranCardNum(dt.getTranCardNum());
        tx.setTranOrigTs(dt.getTranOrigTs());

        // Z-GET-DB2-FORMAT-TIMESTAMP: set processing timestamp to current time
        tx.setTranProcTs(generateDb2Timestamp());

        // 2700-UPDATE-TCATBAL: upsert category balance
        upsertCategoryBalance(accountId, dt.getTranTypeCd(), dt.getTranCatCd(), dt.getTranAmt());

        // 2800-UPDATE-ACCOUNT-REC: update account balances
        int updated = accountRepository.updateBalance(accountId, dt.getTranAmt());
        if (updated == 0) {
            log.error("Failed to update account {} — account not found during posting", accountId);
            throw new IllegalStateException(
                    "Account record not found during posting for acctId=" + accountId);
        }

        // 2900-WRITE-TRANSACTION-FILE: save the transaction
        transactionRepository.save(tx);

        log.debug("Posted transaction {} to account {}", dt.getTranId(), accountId);
    }

    /**
     * Upsert transaction category balance.
     * Maps to COBOL 2700-UPDATE-TCATBAL:
     * - Read by composite key (acctId, typeCd, catCd)
     * - If not found (status '23'): create new record (2700-A-CREATE-TCATBAL-REC)
     * - If found: add amount to existing balance (2700-B-UPDATE-TCATBAL-REC)
     */
    private void upsertCategoryBalance(long acctId, String typeCd, int catCd, BigDecimal amount) {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, typeCd, catCd);
        Optional<TransactionCategoryBalance> existing = categoryBalanceRepository.findById(id);

        if (existing.isPresent()) {
            // 2700-B-UPDATE-TCATBAL-REC: ADD DALYTRAN-AMT TO TRAN-CAT-BAL
            int updated = categoryBalanceRepository.updateBalance(acctId, typeCd, catCd, amount);
            if (updated == 0) {
                log.error("Failed to update category balance for key ({}, {}, {})",
                        acctId, typeCd, catCd);
            }
        } else {
            // 2700-A-CREATE-TCATBAL-REC: INITIALIZE + set key fields + ADD amount
            TransactionCategoryBalance newBal = new TransactionCategoryBalance();
            newBal.setTrancatAcctId(acctId);
            newBal.setTrancatTypeCd(typeCd);
            newBal.setTrancatCd(catCd);
            newBal.setTranCatBal(amount);
            categoryBalanceRepository.save(newBal);
            log.debug("Created new category balance record for ({}, {}, {})", acctId, typeCd, catCd);
        }
    }

    /**
     * Generate a DB2-format timestamp matching COBOL Z-GET-DB2-FORMAT-TIMESTAMP.
     * Format: YYYY-MM-DD-HH.MM.SS.NN0000
     */
    private String generateDb2Timestamp() {
        return LocalDateTime.now().format(DB2_TS_FORMAT);
    }
}
