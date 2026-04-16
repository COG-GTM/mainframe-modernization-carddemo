package com.carddemo.batch.service;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.CardXref;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.RejectedTransaction;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardXrefRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Validates daily transactions against XREF and Account master data.
 * Implements the business rules from COBOL paragraph 1500-VALIDATE-TRAN
 * in CBTRN02C.cbl (lines 370-421).
 */
@Service
public class TransactionValidationService {

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public TransactionValidationService(CardXrefRepository cardXrefRepository,
                                        AccountRepository accountRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Validates a daily transaction.
     * Returns Optional.empty() if valid, or Optional containing the rejection details.
     *
     * Validation rules (matching COBOL behavior):
     * 1. XREF Lookup - short-circuits if card not found
     * 2. Account Lookup - short-circuits if account not found
     * 3. Credit Limit Check - does NOT short-circuit (both 3 and 4 are evaluated)
     * 4. Expiration Check - last-writer-wins if both 3 and 4 fail
     */
    public Optional<RejectedTransaction> validate(DailyTransaction txn) {
        // Rule 1: XREF Lookup
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(txn.getCardNumber());
        if (xrefOpt.isEmpty()) {
            return Optional.of(new RejectedTransaction(txn, 100, "INVALID CARD NUMBER FOUND"));
        }

        CardXref xref = xrefOpt.get();

        // Rule 2: Account Lookup
        Optional<Account> accountOpt = accountRepository.findById(xref.getAccountId());
        if (accountOpt.isEmpty()) {
            return Optional.of(new RejectedTransaction(txn, 101, "ACCOUNT RECORD NOT FOUND"));
        }

        Account account = accountOpt.get();

        // Rules 3 and 4: NOT short-circuited. Both are evaluated.
        // If both fail, reason 103 overwrites 102 (last-writer-wins).
        int failReason = 0;
        String failDescription = null;

        // Rule 3: Credit Limit Check
        // COBOL: COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
        // COBOL: IF ACCT-CREDIT-LIMIT >= WS-TEMP-BAL ... ELSE reason 102
        BigDecimal tempBal = account.getCurrentCycleCredit()
                .subtract(account.getCurrentCycleDebit())
                .add(txn.getAmount());

        if (account.getCreditLimit().compareTo(tempBal) < 0) {
            failReason = 102;
            failDescription = "OVERLIMIT TRANSACTION";
        }

        // Rule 4: Expiration Check
        // COBOL: IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10) ... ELSE reason 103
        if (account.getExpirationDate().isBefore(txn.getOriginTimestamp().toLocalDate())) {
            failReason = 103;
            failDescription = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION";
        }

        if (failReason != 0) {
            return Optional.of(new RejectedTransaction(txn, failReason, failDescription));
        }

        return Optional.empty();
    }

    /**
     * Returns the CardXref for a given card number.
     * Used by the processor to pass xref data to the posting service.
     */
    public Optional<CardXref> lookupXref(String cardNumber) {
        return cardXrefRepository.findById(cardNumber);
    }

    /**
     * Returns the Account for a given account ID.
     * Used by the processor to pass account data to the posting service.
     */
    public Optional<Account> lookupAccount(long accountId) {
        return accountRepository.findById(accountId);
    }
}
