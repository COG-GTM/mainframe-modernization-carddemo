package com.carddemo.account.service;

import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.entity.Account;
import com.carddemo.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Interest Calculation Service translated from CBACT04C.cbl.
 *
 * Original COBOL batch program flow:
 *   1. Open TCATBALF (transaction category balance file) sequentially
 *   2. For each record, look up ACCT-GROUP-ID from account master
 *   3. Look up interest rate from DISCGRP (disclosure group) file
 *      using composite key: ACCT-GROUP-ID + TRAN-TYPE-CD + TRAN-CAT-CD
 *   4. If DIS-INT-RATE != 0:
 *        COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 *        ADD WS-MONTHLY-INT TO WS-TOTAL-INT
 *   5. When account changes, update account:
 *        ADD WS-TOTAL-INT TO ACCT-CURR-BAL
 *        MOVE 0 TO ACCT-CURR-CYC-CREDIT
 *        MOVE 0 TO ACCT-CURR-CYC-DEBIT
 *        REWRITE account record
 *
 * In the modernized version, this is triggered via REST endpoint
 * POST /accounts/{id}/calculate-interest with an interest rate parameter.
 * The TCATBALF and DISCGRP lookups are simplified since the interest rate
 * can be provided directly or defaulted.
 */
@Service
public class InterestCalculationService {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationService.class);

    /**
     * Default annual interest rate (percentage) used when no rate is provided.
     * In the original COBOL, this came from the DISCGRP file.
     */
    private static final BigDecimal DEFAULT_ANNUAL_INTEREST_RATE = new BigDecimal("22.99");

    /**
     * Divisor for converting annual rate to monthly: rate / 1200
     * From CBACT04C line 465: COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
     */
    private static final BigDecimal MONTHLY_DIVISOR = new BigDecimal("1200");

    private final AccountRepository accountRepository;

    public InterestCalculationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Calculate and apply interest for a given account.
     *
     * Translates CBACT04C paragraphs:
     *   1300-COMPUTE-INTEREST:
     *     COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
     *     ADD WS-MONTHLY-INT TO WS-TOTAL-INT
     *
     *   1050-UPDATE-ACCOUNT:
     *     ADD WS-TOTAL-INT TO ACCT-CURR-BAL
     *     MOVE 0 TO ACCT-CURR-CYC-CREDIT
     *     MOVE 0 TO ACCT-CURR-CYC-DEBIT
     *     REWRITE FD-ACCTFILE-REC FROM ACCOUNT-RECORD
     *
     * @param accountId    the account to calculate interest for
     * @param interestRate optional annual interest rate; defaults to 22.99%
     * @return updated AccountResponse with interest applied
     */
    @Transactional
    public AccountResponse calculateInterest(String accountId, BigDecimal interestRate) {
        log.info("Calculating interest for account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found: " + accountId));

        BigDecimal rate = (interestRate != null) ? interestRate : DEFAULT_ANNUAL_INTEREST_RATE;

        // COBOL: COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
        // Using current balance as the balance to compute interest on
        BigDecimal balance = account.getAcctCurrBal();
        BigDecimal monthlyInterest = balance.multiply(rate)
                .divide(MONTHLY_DIVISOR, 2, RoundingMode.HALF_UP);

        log.info("Account {}: balance={}, rate={}, monthlyInterest={}",
                accountId, balance, rate, monthlyInterest);

        // COBOL: ADD WS-TOTAL-INT TO ACCT-CURR-BAL
        account.setAcctCurrBal(balance.add(monthlyInterest));

        // COBOL: MOVE 0 TO ACCT-CURR-CYC-CREDIT / ACCT-CURR-CYC-DEBIT
        account.setAcctCurrCycCredit(BigDecimal.ZERO);
        account.setAcctCurrCycDebit(BigDecimal.ZERO);

        Account saved = accountRepository.save(account);
        log.info("Interest applied for account {}: new balance = {}",
                accountId, saved.getAcctCurrBal());

        return AccountResponse.fromEntity(saved);
    }
}
