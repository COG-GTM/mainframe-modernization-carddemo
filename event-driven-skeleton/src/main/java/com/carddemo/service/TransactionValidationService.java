package com.carddemo.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.carddemo.event.TransactionPendingEvent;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;

/**
 * Transaction validation service.
 *
 * Replaces: CBTRN02C paragraphs 1500-VALIDATE-TRAN, 1500-A-LOOKUP-XREF,
 * and 1500-B-LOOKUP-ACCT (lines 370-422).
 *
 * Encapsulates the four validation rules from the COBOL program:
 *   100 — Card number not found in XREF
 *   101 — Account record not found
 *   102 — Transaction would exceed credit limit (overlimit)
 *   103 — Transaction received after account expiration
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
     * Validate a pending transaction against all business rules.
     *
     * Replaces: 1500-VALIDATE-TRAN (lines 370-378)
     * Runs validations in sequence — first XREF lookup, then account checks.
     * Returns a ValidationResult containing either the resolved XREF + account
     * data (on success) or a failure reason code and description (on failure).
     */
    public ValidationResult validate(TransactionPendingEvent event) {
        // Step 1: Lookup card cross-reference
        // Replaces: 1500-A-LOOKUP-XREF (lines 380-392)
        // MOVE DALYTRAN-CARD-NUM TO FD-XREF-CARD-NUM
        // READ XREF-FILE INTO CARD-XREF-RECORD
        CardXref xref = cardXrefRepository.findById(event.cardNumber()).orElse(null);
        if (xref == null) {
            log.warn("Validation failed: card number {} not found in XREF", event.cardNumber());
            return ValidationResult.failure(100, "INVALID CARD NUMBER FOUND");
        }

        // Step 2: Lookup account and perform balance/expiration checks
        // Replaces: 1500-B-LOOKUP-ACCT (lines 393-422)
        // MOVE XREF-ACCT-ID TO FD-ACCT-ID
        // READ ACCOUNT-FILE INTO ACCOUNT-RECORD
        Account account = accountRepository.findById(xref.getAcctId()).orElse(null);
        if (account == null) {
            log.warn("Validation failed: account {} not found for card {}",
                    xref.getAcctId(), event.cardNumber());
            return ValidationResult.failure(101, "ACCOUNT RECORD NOT FOUND");
        }

        // Credit limit check
        // Replaces: COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT
        //                               - ACCT-CURR-CYC-DEBIT
        //                               + DALYTRAN-AMT
        //           IF ACCT-CREDIT-LIMIT >= WS-TEMP-BAL
        BigDecimal cycleCredit = account.getCurrentCycleCredit() != null
                ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
        BigDecimal cycleDebit = account.getCurrentCycleDebit() != null
                ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
        BigDecimal tempBalance = cycleCredit.subtract(cycleDebit).add(event.amount());

        if (account.getCreditLimit() != null && account.getCreditLimit().compareTo(tempBalance) < 0) {
            log.warn("Validation failed: overlimit for account {} — limit={}, projected={}",
                    xref.getAcctId(), account.getCreditLimit(), tempBalance);
            return ValidationResult.failure(102, "OVERLIMIT TRANSACTION");
        }

        // Expiration date check
        // Replaces: IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10)
        // COBOL substring (1:10) extracts the date portion of the timestamp
        if (account.getExpirationDate() != null && event.originTimestamp() != null) {
            String txnDate = event.originTimestamp().length() >= 10
                    ? event.originTimestamp().substring(0, 10)
                    : event.originTimestamp();
            if (account.getExpirationDate().compareTo(txnDate) < 0) {
                log.warn("Validation failed: account {} expired {} before txn date {}",
                        xref.getAcctId(), account.getExpirationDate(), txnDate);
                return ValidationResult.failure(103,
                        "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
            }
        }

        return ValidationResult.success(xref, account);
    }

    /**
     * Result of transaction validation, carrying either success data or failure info.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final CardXref xref;
        private final Account account;
        private final int failureReasonCode;
        private final String failureDescription;

        private ValidationResult(boolean valid, CardXref xref, Account account,
                                 int failureReasonCode, String failureDescription) {
            this.valid = valid;
            this.xref = xref;
            this.account = account;
            this.failureReasonCode = failureReasonCode;
            this.failureDescription = failureDescription;
        }

        public static ValidationResult success(CardXref xref, Account account) {
            return new ValidationResult(true, xref, account, 0, null);
        }

        public static ValidationResult failure(int code, String description) {
            return new ValidationResult(false, null, null, code, description);
        }

        public boolean isValid() {
            return valid;
        }

        public CardXref getXref() {
            return xref;
        }

        public Account getAccount() {
            return account;
        }

        public int getFailureReasonCode() {
            return failureReasonCode;
        }

        public String getFailureDescription() {
            return failureDescription;
        }
    }
}
