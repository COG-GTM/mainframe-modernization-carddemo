package com.carddemo.account.service;

import com.carddemo.account.dto.AccountRequest;
import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.dto.BalanceUpdateRequest;
import com.carddemo.account.entity.Account;
import com.carddemo.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Account Service translating business logic from:
 * - COACTVWC.cbl (Account View): read account by ID from ACCTDAT VSAM file
 * - COACTUPC.cbl (Account Update): validate and update account fields
 * - CBACT01C.cbl (Batch Print): read and display account data
 *
 * Original COBOL flow for view (COACTVWC 9000-READ-ACCT):
 *   1. Look up account in CARDXREF by account ID
 *   2. Read account data from ACCTDAT
 *   3. Read customer data from CUSTDAT
 *   In the modernized version, we read directly from the accounts table.
 *
 * Original COBOL flow for update (COACTUPC):
 *   1. Validate all input fields (status Y/N, numeric limits, dates)
 *   2. Compare with existing record to detect changes
 *   3. REWRITE the ACCTDAT record with updated fields
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Get account by ID.
     * Translates COACTVWC paragraph 9300-GETACCTDATA-BYACCT:
     *   EXEC CICS READ DATASET(ACCTDAT) RIDFLD(acct-id) INTO(ACCOUNT-RECORD)
     *
     * @param accountId 11-digit account identifier
     * @return AccountResponse if found, empty otherwise
     */
    public Optional<AccountResponse> getAccount(String accountId) {
        log.info("Looking up account: {}", accountId);
        return accountRepository.findById(accountId)
                .map(AccountResponse::fromEntity);
    }

    /**
     * Update account fields.
     * Translates COACTUPC paragraph 9000-WRITE-DATA:
     *   Validates input, then EXEC CICS REWRITE on ACCTDAT.
     *
     * Updatable fields (from COACTUPC WS-NON-KEY-FLAGS):
     *   - ACCT-ACTIVE-STATUS (must be Y or N)
     *   - ACCT-CREDIT-LIMIT (signed numeric)
     *   - ACCT-CASH-CREDIT-LIMIT (signed numeric)
     *   - ACCT-GROUP-ID
     *   - ACCT-ADDR-ZIP
     *
     * @param accountId the account to update
     * @param request   the fields to update
     * @return updated AccountResponse
     * @throws IllegalArgumentException if account not found or validation fails
     */
    @Transactional
    public AccountResponse updateAccount(String accountId, AccountRequest request) {
        log.info("Updating account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found: " + accountId));

        // Apply updates for non-null fields (mirrors COACTUPC change detection)
        if (request.getAcctActiveStatus() != null) {
            String status = request.getAcctActiveStatus().toUpperCase();
            if (!"Y".equals(status) && !"N".equals(status)) {
                throw new IllegalArgumentException(
                        "Account active status must be Y or N");
            }
            account.setAcctActiveStatus(status);
        }
        if (request.getAcctCreditLimit() != null) {
            account.setAcctCreditLimit(request.getAcctCreditLimit());
        }
        if (request.getAcctCashCreditLimit() != null) {
            account.setAcctCashCreditLimit(request.getAcctCashCreditLimit());
        }
        if (request.getAcctGroupId() != null) {
            account.setAcctGroupId(request.getAcctGroupId());
        }
        if (request.getAcctAddrZip() != null) {
            account.setAcctAddrZip(request.getAcctAddrZip());
        }

        Account saved = accountRepository.save(account);
        log.info("Account {} updated successfully", accountId);
        return AccountResponse.fromEntity(saved);
    }

    /**
     * Update account balance.
     * Used by Transaction Service and Billing Service via PUT /accounts/{id}/balance.
     *
     * Translates the balance update portion of CBACT04C paragraph 1050-UPDATE-ACCOUNT:
     *   ADD amount TO ACCT-CURR-BAL
     *   Update ACCT-CURR-CYC-CREDIT or ACCT-CURR-CYC-DEBIT based on sign
     *
     * @param accountId the account to update
     * @param request   contains the amount (positive = credit, negative = debit)
     * @return updated AccountResponse
     */
    @Transactional
    public AccountResponse updateBalance(String accountId, BalanceUpdateRequest request) {
        log.info("Updating balance for account: {}, amount: {}", accountId, request.getAmount());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found: " + accountId));

        BigDecimal amount = request.getAmount();

        // Add amount to current balance (COBOL: ADD amount TO ACCT-CURR-BAL)
        account.setAcctCurrBal(account.getAcctCurrBal().add(amount));

        // Update cycle credit or debit based on sign
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            account.setAcctCurrCycCredit(
                    account.getAcctCurrCycCredit().add(amount));
        } else {
            account.setAcctCurrCycDebit(
                    account.getAcctCurrCycDebit().add(amount.abs()));
        }

        Account saved = accountRepository.save(account);
        log.info("Balance updated for account {}: new balance = {}",
                accountId, saved.getAcctCurrBal());
        return AccountResponse.fromEntity(saved);
    }
}
