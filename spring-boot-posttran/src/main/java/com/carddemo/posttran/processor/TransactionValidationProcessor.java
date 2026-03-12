package com.carddemo.posttran.processor;

import com.carddemo.posttran.model.Account;
import com.carddemo.posttran.model.CardXref;
import com.carddemo.posttran.model.DailyTransaction;
import com.carddemo.posttran.model.ProcessedTransaction;
import com.carddemo.posttran.model.ValidationFailReason;
import com.carddemo.posttran.repository.AccountRepository;
import com.carddemo.posttran.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Validates daily transactions against cross-reference and account data.
 * Maps to COBOL sections:
 * <ul>
 *   <li>1500-VALIDATE-TRAN — orchestrates validation</li>
 *   <li>1500-A-LOOKUP-XREF — card cross-reference lookup</li>
 *   <li>1500-B-LOOKUP-ACCT — account lookup + credit limit + expiry checks</li>
 * </ul>
 *
 * Returns a {@link ProcessedTransaction} with validation status and resolved account ID.
 */
public class TransactionValidationProcessor
        implements ItemProcessor<DailyTransaction, ProcessedTransaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionValidationProcessor.class);

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public TransactionValidationProcessor(CardXrefRepository cardXrefRepository,
                                          AccountRepository accountRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ProcessedTransaction process(DailyTransaction dailyTransaction) {
        // 1500-A-LOOKUP-XREF: look up card cross-reference
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(dailyTransaction.getTranCardNum());
        if (xrefOpt.isEmpty()) {
            log.warn("Validation failed for tran {}: {}",
                    dailyTransaction.getTranId(),
                    ValidationFailReason.INVALID_CARD_NUMBER.getDescription());
            return ProcessedTransaction.rejectedResult(
                    dailyTransaction,
                    ValidationFailReason.INVALID_CARD_NUMBER.getCode(),
                    ValidationFailReason.INVALID_CARD_NUMBER.getDescription());
        }

        CardXref xref = xrefOpt.get();
        long accountId = xref.getXrefAcctId();

        // 1500-B-LOOKUP-ACCT: look up account record
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.warn("Validation failed for tran {}: {}",
                    dailyTransaction.getTranId(),
                    ValidationFailReason.ACCOUNT_NOT_FOUND.getDescription());
            return ProcessedTransaction.rejectedResult(
                    dailyTransaction,
                    ValidationFailReason.ACCOUNT_NOT_FOUND.getCode(),
                    ValidationFailReason.ACCOUNT_NOT_FOUND.getDescription());
        }

        Account account = accountOpt.get();

        // COBOL 1500-B-LOOKUP-ACCT runs both checks sequentially within the
        // NOT INVALID KEY block (lines 403-420). The last failing check
        // overwrites WS-VALIDATION-FAIL-REASON, so we accumulate here and
        // the last failure wins — matching the original COBOL behavior.
        int failReason = 0;
        String failReasonDesc = "";

        // Credit limit check (COBOL lines 403-413):
        // WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
        // IF ACCT-CREDIT-LIMIT < WS-TEMP-BAL -> fail 102
        BigDecimal currCycCredit = account.getAcctCurrCycCredit() != null
                ? account.getAcctCurrCycCredit() : BigDecimal.ZERO;
        BigDecimal currCycDebit = account.getAcctCurrCycDebit() != null
                ? account.getAcctCurrCycDebit() : BigDecimal.ZERO;
        BigDecimal creditLimit = account.getAcctCreditLimit() != null
                ? account.getAcctCreditLimit() : BigDecimal.ZERO;

        BigDecimal tempBal = currCycCredit
                .subtract(currCycDebit)
                .add(dailyTransaction.getTranAmt());

        if (creditLimit.compareTo(tempBal) < 0) {
            failReason = ValidationFailReason.OVER_LIMIT.getCode();
            failReasonDesc = ValidationFailReason.OVER_LIMIT.getDescription();
        }

        // Account expiry check (COBOL lines 414-420):
        // IF ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10) -> fail 103
        // This runs regardless of the credit limit result (COBOL sequential behavior).
        String acctExpDate = account.getAcctExpirationDate() != null
                ? account.getAcctExpirationDate().trim() : "";
        String tranDatePart = "";
        if (dailyTransaction.getTranOrigTs() != null
                && dailyTransaction.getTranOrigTs().length() >= 10) {
            tranDatePart = dailyTransaction.getTranOrigTs().substring(0, 10);
        }

        if (!acctExpDate.isEmpty() && !tranDatePart.isEmpty()
                && acctExpDate.compareTo(tranDatePart) < 0) {
            failReason = ValidationFailReason.ACCOUNT_EXPIRED.getCode();
            failReasonDesc = ValidationFailReason.ACCOUNT_EXPIRED.getDescription();
        }

        // If any validation failed, reject the transaction
        if (failReason != 0) {
            log.warn("Validation failed for tran {}: {}", dailyTransaction.getTranId(), failReasonDesc);
            return ProcessedTransaction.rejectedResult(dailyTransaction, failReason, failReasonDesc);
        }

        // All validations passed
        return ProcessedTransaction.validResult(dailyTransaction, accountId);
    }
}
