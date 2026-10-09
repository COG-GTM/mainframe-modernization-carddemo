package com.carddemo.service;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.exception.ConcurrentUpdateException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.web.dto.AccountUpdateRequest;
import com.carddemo.web.dto.AccountView;
import com.carddemo.web.dto.CustomerView;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COACTVWC (account view) and COACTUPC (account update).
 *
 * <p>Both programs resolve the account through the card cross reference before reading the account
 * master and the owning customer, and both surface the same messages on failure.
 */
@Service
public class AccountService {

    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final CardXrefRepository xrefs;
    private final AccountUpdateValidator validator;

    public AccountService(AccountRepository accounts,
                          CustomerRepository customers,
                          CardXrefRepository xrefs,
                          AccountUpdateValidator validator) {
        this.accounts = accounts;
        this.customers = customers;
        this.xrefs = xrefs;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public AccountView view(Long accountId) {
        validateAccountId(accountId);
        CardXref xref = xrefs.findFirstByAccountIdOrderByCardNumber(accountId)
                .orElseThrow(() -> new RecordNotFoundException(
                        "Did not find this account in account card xref file"));
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new RecordNotFoundException(
                        "Did not find this account in account master file"));
        Customer customer = customers.findById(xref.getCustomerId())
                .orElseThrow(() -> new RecordNotFoundException(
                        "Did not find associated customer in master file"));
        return toView(account, customer);
    }

    @Transactional
    public AccountView update(Long accountId, AccountUpdateRequest request) {
        AccountView current = view(accountId);
        if (request.version() == null || request.version() != current.version()) {
            throw new ConcurrentUpdateException("Record changed by some one else. Please review");
        }
        validator.validate(request, current);
        if (isUnchanged(current, request)) {
            throw new BusinessRuleException("No change detected with respect to values fetched.");
        }

        Account account = accounts.findById(accountId).orElseThrow();
        Customer customer = customers.findById(current.customer().customerId()).orElseThrow();

        account.setActiveStatus(request.activeStatus().toUpperCase());
        account.setCreditLimit(request.creditLimit());
        account.setCashCreditLimit(request.cashCreditLimit());
        account.setCurrentBalance(request.currentBalance());
        account.setCurrentCycleCredit(request.currentCycleCredit());
        account.setCurrentCycleDebit(request.currentCycleDebit());
        account.setOpenDate(request.openDate());
        account.setExpirationDate(request.expirationDate());
        account.setReissueDate(request.reissueDate());
        account.setGroupId(request.groupId());

        CustomerView update = request.customer();
        customer.setFirstName(update.firstName());
        customer.setMiddleName(update.middleName());
        customer.setLastName(update.lastName());
        customer.setAddressLine1(update.addressLine1());
        customer.setAddressLine2(update.addressLine2());
        customer.setAddressLine3(update.addressLine3());
        customer.setStateCode(update.stateCode());
        customer.setCountryCode(update.countryCode());
        customer.setZipCode(update.zipCode());
        customer.setPhoneNumber1(update.phoneNumber1());
        customer.setPhoneNumber2(update.phoneNumber2());
        customer.setSsn(update.ssn());
        customer.setGovernmentIssuedId(update.governmentIssuedId());
        customer.setDateOfBirth(update.dateOfBirth());
        customer.setEftAccountId(update.eftAccountId());
        customer.setPrimaryCardHolderIndicator(update.primaryCardHolderIndicator());
        customer.setFicoCreditScore(update.ficoCreditScore());
        account.setAddressZip(update.zipCode());

        return toView(accounts.save(account), customers.save(customer));
    }

    /** COACTVWC / COACTUPC: "Account number must be a non zero 11 digit number". */
    public static void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0 || accountId > 99_999_999_999L) {
            throw new BusinessRuleException("Account number must be a non zero 11 digit number");
        }
    }

    private boolean isUnchanged(AccountView current, AccountUpdateRequest request) {
        return equalsIgnoreCaseTrimmed(current.activeStatus(), request.activeStatus())
                && equalAmount(current.creditLimit(), request.creditLimit())
                && equalAmount(current.cashCreditLimit(), request.cashCreditLimit())
                && equalAmount(current.currentBalance(), request.currentBalance())
                && equalAmount(current.currentCycleCredit(), request.currentCycleCredit())
                && equalAmount(current.currentCycleDebit(), request.currentCycleDebit())
                && equalsIgnoreCaseTrimmed(current.openDate(), request.openDate())
                && equalsIgnoreCaseTrimmed(current.expirationDate(), request.expirationDate())
                && equalsIgnoreCaseTrimmed(current.reissueDate(), request.reissueDate())
                && equalsIgnoreCaseTrimmed(current.groupId(), request.groupId())
                && Objects.equals(withoutId(current.customer()), withoutId(request.customer()));
    }

    private static CustomerView withoutId(CustomerView customer) {
        return new CustomerView(
                null,
                customer.firstName(),
                customer.middleName(),
                customer.lastName(),
                customer.addressLine1(),
                customer.addressLine2(),
                customer.addressLine3(),
                customer.stateCode(),
                customer.countryCode(),
                customer.zipCode(),
                customer.phoneNumber1(),
                customer.phoneNumber2(),
                customer.ssn(),
                customer.governmentIssuedId(),
                customer.dateOfBirth(),
                customer.eftAccountId(),
                customer.primaryCardHolderIndicator(),
                customer.ficoCreditScore());
    }

    private static boolean equalsIgnoreCaseTrimmed(String left, String right) {
        return Objects.toString(left, "").trim().equalsIgnoreCase(Objects.toString(right, "").trim());
    }

    private static boolean equalAmount(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.compareTo(right) == 0;
    }

    static AccountView toView(Account account, Customer customer) {
        return new AccountView(
                account.getId(),
                account.getActiveStatus(),
                account.getCurrentBalance(),
                account.getCreditLimit(),
                account.getCashCreditLimit(),
                account.getOpenDate(),
                account.getExpirationDate(),
                account.getReissueDate(),
                account.getCurrentCycleCredit(),
                account.getCurrentCycleDebit(),
                account.getGroupId(),
                new CustomerView(
                        customer.getId(),
                        customer.getFirstName(),
                        customer.getMiddleName(),
                        customer.getLastName(),
                        customer.getAddressLine1(),
                        customer.getAddressLine2(),
                        customer.getAddressLine3(),
                        customer.getStateCode(),
                        customer.getCountryCode(),
                        customer.getZipCode(),
                        customer.getPhoneNumber1(),
                        customer.getPhoneNumber2(),
                        customer.getSsn(),
                        customer.getGovernmentIssuedId(),
                        customer.getDateOfBirth(),
                        customer.getEftAccountId(),
                        customer.getPrimaryCardHolderIndicator(),
                        customer.getFicoCreditScore()),
                account.getVersion());
    }
}
