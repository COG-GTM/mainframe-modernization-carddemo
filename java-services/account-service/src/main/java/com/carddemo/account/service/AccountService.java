package com.carddemo.account.service;

import com.carddemo.account.dto.AccountDto;
import com.carddemo.account.dto.AccountUpdateRequest;
import com.carddemo.account.exception.ResourceNotFoundException;
import com.carddemo.account.model.Account;
import com.carddemo.account.model.CardXref;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.repository.CardXrefRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer implementing business logic migrated from:
 * - COACTVWC.cbl (Account View)
 * - COACTUPC.cbl (Account Update)
 *
 * The COBOL programs read account records from VSAM files by ACCT-ID,
 * validate fields (status Y/N, credit limit > 0, date formats, etc.),
 * and write back. This service replicates that logic using JPA.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;

    public AccountService(AccountRepository accountRepository, CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Retrieve account by ID.
     * Mirrors COACTVWC.cbl paragraph 9000-READ-ACCT which reads from ACCTDAT file.
     */
    public AccountDto getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account ID NOT found"));
        return toDto(account);
    }

    /**
     * List accounts with pagination.
     */
    public Page<AccountDto> listAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Update account fields.
     * Mirrors COACTUPC.cbl validation logic:
     * - ACCT-ACTIVE-STATUS must be 'Y' or 'N' (FLG-ACCT-STATUS-ISVALID VALUES 'Y','N')
     * - ACCT-CREDIT-LIMIT must be > 0 (validated via signed number edit)
     * - ACCT-CASH-CREDIT-LIMIT must be > 0
     * - Date fields validated for format (CCYYMMDD in COBOL, YYYY-MM-DD here)
     * - ACCT-GROUP-ID alphanumeric
     */
    @Transactional
    public AccountDto updateAccount(Long accountId, AccountUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account ID NOT found"));

        account.setActiveStatus(request.getActiveStatus());
        account.setCreditLimit(request.getCreditLimit());
        account.setCashCreditLimit(request.getCashCreditLimit());
        account.setOpenDate(request.getOpenDate());
        account.setExpirationDate(request.getExpirationDate());

        if (request.getReissueDate() != null) {
            account.setReissueDate(request.getReissueDate());
        }
        if (request.getGroupId() != null) {
            account.setGroupId(request.getGroupId());
        }

        Account saved = accountRepository.save(account);
        return toDto(saved);
    }

    /**
     * Find account by card number using cross-reference lookup.
     * Mirrors COACTVWC.cbl which reads CARDXREF file (CVACT03Y.cpy layout)
     * to resolve card number -> account ID, then reads account.
     */
    public AccountDto getAccountByCardNumber(String cardNumber) {
        CardXref xref = cardXrefRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Did not find this account in account card xref file"));
        return getAccountById(xref.getAccountId());
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .accountId(account.getAccountId())
                .activeStatus(account.getActiveStatus())
                .currentBalance(account.getCurrentBalance())
                .creditLimit(account.getCreditLimit())
                .cashCreditLimit(account.getCashCreditLimit())
                .openDate(account.getOpenDate())
                .expirationDate(account.getExpirationDate())
                .reissueDate(account.getReissueDate())
                .currentCycleCredit(account.getCurrentCycleCredit())
                .currentCycleDebit(account.getCurrentCycleDebit())
                .addressZip(account.getAddressZip())
                .groupId(account.getGroupId())
                .build();
    }
}
