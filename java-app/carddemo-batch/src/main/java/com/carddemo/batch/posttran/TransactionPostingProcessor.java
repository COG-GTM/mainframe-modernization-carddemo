package com.carddemo.batch.posttran;

import com.carddemo.entity.*;
import com.carddemo.entity.TransactionCategoryBalance.TransactionCategoryBalanceId;
import com.carddemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Transaction posting processor — translates EXACTLY the validation and posting logic from CBTRN02C.cbl.
 * 
 * Validation sequence:
 * 1. Look up card in CardXref — reject code 100 if not found
 * 2. Look up account — reject code 101 if not found
 * 3. Credit limit check: currentCycleCredit - currentCycleDebit + amount <= creditLimit — reject code 102
 * 4. Expiry check: account expiration >= transaction date — reject code 103
 * 5. If valid: write to Transaction, update TransactionCategoryBalance, update Account balances
 */
public class TransactionPostingProcessor implements ItemProcessor<DailyTransaction, TransactionPostingResult> {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingProcessor.class);

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryBalanceRepository tcatBalRepository;

    private int processedCount = 0;
    private int rejectCount = 0;

    public TransactionPostingProcessor(CardXrefRepository cardXrefRepository,
                                        AccountRepository accountRepository,
                                        TransactionRepository transactionRepository,
                                        TransactionCategoryBalanceRepository tcatBalRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.tcatBalRepository = tcatBalRepository;
    }

    @Override
    public TransactionPostingResult process(DailyTransaction dailyTran) throws Exception {
        processedCount++;

        // Step 1: Look up card in CardXref
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(dailyTran.getCardNum());
        if (xrefOpt.isEmpty()) {
            rejectCount++;
            return createReject(dailyTran, 100, "Card number not found in cross-reference");
        }

        CardXref xref = xrefOpt.get();

        // Step 2: Look up account
        Optional<Account> acctOpt = accountRepository.findById(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            rejectCount++;
            return createReject(dailyTran, 101, "Account not found for card");
        }

        Account account = acctOpt.get();

        // Step 3: Credit limit check
        BigDecimal cycleCredit = account.getCurrentCycleCredit() != null ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
        BigDecimal cycleDebit = account.getCurrentCycleDebit() != null ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
        BigDecimal creditLimit = account.getCreditLimit() != null ? account.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal tranAmt = dailyTran.getAmount() != null ? dailyTran.getAmount() : BigDecimal.ZERO;

        BigDecimal projectedBalance = cycleCredit.subtract(cycleDebit).add(tranAmt);
        if (projectedBalance.compareTo(creditLimit) > 0) {
            rejectCount++;
            return createReject(dailyTran, 102, "Transaction would exceed credit limit");
        }

        // Step 4: Expiry check
        if (account.getExpirationDate() != null && dailyTran.getOrigTimestamp() != null) {
            if (account.getExpirationDate().isBefore(dailyTran.getOrigTimestamp().toLocalDate())) {
                rejectCount++;
                return createReject(dailyTran, 103, "Account has expired");
            }
        }

        // Step 5: Valid — create posted transaction
        Transaction posted = new Transaction();
        posted.setTransactionId(dailyTran.getTransactionId());
        posted.setCardNum(dailyTran.getCardNum());
        posted.setTypeCd(dailyTran.getTypeCd());
        posted.setCategoryCd(dailyTran.getCategoryCd());
        posted.setSource(dailyTran.getSource());
        posted.setDescription(dailyTran.getDescription());
        posted.setAmount(dailyTran.getAmount());
        posted.setMerchantId(dailyTran.getMerchantId());
        posted.setMerchantName(dailyTran.getMerchantName());
        posted.setMerchantCity(dailyTran.getMerchantCity());
        posted.setMerchantZip(dailyTran.getMerchantZip());
        posted.setOrigTimestamp(dailyTran.getOrigTimestamp());
        posted.setProcTimestamp(LocalDateTime.now());

        // Update TransactionCategoryBalance
        TransactionCategoryBalanceId tcbId = new TransactionCategoryBalanceId(
                xref.getAcctId(),
                dailyTran.getTypeCd(),
                dailyTran.getCategoryCd() != null ? dailyTran.getCategoryCd() : 0
        );
        TransactionCategoryBalance tcb = tcatBalRepository.findById(tcbId).orElseGet(() -> {
            TransactionCategoryBalance newTcb = new TransactionCategoryBalance();
            newTcb.setId(tcbId);
            newTcb.setBalance(BigDecimal.ZERO);
            return newTcb;
        });
        tcb.setBalance(tcb.getBalance().add(tranAmt));
        tcatBalRepository.save(tcb);

        // Update Account balances — credits add to currentCycleCredit, debits add to currentCycleDebit
        String typeCd = dailyTran.getTypeCd() != null ? dailyTran.getTypeCd().trim() : "";
        if ("01".equals(typeCd) || "04".equals(typeCd) || "05".equals(typeCd)) {
            // Purchase/Authorization/Refund → debit
            account.setCurrentCycleDebit(cycleDebit.add(tranAmt));
        } else if ("02".equals(typeCd) || "03".equals(typeCd)) {
            // Payment/Credit → credit
            account.setCurrentCycleCredit(cycleCredit.add(tranAmt));
        }
        account.setCurrentBalance(account.getCurrentBalance().add(tranAmt));
        accountRepository.save(account);

        return new TransactionPostingResult(posted, null, true);
    }

    private TransactionPostingResult createReject(DailyTransaction dailyTran, int code, String reason) {
        DailyTransactionReject reject = new DailyTransactionReject();
        reject.setTransactionId(dailyTran.getTransactionId());
        reject.setCardNum(dailyTran.getCardNum());
        reject.setAmount(dailyTran.getAmount());
        reject.setRejectReasonCode(code);
        reject.setRejectReasonDescription(reason);
        reject.setRejectTimestamp(LocalDateTime.now());
        return new TransactionPostingResult(null, reject, false);
    }

    public int getProcessedCount() { return processedCount; }
    public int getRejectCount() { return rejectCount; }
}
