package com.carddemo.accountservice.service;

import com.carddemo.accountservice.dto.AccountUpdateRequest;
import com.carddemo.accountservice.dto.AccountViewResponse;
import com.carddemo.accountservice.dto.AccountViewResponse.AccountDetails;
import com.carddemo.accountservice.dto.AccountViewResponse.CardInfo;
import com.carddemo.accountservice.dto.AccountViewResponse.CustomerDetails;
import com.carddemo.accountservice.entity.Account;
import com.carddemo.accountservice.entity.CardXref;
import com.carddemo.accountservice.entity.Customer;
import com.carddemo.accountservice.exception.AccountNotFoundException;
import com.carddemo.accountservice.exception.AccountValidationException;
import com.carddemo.accountservice.exception.CardXrefNotFoundException;
import com.carddemo.accountservice.exception.CustomerNotFoundException;
import com.carddemo.accountservice.repository.AccountRepository;
import com.carddemo.accountservice.repository.CardXrefRepository;
import com.carddemo.accountservice.repository.CustomerRepository;
import com.carddemo.accountservice.validation.AccountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account service implementing business logic from COACTVWC.cbl (Account View)
 * and COACTUPC.cbl (Account Update).
 *
 * Lookup chain (from COACTVWC.cbl):
 *   Account ID → CXACAIX (card xref) → ACCTDAT (account) + CUSTDAT (customer)
 *
 * Update pattern (from COACTUPC.cbl):
 *   READ for UPDATE → validate → REWRITE (optimistic locking via @Version)
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountValidator accountValidator;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          CardXrefRepository cardXrefRepository,
                          AccountValidator accountValidator) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountValidator = accountValidator;
    }

    /**
     * View account details — ports COACTVWC.cbl logic.
     * Cross-references: CXACAIX → gets card/customer → reads ACCTDAT → reads CUSTDAT.
     */
    @Transactional(readOnly = true)
    public AccountViewResponse viewAccount(Long accountId) {
        accountValidator.validateAccountId(accountId);

        // Step 1: Look up card xref by account ID (CXACAIX alternate index)
        CardXref xref = cardXrefRepository.findFirstByXrefAcctId(accountId)
                .orElseThrow(CardXrefNotFoundException::new);

        // Step 2: Read account master (ACCTDAT)
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        // Step 3: Read customer master (CUSTDAT)
        Customer customer = customerRepository.findById(xref.getXrefCustId())
                .orElseThrow(CustomerNotFoundException::new);

        return buildViewResponse(account, customer, xref);
    }

    /**
     * Update account details — ports COACTUPC.cbl logic.
     * Validates all fields, detects changes, then performs optimistic update.
     */
    @Transactional
    public AccountViewResponse updateAccount(Long accountId, AccountUpdateRequest request) {
        accountValidator.validateAccountId(accountId);
        accountValidator.validateUpdateRequest(request);

        // Step 1: Look up card xref by account ID
        CardXref xref = cardXrefRepository.findFirstByXrefAcctId(accountId)
                .orElseThrow(CardXrefNotFoundException::new);

        // Step 2: Read account for update (optimistic locking via @Version)
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        // Step 3: Read customer for update
        Customer customer = customerRepository.findById(xref.getXrefCustId())
                .orElseThrow(CustomerNotFoundException::new);

        // Step 4: Detect changes — mirrors COACTUPC.cbl "No change detected" check
        boolean accountChanged = applyAccountChanges(account, request);
        boolean customerChanged = applyCustomerChanges(customer, request);

        if (!accountChanged && !customerChanged) {
            throw new AccountValidationException(
                    "No change detected with respect to values fetched.");
        }

        // Step 5: Persist changes (REWRITE in COBOL, JPA merge with optimistic lock)
        if (accountChanged) {
            accountRepository.save(account);
            log.info("Account {} updated", accountId);
        }
        if (customerChanged) {
            customerRepository.save(customer);
            log.info("Customer {} updated for account {}", customer.getCustId(), accountId);
        }

        return buildViewResponse(account, customer, xref);
    }

    private boolean applyAccountChanges(Account account, AccountUpdateRequest request) {
        boolean changed = false;

        if (request.activeStatus() != null && !request.activeStatus().equals(account.getAcctActiveStatus())) {
            account.setAcctActiveStatus(request.activeStatus());
            changed = true;
        }
        if (request.currentBalance() != null && request.currentBalance().compareTo(account.getAcctCurrBal()) != 0) {
            account.setAcctCurrBal(request.currentBalance());
            changed = true;
        }
        if (request.creditLimit() != null && request.creditLimit().compareTo(account.getAcctCreditLimit()) != 0) {
            account.setAcctCreditLimit(request.creditLimit());
            changed = true;
        }
        if (request.cashCreditLimit() != null && request.cashCreditLimit().compareTo(account.getAcctCashCreditLimit()) != 0) {
            account.setAcctCashCreditLimit(request.cashCreditLimit());
            changed = true;
        }
        if (request.openDate() != null && !request.openDate().equals(account.getAcctOpenDate())) {
            account.setAcctOpenDate(request.openDate());
            changed = true;
        }
        if (request.expirationDate() != null && !request.expirationDate().equals(account.getAcctExpirationDate())) {
            account.setAcctExpirationDate(request.expirationDate());
            changed = true;
        }
        if (request.reissueDate() != null && !request.reissueDate().equals(account.getAcctReissueDate())) {
            account.setAcctReissueDate(request.reissueDate());
            changed = true;
        }
        if (request.currentCycleCredit() != null && request.currentCycleCredit().compareTo(account.getAcctCurrCycCredit()) != 0) {
            account.setAcctCurrCycCredit(request.currentCycleCredit());
            changed = true;
        }
        if (request.currentCycleDebit() != null && request.currentCycleDebit().compareTo(account.getAcctCurrCycDebit()) != 0) {
            account.setAcctCurrCycDebit(request.currentCycleDebit());
            changed = true;
        }
        if (request.groupId() != null && !request.groupId().equals(account.getAcctGroupId())) {
            account.setAcctGroupId(request.groupId());
            changed = true;
        }

        return changed;
    }

    private boolean applyCustomerChanges(Customer customer, AccountUpdateRequest request) {
        boolean changed = false;

        if (request.firstName() != null && !request.firstName().equals(customer.getCustFirstName())) {
            customer.setCustFirstName(request.firstName());
            changed = true;
        }
        if (request.middleName() != null && !request.middleName().equals(customer.getCustMiddleName())) {
            customer.setCustMiddleName(request.middleName());
            changed = true;
        }
        if (request.lastName() != null && !request.lastName().equals(customer.getCustLastName())) {
            customer.setCustLastName(request.lastName());
            changed = true;
        }
        if (request.addressLine1() != null && !request.addressLine1().equals(customer.getCustAddrLine1())) {
            customer.setCustAddrLine1(request.addressLine1());
            changed = true;
        }
        if (request.addressLine2() != null && !request.addressLine2().equals(customer.getCustAddrLine2())) {
            customer.setCustAddrLine2(request.addressLine2());
            changed = true;
        }
        if (request.addressLine3() != null && !request.addressLine3().equals(customer.getCustAddrLine3())) {
            customer.setCustAddrLine3(request.addressLine3());
            changed = true;
        }
        if (request.stateCode() != null && !request.stateCode().equals(customer.getCustAddrStateCd())) {
            customer.setCustAddrStateCd(request.stateCode());
            changed = true;
        }
        if (request.countryCode() != null && !request.countryCode().equals(customer.getCustAddrCountryCd())) {
            customer.setCustAddrCountryCd(request.countryCode());
            changed = true;
        }
        if (request.zip() != null && !request.zip().equals(customer.getCustAddrZip())) {
            customer.setCustAddrZip(request.zip());
            changed = true;
        }
        if (request.phoneNumber1() != null && !request.phoneNumber1().equals(customer.getCustPhoneNum1())) {
            customer.setCustPhoneNum1(request.phoneNumber1());
            changed = true;
        }
        if (request.phoneNumber2() != null && !request.phoneNumber2().equals(customer.getCustPhoneNum2())) {
            customer.setCustPhoneNum2(request.phoneNumber2());
            changed = true;
        }
        if (request.ssn() != null) {
            String digitsOnly = request.ssn().replaceAll("[^0-9]", "");
            Long ssnLong = digitsOnly.isEmpty() ? null : Long.parseLong(digitsOnly);
            if (ssnLong != null && !ssnLong.equals(customer.getCustSsn())) {
                customer.setCustSsn(ssnLong);
                changed = true;
            }
        }
        if (request.govtIssuedId() != null && !request.govtIssuedId().equals(customer.getCustGovtIssuedId())) {
            customer.setCustGovtIssuedId(request.govtIssuedId());
            changed = true;
        }
        if (request.dateOfBirth() != null && !request.dateOfBirth().equals(customer.getCustDobYyyyMmDd())) {
            customer.setCustDobYyyyMmDd(request.dateOfBirth());
            changed = true;
        }
        if (request.eftAccountId() != null && !request.eftAccountId().equals(customer.getCustEftAccountId())) {
            customer.setCustEftAccountId(request.eftAccountId());
            changed = true;
        }
        if (request.primaryCardHolderIndicator() != null
                && !request.primaryCardHolderIndicator().equals(customer.getCustPriCardHolderInd())) {
            customer.setCustPriCardHolderInd(request.primaryCardHolderIndicator());
            changed = true;
        }
        if (request.ficoCreditScore() != null
                && !request.ficoCreditScore().equals(customer.getCustFicoCreditScore())) {
            customer.setCustFicoCreditScore(request.ficoCreditScore());
            changed = true;
        }

        return changed;
    }

    private AccountViewResponse buildViewResponse(Account account, Customer customer, CardXref xref) {
        String maskedSsn = customer.getCustSsn() != null
                ? String.format("%09d", customer.getCustSsn())
                : null;

        AccountDetails accountDetails = new AccountDetails(
                account.getAcctId(),
                account.getAcctActiveStatus(),
                account.getAcctCurrBal(),
                account.getAcctCreditLimit(),
                account.getAcctCashCreditLimit(),
                account.getAcctOpenDate(),
                account.getAcctExpirationDate(),
                account.getAcctReissueDate(),
                account.getAcctCurrCycCredit(),
                account.getAcctCurrCycDebit(),
                account.getAcctAddrZip(),
                account.getAcctGroupId()
        );

        CustomerDetails customerDetails = new CustomerDetails(
                customer.getCustId(),
                customer.getCustFirstName(),
                customer.getCustMiddleName(),
                customer.getCustLastName(),
                customer.getCustAddrLine1(),
                customer.getCustAddrLine2(),
                customer.getCustAddrLine3(),
                customer.getCustAddrStateCd(),
                customer.getCustAddrCountryCd(),
                customer.getCustAddrZip(),
                customer.getCustPhoneNum1(),
                customer.getCustPhoneNum2(),
                maskedSsn,
                customer.getCustGovtIssuedId(),
                customer.getCustDobYyyyMmDd(),
                customer.getCustEftAccountId(),
                customer.getCustPriCardHolderInd(),
                customer.getCustFicoCreditScore()
        );

        CardInfo cardInfo = new CardInfo(xref.getXrefCardNum());

        return new AccountViewResponse(accountDetails, customerDetails, cardInfo);
    }
}
