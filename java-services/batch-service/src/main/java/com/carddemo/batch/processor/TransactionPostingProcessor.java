package com.carddemo.batch.processor;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.CardCrossReference;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.model.TransactionCategoryBalance;
import com.carddemo.batch.model.TransactionCategoryBalanceId;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.CardRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Transaction posting processor migrated from COBOL programs CBTRN01C + CBTRN02C.
 *
 * Business logic:
 * 1. Validate card number exists via CardCrossReference (CBTRN02C 1500-A-LOOKUP-XREF)
 * 2. Look up account via cross-reference (CBTRN02C 1500-B-LOOKUP-ACCT)
 * 3. Validate credit limit not exceeded
 * 4. Update account balance — add for credits, subtract for debits (CBTRN02C 2800-UPDATE-ACCOUNT-REC)
 * 5. Update transaction category balance totals (CBTRN02C 2700-UPDATE-TCATBAL)
 * 6. Create transaction record with processed timestamp (CBTRN02C 2000-POST-TRANSACTION)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionPostingProcessor implements ItemProcessor<DailyTransaction, Transaction> {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSS000");

    private final CardRepository cardRepository;
    private final CardCrossReferenceRepository xrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tcatBalRepository;

    @Override
    public Transaction process(DailyTransaction dailyTran) throws Exception {
        log.info("Processing daily transaction: {}", dailyTran.getTranId());

        // Step 1: Validate card number exists (mirrors 1500-A-LOOKUP-XREF)
        String cardNum = dailyTran.getCardNum();
        if (cardNum == null || cardRepository.findByCardNum(cardNum).isEmpty()) {
            log.warn("Invalid card number: {}. Skipping transaction {}", cardNum, dailyTran.getTranId());
            return null;
        }

        // Step 2: Look up account via cross-reference (mirrors 1500-B-LOOKUP-ACCT)
        Optional<CardCrossReference> xrefOpt = xrefRepository.findByCardNum(cardNum);
        if (xrefOpt.isEmpty()) {
            log.warn("Card cross-reference not found for card: {}. Skipping transaction {}",
                    cardNum, dailyTran.getTranId());
            return null;
        }

        CardCrossReference xref = xrefOpt.get();
        Optional<Account> accountOpt = accountRepository.findById(xref.getAcctId());
        if (accountOpt.isEmpty()) {
            log.warn("Account {} not found. Skipping transaction {}", xref.getAcctId(), dailyTran.getTranId());
            return null;
        }

        Account account = accountOpt.get();

        // Step 3: Validate credit limit (mirrors CBTRN02C 1500-B-LOOKUP-ACCT validation)
        BigDecimal tempBal = account.getCurrentCycleCredit()
                .subtract(account.getCurrentCycleDebit())
                .add(dailyTran.getAmount());
        if (account.getCreditLimit().compareTo(tempBal) < 0) {
            log.warn("Overlimit transaction for account {}. Skipping transaction {}",
                    account.getAcctId(), dailyTran.getTranId());
            return null;
        }

        // Step 4: Update account balance (mirrors 2800-UPDATE-ACCOUNT-REC)
        updateAccountBalance(account, dailyTran.getAmount());

        // Step 5: Update transaction category balance (mirrors 2700-UPDATE-TCATBAL)
        updateTransactionCategoryBalance(xref.getAcctId(), dailyTran.getTypeCd(),
                dailyTran.getCatCd(), dailyTran.getAmount());

        // Step 6: Build transaction record (mirrors 2000-POST-TRANSACTION)
        String processedTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        return Transaction.builder()
                .tranId(dailyTran.getTranId())
                .typeCd(dailyTran.getTypeCd())
                .catCd(dailyTran.getCatCd())
                .source(dailyTran.getSource())
                .description(dailyTran.getDescription())
                .amount(dailyTran.getAmount())
                .merchantId(dailyTran.getMerchantId())
                .merchantName(dailyTran.getMerchantName())
                .merchantCity(dailyTran.getMerchantCity())
                .merchantZip(dailyTran.getMerchantZip())
                .cardNum(dailyTran.getCardNum())
                .origTimestamp(dailyTran.getOrigTimestamp())
                .procTimestamp(processedTimestamp)
                .build();
    }

    /**
     * Update account balance to reflect posted transaction.
     * Mirrors CBTRN02C paragraph 2800-UPDATE-ACCOUNT-REC:
     *   ADD DALYTRAN-AMT TO ACCT-CURR-BAL
     *   IF DALYTRAN-AMT >= 0: ADD to ACCT-CURR-CYC-CREDIT
     *   ELSE: ADD to ACCT-CURR-CYC-DEBIT
     */
    void updateAccountBalance(Account account, BigDecimal amount) {
        account.setCurrentBalance(account.getCurrentBalance().add(amount));

        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            account.setCurrentCycleCredit(account.getCurrentCycleCredit().add(amount));
        } else {
            account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(amount));
        }

        accountRepository.save(account);
    }

    /**
     * Update transaction category balance.
     * Mirrors CBTRN02C paragraphs 2700-UPDATE-TCATBAL / 2700-A-CREATE / 2700-B-UPDATE:
     *   If record exists: ADD DALYTRAN-AMT TO TRAN-CAT-BAL, REWRITE
     *   If not: create new record with DALYTRAN-AMT as initial balance
     */
    void updateTransactionCategoryBalance(Long acctId, String typeCd, Integer catCd, BigDecimal amount) {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, typeCd, catCd);
        Optional<TransactionCategoryBalance> existing = tcatBalRepository.findById(id);

        if (existing.isPresent()) {
            TransactionCategoryBalance tcatBal = existing.get();
            tcatBal.setBalance(tcatBal.getBalance().add(amount));
            tcatBalRepository.save(tcatBal);
        } else {
            TransactionCategoryBalance newTcatBal = TransactionCategoryBalance.builder()
                    .acctId(acctId)
                    .typeCd(typeCd)
                    .catCd(catCd)
                    .balance(amount)
                    .build();
            tcatBalRepository.save(newTcatBal);
        }
    }
}
