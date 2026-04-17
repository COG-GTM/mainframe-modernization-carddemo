package com.carddemo.account.service;

import com.carddemo.account.dto.AccountListResponse;
import com.carddemo.account.dto.AccountListResponse.AccountSummary;
import com.carddemo.account.dto.AccountResponse;
import com.carddemo.account.dto.AccountResponse.AccountData;
import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.dto.AccountUpdateRequest.AccountFields;
import com.carddemo.account.entity.AccountEntity;
import com.carddemo.account.entity.CardXrefEntity;
import com.carddemo.account.entity.CustomerEntity;
import com.carddemo.account.exception.ResourceNotFoundException;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.repository.CardXrefRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for account operations.
 * Migrated from: COACTVWC.cbl (Account View — CAVW) and COACTUPC.cbl (Account Update — CAUP)
 * CICS operations: READ ACCTDAT, READ CUSTDAT, STARTBR/READNEXT CXACAIX, REWRITE ACCTDAT, REWRITE CUSTDAT
 * VSAM files: ACCTDAT (KSDS), CUSTDAT (KSDS), CXACAIX (AIX PATH)
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerService customerService;

    public AccountService(AccountRepository accountRepository,
                          CardXrefRepository cardXrefRepository,
                          CustomerService customerService) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerService = customerService;
    }

    /**
     * View account with combined customer and card data.
     * Replaces COACTVWC.cbl: reads ACCTDAT, CUSTDAT, and browses CXACAIX.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + accountId));

        // Read linked cards from CXACAIX (cross-reference AIX)
        List<CardXrefEntity> xrefs = cardXrefRepository.findByAccountId(accountId);

        // Find customer via first cross-reference entry
        CustomerEntity customer = null;
        if (!xrefs.isEmpty()) {
            Long custId = xrefs.getFirst().getCustId();
            try {
                customer = customerService.findById(custId);
            } catch (ResourceNotFoundException ignored) {
                // Customer may not exist in edge cases
            }
        }

        List<String> cardNumbers = xrefs.stream()
                .map(CardXrefEntity::getCardNum)
                .toList();

        return new AccountResponse(
                toAccountData(account),
                customer != null ? customerService.toDto(customer) : null,
                cardNumbers
        );
    }

    /**
     * Update account and customer atomically.
     * Replaces COACTUPC.cbl: REWRITE ACCTDAT + REWRITE CUSTDAT in same CICS unit of work.
     */
    @Transactional
    public AccountResponse updateAccount(Long accountId, AccountUpdateRequest request) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + accountId));

        // Update account fields (EXEC CICS REWRITE on ACCTDAT)
        if (request.account() != null) {
            applyAccountFields(account, request.account());
            accountRepository.save(account);
        }

        // Read linked cards from CXACAIX
        List<CardXrefEntity> xrefs = cardXrefRepository.findByAccountId(accountId);

        // Update customer fields (EXEC CICS REWRITE on CUSTDAT)
        CustomerEntity customer = null;
        if (!xrefs.isEmpty()) {
            Long custId = xrefs.getFirst().getCustId();
            if (request.customer() != null) {
                customer = customerService.updateCustomer(custId, request.customer());
            } else {
                try {
                    customer = customerService.findById(custId);
                } catch (ResourceNotFoundException ignored) {
                    // Customer may not exist
                }
            }
        }

        List<String> cardNumbers = xrefs.stream()
                .map(CardXrefEntity::getCardNum)
                .toList();

        return new AccountResponse(
                toAccountData(account),
                customer != null ? customerService.toDto(customer) : null,
                cardNumbers
        );
    }

    /**
     * List accounts with pagination.
     */
    @Transactional(readOnly = true)
    public AccountListResponse listAccounts(int page, int size) {
        Page<AccountEntity> accountPage = accountRepository.findAll(
                PageRequest.of(page, size, Sort.by("accountId")));

        List<AccountSummary> summaries = accountPage.getContent().stream()
                .map(a -> new AccountSummary(
                        a.getAccountId(),
                        a.getActiveStatus(),
                        a.getCurrentBalance(),
                        a.getCreditLimit(),
                        a.getGroupId()))
                .toList();

        return new AccountListResponse(
                summaries,
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages()
        );
    }

    private void applyAccountFields(AccountEntity entity, AccountFields fields) {
        if (fields.activeStatus() != null) entity.setActiveStatus(fields.activeStatus());
        if (fields.currentBalance() != null) entity.setCurrentBalance(fields.currentBalance());
        if (fields.creditLimit() != null) entity.setCreditLimit(fields.creditLimit());
        if (fields.cashCreditLimit() != null) entity.setCashCreditLimit(fields.cashCreditLimit());
        if (fields.openDate() != null) entity.setOpenDate(fields.openDate());
        if (fields.expirationDate() != null) entity.setExpirationDate(fields.expirationDate());
        if (fields.reissueDate() != null) entity.setReissueDate(fields.reissueDate());
        if (fields.currentCycleCredit() != null) entity.setCurrentCycleCredit(fields.currentCycleCredit());
        if (fields.currentCycleDebit() != null) entity.setCurrentCycleDebit(fields.currentCycleDebit());
        if (fields.groupId() != null) entity.setGroupId(fields.groupId());
    }

    private AccountData toAccountData(AccountEntity entity) {
        return new AccountData(
                entity.getAccountId(),
                entity.getActiveStatus(),
                entity.getCurrentBalance(),
                entity.getCreditLimit(),
                entity.getCashCreditLimit(),
                entity.getOpenDate(),
                entity.getExpirationDate(),
                entity.getReissueDate(),
                entity.getCurrentCycleCredit(),
                entity.getCurrentCycleDebit(),
                entity.getGroupId()
        );
    }
}
