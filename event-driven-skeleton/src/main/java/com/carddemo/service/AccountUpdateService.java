package com.carddemo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.event.TransactionPendingEvent;
import com.carddemo.model.Account;
import com.carddemo.model.TranCatBalance;
import com.carddemo.model.TranCatBalanceId;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRepository;

/**
 * Service for updating account balances and persisting transactions.
 *
 * Replaces: CBTRN02C paragraphs 2700-UPDATE-TCATBAL, 2800-UPDATE-ACCOUNT-REC,
 * and 2900-WRITE-TRANSACTION-FILE (lines 467-579).
 *
 * All three updates execute within a single @Transactional boundary,
 * preserving the atomicity that COBOL achieved through VSAM file I/O
 * within a single program execution unit.
 */
@Service
public class AccountUpdateService {

    private static final Logger log = LoggerFactory.getLogger(AccountUpdateService.class);
    private static final DateTimeFormatter DB2_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountUpdateService(TranCatBalanceRepository tranCatBalanceRepository,
                                AccountRepository accountRepository,
                                TransactionRepository transactionRepository) {
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Update transaction category balance.
     *
     * Replaces: 2700-UPDATE-TCATBAL (lines 467-501)
     *   - Looks up by composite key (acctId, typeCd, catCd)
     *   - If not found → creates new record (2700-A-CREATE-TCATBAL-REC)
     *   - If found → adds amount to existing balance (2700-B-UPDATE-TCATBAL-REC)
     */
    @Transactional
    public void updateTransactionCategoryBalance(long accountId, String typeCd,
                                                  int catCd, BigDecimal amount) {
        TranCatBalanceId key = new TranCatBalanceId(accountId, typeCd, catCd);
        TranCatBalance catBal = tranCatBalanceRepository.findById(key).orElse(null);

        if (catBal == null) {
            // Replaces: 2700-A-CREATE-TCATBAL-REC — WRITE FD-TRAN-CAT-BAL-RECORD
            catBal = new TranCatBalance(accountId, typeCd, catCd, amount);
            tranCatBalanceRepository.save(catBal);
            log.debug("Created new category balance: acct={}, type={}, cat={}, amount={}",
                    accountId, typeCd, catCd, amount);
        } else {
            // Replaces: 2700-B-UPDATE-TCATBAL-REC — ADD DALYTRAN-AMT TO TRAN-CAT-BAL
            //           REWRITE FD-TRAN-CAT-BAL-RECORD
            BigDecimal currentBalance = catBal.getBalance() != null
                    ? catBal.getBalance() : BigDecimal.ZERO;
            catBal.setBalance(currentBalance.add(amount));
            tranCatBalanceRepository.save(catBal);
            log.debug("Updated category balance: acct={}, type={}, cat={}, newBalance={}",
                    accountId, typeCd, catCd, catBal.getBalance());
        }
    }

    /**
     * Update account balance and cycle accumulators.
     *
     * Replaces: 2800-UPDATE-ACCOUNT-REC (lines 545-560)
     *   - ADD DALYTRAN-AMT TO ACCT-CURR-BAL
     *   - IF DALYTRAN-AMT >= 0 → ADD TO ACCT-CURR-CYC-CREDIT
     *   - ELSE → ADD TO ACCT-CURR-CYC-DEBIT
     *   - REWRITE FD-ACCTFILE-REC FROM ACCOUNT-RECORD
     */
    @Transactional
    public void updateAccountBalance(Account account, BigDecimal amount) {
        // ADD DALYTRAN-AMT TO ACCT-CURR-BAL
        BigDecimal currentBalance = account.getCurrentBalance() != null
                ? account.getCurrentBalance() : BigDecimal.ZERO;
        account.setCurrentBalance(currentBalance.add(amount));

        // Update cycle accumulators based on sign of amount
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal cycleCredit = account.getCurrentCycleCredit() != null
                    ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
            account.setCurrentCycleCredit(cycleCredit.add(amount));
        } else {
            BigDecimal cycleDebit = account.getCurrentCycleDebit() != null
                    ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
            account.setCurrentCycleDebit(cycleDebit.add(amount));
        }

        accountRepository.save(account);
        log.debug("Updated account {}: balance={}, cycleCredit={}, cycleDebit={}",
                account.getAcctId(), account.getCurrentBalance(),
                account.getCurrentCycleCredit(), account.getCurrentCycleDebit());
    }

    /**
     * Persist a posted transaction to the transaction table.
     *
     * Replaces: 2900-WRITE-TRANSACTION-FILE (lines 562-579)
     *   - Maps DALYTRAN fields → TRAN-RECORD fields
     *   - Generates DB2-format processing timestamp (Z-GET-DB2-FORMAT-TIMESTAMP)
     *   - WRITE FD-TRANFILE-REC FROM TRAN-RECORD
     */
    @Transactional
    public Transaction writeTransaction(TransactionPendingEvent event) {
        // Replaces: Z-GET-DB2-FORMAT-TIMESTAMP — generates DB2-format timestamp
        String processTimestamp = LocalDateTime.now().format(DB2_TS_FORMAT);

        Transaction transaction = new Transaction(
                event.transactionId(),
                event.typeCd(),
                event.catCd(),
                event.source(),
                event.description(),
                event.amount(),
                event.merchantId(),
                event.merchantName(),
                event.merchantCity(),
                event.merchantZip(),
                event.cardNumber(),
                event.originTimestamp(),
                processTimestamp
        );

        transactionRepository.save(transaction);
        log.info("Persisted transaction: id={}, card={}, amount={}",
                event.transactionId(), event.cardNumber(), event.amount());

        return transaction;
    }
}
