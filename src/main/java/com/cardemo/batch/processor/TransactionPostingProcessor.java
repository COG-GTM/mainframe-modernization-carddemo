package com.cardemo.batch.processor;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.RejectedTransaction;
import com.cardemo.batch.model.Transaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionPostingResult;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.service.AccountUpdateService;
import com.cardemo.batch.service.TimestampService;
import com.cardemo.batch.service.TransactionCategoryBalanceService;
import com.cardemo.batch.service.TransactionValidationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Optional;

/**
 * Spring Batch ItemProcessor that validates and processes daily transactions.
 * Corresponds to the main processing loop in CBTRN02C (paragraphs 1500 through 2900).
 */
public class TransactionPostingProcessor
        implements ItemProcessor<DailyTransaction, TransactionPostingResult> {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingProcessor.class);

    private final TransactionValidationService validationService;
    private final AccountUpdateService accountUpdateService;
    private final TransactionCategoryBalanceService categoryBalanceService;
    private final TimestampService timestampService;
    private final Counter transactionCounter;
    private final Counter rejectCounter;

    public TransactionPostingProcessor(TransactionValidationService validationService,
                                        AccountUpdateService accountUpdateService,
                                        TransactionCategoryBalanceService categoryBalanceService,
                                        TimestampService timestampService,
                                        MeterRegistry meterRegistry) {
        this.validationService = validationService;
        this.accountUpdateService = accountUpdateService;
        this.categoryBalanceService = categoryBalanceService;
        this.timestampService = timestampService;
        this.transactionCounter = Counter.builder("cardemo.batch.transactions.total")
                .description("Total transactions processed (WS-TRANSACTION-COUNT)")
                .register(meterRegistry);
        this.rejectCounter = Counter.builder("cardemo.batch.transactions.rejected")
                .description("Rejected transactions (WS-REJECT-COUNT)")
                .register(meterRegistry);
    }

    @Override
    public TransactionPostingResult process(DailyTransaction dailyTran) {
        transactionCounter.increment();

        // 1500-VALIDATE-TRAN
        ValidationResult validation = validationService.validate(dailyTran);

        if (!validation.isValid()) {
            // Rejected: build dead-letter record (2500-WRITE-REJECT-REC)
            rejectCounter.increment();
            RejectedTransaction rejected = new RejectedTransaction();
            rejected.setTransactionId(dailyTran.getId());
            rejected.setFailReasonCode(validation.getFailReasonCode());
            rejected.setFailReasonDescription(validation.getFailReasonDescription());
            rejected.setOriginalRecord(buildOriginalRecord(dailyTran));
            log.info("Transaction {} rejected: code={}, reason={}",
                    dailyTran.getId(), validation.getFailReasonCode(),
                    validation.getFailReasonDescription());
            return TransactionPostingResult.rejected(rejected);
        }

        // 2000-POST-TRANSACTION: build the posted transaction
        Transaction posted = buildPostedTransaction(dailyTran);

        // Look up xref and account for update operations
        Optional<CardXref> xrefOpt = validationService.lookupXref(dailyTran.getCardNum());
        if (xrefOpt.isEmpty()) {
            // Should not happen since validation passed, but handle defensively
            log.error("Unexpected: XREF not found after validation for card {}", dailyTran.getCardNum());
            return buildRejection(dailyTran, 100, "INVALID CARD NUMBER FOUND");
        }
        CardXref xref = xrefOpt.get();

        Optional<Account> acctOpt = validationService.lookupAccount(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            log.error("Unexpected: Account not found after validation for acctId {}", xref.getAcctId());
            return buildRejection(dailyTran, 101, "ACCOUNT RECORD NOT FOUND");
        }
        Account account = acctOpt.get();

        // 2700-UPDATE-TCATBAL
        TransactionCategoryBalanceService.UpdateResult catBalResult =
                categoryBalanceService.updateCategoryBalance(xref.getAcctId(), dailyTran);

        // 2800-UPDATE-ACCOUNT-REC
        accountUpdateService.updateAccountBalances(account, dailyTran);

        log.info("Transaction {} posted successfully", dailyTran.getId());
        return TransactionPostingResult.posted(posted, account,
                catBalResult.categoryBalance(), catBalResult.created());
    }

    private Transaction buildPostedTransaction(DailyTransaction dt) {
        Transaction t = new Transaction();
        t.setId(dt.getId());
        t.setTypeCd(dt.getTypeCd());
        t.setCatCd(dt.getCatCd());
        t.setSource(dt.getSource());
        t.setDescription(dt.getDescription());
        t.setAmount(dt.getAmount());
        t.setMerchantId(dt.getMerchantId());
        t.setMerchantName(dt.getMerchantName());
        t.setMerchantCity(dt.getMerchantCity());
        t.setMerchantZip(dt.getMerchantZip());
        t.setCardNum(dt.getCardNum());
        t.setOrigTimestamp(dt.getOrigTimestamp());
        t.setProcTimestamp(timestampService.generateDb2Timestamp());
        return t;
    }

    private TransactionPostingResult buildRejection(DailyTransaction dt, int code, String desc) {
        rejectCounter.increment();
        RejectedTransaction rejected = new RejectedTransaction();
        rejected.setTransactionId(dt.getId());
        rejected.setFailReasonCode(code);
        rejected.setFailReasonDescription(desc);
        rejected.setOriginalRecord(buildOriginalRecord(dt));
        return TransactionPostingResult.rejected(rejected);
    }

    private String buildOriginalRecord(DailyTransaction dt) {
        return String.format("ID=%s|CARD=%s|AMT=%s",
                dt.getId(), dt.getCardNum(), dt.getAmount());
    }
}
