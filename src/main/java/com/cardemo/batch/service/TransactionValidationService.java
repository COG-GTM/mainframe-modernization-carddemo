package com.cardemo.batch.service;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implements the 4 validation rules from CBTRN02C paragraphs 1500-A and 1500-B.
 */
@Service
public class TransactionValidationService {

    private static final Logger log = LoggerFactory.getLogger(TransactionValidationService.class);

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public TransactionValidationService(CardXrefRepository cardXrefRepository,
                                         AccountRepository accountRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Validates a daily transaction through the 4-rule chain.
     * Returns a ValidationResult with either success or failure code/description.
     */
    public ValidationResult validate(DailyTransaction dailyTran) {
        // 1500-A-LOOKUP-XREF: Code 100
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(dailyTran.getCardNum());
        if (xrefOpt.isEmpty()) {
            log.debug("Card number {} not found in XREF", dailyTran.getCardNum());
            return ValidationResult.failure(100, "INVALID CARD NUMBER FOUND");
        }

        CardXref xref = xrefOpt.get();

        // 1500-B-LOOKUP-ACCT: Code 101
        Optional<Account> acctOpt = accountRepository.findById(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            log.debug("Account {} not found", xref.getAcctId());
            return ValidationResult.failure(101, "ACCOUNT RECORD NOT FOUND");
        }

        Account account = acctOpt.get();

        // 1500-B overlimit check: Code 102
        // COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
        // IF ACCT-CREDIT-LIMIT >= WS-TEMP-BAL -> OK, ELSE -> 102
        BigDecimal tempBal = account.getCurrentCycleCredit()
                .subtract(account.getCurrentCycleDebit())
                .add(dailyTran.getAmount());
        if (account.getCreditLimit().compareTo(tempBal) < 0) {
            log.debug("Overlimit: creditLimit={}, tempBal={}", account.getCreditLimit(), tempBal);
            return ValidationResult.failure(102, "OVERLIMIT TRANSACTION");
        }

        // 1500-B expiration check: Code 103
        // IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10) -> OK
        String acctExpDate = account.getExpirationDate();
        String tranDatePortion = dailyTran.getOrigTimestamp() != null
                && dailyTran.getOrigTimestamp().length() >= 10
                ? dailyTran.getOrigTimestamp().substring(0, 10)
                : "";
        if (acctExpDate != null && acctExpDate.compareTo(tranDatePortion) < 0) {
            log.debug("Account expired: expDate={}, tranDate={}", acctExpDate, tranDatePortion);
            return ValidationResult.failure(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        return ValidationResult.success();
    }

    /**
     * Looks up the CardXref for a given card number.
     * Used by the processor after validation passes.
     */
    public Optional<CardXref> lookupXref(String cardNum) {
        return cardXrefRepository.findById(cardNum);
    }

    /**
     * Looks up the Account for a given account ID.
     */
    public Optional<Account> lookupAccount(long acctId) {
        return accountRepository.findById(acctId);
    }
}
