package com.carddemo.service.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.web.account.dto.AccountUpdateRequest;
import com.carddemo.web.account.dto.AccountUpdateResponse;
import com.carddemo.web.account.dto.AccountViewResponse;
import com.carddemo.web.account.dto.FieldValidationError;

/**
 * Java port of the account online functions {@code COACTVWC} (view) and {@code COACTUPC}
 * (update).
 *
 * <p><strong>View</strong> reproduces {@code COACTVWC}: validate the account id
 * ({@code 2210-EDIT-ACCOUNT}), then chain the reads {@code 9200-GETCARDXREF-BYACCT} →
 * {@code 9300-GETACCTDATA-BYACCT} → {@code 9400-GETCUSTDATA-BYCUST} to resolve the account
 * and its associated customer via the card cross-reference.</p>
 *
 * <p><strong>Update</strong> reproduces {@code COACTUPC}: validate the account id
 * ({@code 1210-EDIT-ACCOUNT}), read the same account/customer chain
 * ({@code 9000-READ-ACCT}), run every field edit ({@code 1200-EDIT-MAP-INPUTS} via
 * {@link AccountValidator}), detect the no-change case ({@code 1205-COMPARE-OLD-NEW}) and,
 * when changes are valid, persist them ({@code 9600-WRITE-PROCESSING}).</p>
 */
@Service
public class AccountService {

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountValidator validator;

    public AccountService(CardXrefRepository cardXrefRepository, AccountRepository accountRepository,
            CustomerRepository customerRepository, AccountValidator validator) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.validator = validator;
    }

    /** {@code COACTVWC} account view. */
    @Transactional(readOnly = true)
    public AccountViewResponse view(String acctId) {
        String normalized = validateAndNormalizeAcctId(acctId, false);
        Resolved resolved = readAccount(normalized);
        return toViewResponse(resolved.account(), resolved.customer());
    }

    /** {@code COACTUPC} account update. */
    @Transactional
    public AccountUpdateResponse update(String acctId, AccountUpdateRequest request) {
        String normalized = validateAndNormalizeAcctId(acctId, true);
        Resolved resolved = readAccount(normalized);
        Account account = resolved.account();
        Customer customer = resolved.customer();

        // 1205-COMPARE-OLD-NEW runs before the field edits: when the submitted values match
        // what was fetched the program short-circuits with the no-change notice and never
        // validates.
        if (!hasChanges(account, customer, request)) {
            return new AccountUpdateResponse(
                "No change detected with respect to values fetched.",
                toViewResponse(account, customer));
        }

        List<FieldValidationError> errors = validator.validate(request);
        if (!errors.isEmpty()) {
            throw new AccountInputException(errors.get(0).message(), errors);
        }

        applyChanges(account, customer, request);
        accountRepository.save(account);
        customerRepository.save(customer);
        return new AccountUpdateResponse(
            "Changes committed to database",
            toViewResponse(account, customer));
    }

    /**
     * {@code 1205-COMPARE-OLD-NEW}: report whether any submitted value differs from the
     * fetched record. Comparison is null-safe and does not mutate the entities; a value that
     * cannot be parsed into the stored form (e.g. a non-numeric amount) counts as a change so
     * the subsequent field edits get a chance to reject it.
     */
    private boolean hasChanges(Account account, Customer customer, AccountUpdateRequest r) {
        return strChanged(account.getAcctActiveStatus(), r.acctActiveStatus())
            || moneyChanged(account.getAcctCreditLimit(), r.creditLimit())
            || moneyChanged(account.getAcctCashCreditLimit(), r.cashCreditLimit())
            || moneyChanged(account.getAcctCurrBal(), r.currentBalance())
            || moneyChanged(account.getAcctCurrCycCredit(), r.currentCycleCredit())
            || moneyChanged(account.getAcctCurrCycDebit(), r.currentCycleDebit())
            || strChanged(account.getAcctOpenDate(), safeFormatDate(r.openYear(), r.openMonth(), r.openDay()))
            || strChanged(account.getAcctExpirationDate(),
                safeFormatDate(r.expiryYear(), r.expiryMonth(), r.expiryDay()))
            || strChanged(account.getAcctReissueDate(),
                safeFormatDate(r.reissueYear(), r.reissueMonth(), r.reissueDay()))
            || strChanged(account.getAcctGroupId(), r.accountGroupId())
            || strChanged(customer.getCustFirstName(), r.firstName())
            || strChanged(customer.getCustMiddleName(), r.middleName())
            || strChanged(customer.getCustLastName(), r.lastName())
            || strChanged(customer.getCustAddrLine1(), r.addressLine1())
            || strChanged(customer.getCustAddrLine2(), r.addressLine2())
            || strChanged(customer.getCustAddrLine3(), r.city())
            || strChanged(customer.getCustAddrStateCd(), r.stateCode())
            || strChanged(customer.getCustAddrZip(), r.zipCode())
            || strChanged(customer.getCustAddrCountryCd(), r.countryCode())
            || strChanged(customer.getCustPhoneNum1(),
                formatPhone(r.phone1Area(), r.phone1Prefix(), r.phone1Line()))
            || strChanged(customer.getCustPhoneNum2(),
                formatPhone(r.phone2Area(), r.phone2Prefix(), r.phone2Line()))
            || strChanged(customer.getCustSsn(), concatSsn(r))
            || strChanged(customer.getCustGovtIssuedId(), r.governmentIssuedId())
            || strChanged(customer.getCustDobYyyyMmDd(), safeFormatDate(r.dobYear(), r.dobMonth(), r.dobDay()))
            || strChanged(customer.getCustEftAccountId(), r.eftAccountId())
            || strChanged(customer.getCustPriCardHolderInd(), r.primaryCardHolderIndicator())
            || ficoChanged(customer.getCustFicoCreditScore(), r.ficoScore());
    }

    private static boolean strChanged(String current, String submitted) {
        return !Objects.equals(trim(current), trim(submitted));
    }

    private static boolean moneyChanged(BigDecimal current, String submitted) {
        BigDecimal parsed = FieldEditors.parseSigned9v2(submitted);
        if (parsed == null) {
            return true;
        }
        return current == null || current.compareTo(parsed) != 0;
    }

    private static boolean ficoChanged(Integer current, String submitted) {
        if (submitted == null || !submitted.trim().matches("\\d+")) {
            return true;
        }
        return !Objects.equals(current, Integer.valueOf(submitted.trim()));
    }

    private static String concatSsn(AccountUpdateRequest r) {
        return trim(r.ssnPart1()) + trim(r.ssnPart2()) + trim(r.ssnPart3());
    }

    private static String safeFormatDate(String year, String month, String day) {
        if (year == null || month == null || day == null
            || !year.trim().matches("\\d+") || !month.trim().matches("\\d+") || !day.trim().matches("\\d+")) {
            return null;
        }
        return formatDate(year, month, day);
    }

    // ---- account id edit (2210-EDIT-ACCOUNT / 1210-EDIT-ACCOUNT) ---------------------

    private String validateAndNormalizeAcctId(String acctId, boolean update) {
        String value = acctId == null ? "" : acctId.trim();
        if (value.equals("*")) {
            value = "";
        }
        if (value.isEmpty()) {
            throw new AccountInputException("Account number not provided");
        }
        boolean numeric = value.chars().allMatch(Character::isDigit);
        if (!numeric || value.length() > 11 || new BigDecimal(value).signum() == 0) {
            throw new AccountInputException(update
                ? "Account Number if supplied must be a 11 digit Non-Zero Number"
                : "Account Filter must  be a non-zero 11 digit number");
        }
        return String.format("%011d", Long.parseLong(value));
    }

    // ---- read chain (9000-READ-ACCT) -------------------------------------------------

    private Resolved readAccount(String acctId) {
        List<CardXref> xrefs = cardXrefRepository.findByXrefAcctId(acctId);
        if (xrefs.isEmpty()) {
            throw new AccountNotFoundException("Did not find this account in account card xref file");
        }
        Account account = accountRepository.findById(acctId)
            .orElseThrow(() -> new AccountNotFoundException(
                "Did not find this account in account master file"));
        String custId = xrefs.get(0).getXrefCustId();
        Customer customer = customerRepository.findById(custId)
            .orElseThrow(() -> new AccountNotFoundException(
                "Did not find associated customer in master file"));
        return new Resolved(account, customer);
    }

    // ---- apply changes (9600-WRITE-PROCESSING) ---------------------------------------

    private boolean applyChanges(Account account, Customer customer, AccountUpdateRequest r) {
        boolean changed = false;

        changed |= setIfChanged(account::getAcctActiveStatus, account::setAcctActiveStatus,
            trim(r.acctActiveStatus()));
        changed |= setMoneyIfChanged(account::getAcctCreditLimit, account::setAcctCreditLimit,
            AccountValidator.money(r.creditLimit()));
        changed |= setMoneyIfChanged(account::getAcctCashCreditLimit, account::setAcctCashCreditLimit,
            AccountValidator.money(r.cashCreditLimit()));
        changed |= setMoneyIfChanged(account::getAcctCurrBal, account::setAcctCurrBal,
            AccountValidator.money(r.currentBalance()));
        changed |= setMoneyIfChanged(account::getAcctCurrCycCredit, account::setAcctCurrCycCredit,
            AccountValidator.money(r.currentCycleCredit()));
        changed |= setMoneyIfChanged(account::getAcctCurrCycDebit, account::setAcctCurrCycDebit,
            AccountValidator.money(r.currentCycleDebit()));
        changed |= setIfChanged(account::getAcctOpenDate, account::setAcctOpenDate,
            formatDate(r.openYear(), r.openMonth(), r.openDay()));
        changed |= setIfChanged(account::getAcctExpirationDate, account::setAcctExpirationDate,
            formatDate(r.expiryYear(), r.expiryMonth(), r.expiryDay()));
        changed |= setIfChanged(account::getAcctReissueDate, account::setAcctReissueDate,
            formatDate(r.reissueYear(), r.reissueMonth(), r.reissueDay()));
        changed |= setIfChanged(account::getAcctGroupId, account::setAcctGroupId, trim(r.accountGroupId()));

        changed |= setIfChanged(customer::getCustFirstName, customer::setCustFirstName, trim(r.firstName()));
        changed |= setIfChanged(customer::getCustMiddleName, customer::setCustMiddleName, trim(r.middleName()));
        changed |= setIfChanged(customer::getCustLastName, customer::setCustLastName, trim(r.lastName()));
        changed |= setIfChanged(customer::getCustAddrLine1, customer::setCustAddrLine1, trim(r.addressLine1()));
        changed |= setIfChanged(customer::getCustAddrLine2, customer::setCustAddrLine2, trim(r.addressLine2()));
        changed |= setIfChanged(customer::getCustAddrLine3, customer::setCustAddrLine3, trim(r.city()));
        changed |= setIfChanged(customer::getCustAddrStateCd, customer::setCustAddrStateCd, trim(r.stateCode()));
        changed |= setIfChanged(customer::getCustAddrZip, customer::setCustAddrZip, trim(r.zipCode()));
        changed |= setIfChanged(customer::getCustAddrCountryCd, customer::setCustAddrCountryCd,
            trim(r.countryCode()));
        changed |= setIfChanged(customer::getCustPhoneNum1, customer::setCustPhoneNum1,
            formatPhone(r.phone1Area(), r.phone1Prefix(), r.phone1Line()));
        changed |= setIfChanged(customer::getCustPhoneNum2, customer::setCustPhoneNum2,
            formatPhone(r.phone2Area(), r.phone2Prefix(), r.phone2Line()));
        changed |= setIfChanged(customer::getCustSsn, customer::setCustSsn,
            trim(r.ssnPart1()) + trim(r.ssnPart2()) + trim(r.ssnPart3()));
        changed |= setIfChanged(customer::getCustGovtIssuedId, customer::setCustGovtIssuedId,
            trim(r.governmentIssuedId()));
        changed |= setIfChanged(customer::getCustDobYyyyMmDd, customer::setCustDobYyyyMmDd,
            formatDate(r.dobYear(), r.dobMonth(), r.dobDay()));
        changed |= setIfChanged(customer::getCustEftAccountId, customer::setCustEftAccountId,
            trim(r.eftAccountId()));
        changed |= setIfChanged(customer::getCustPriCardHolderInd, customer::setCustPriCardHolderInd,
            trim(r.primaryCardHolderIndicator()));

        Integer newFico = Integer.valueOf(r.ficoScore().trim());
        if (!Objects.equals(customer.getCustFicoCreditScore(), newFico)) {
            customer.setCustFicoCreditScore(newFico);
            changed = true;
        }
        return changed;
    }

    private static boolean setIfChanged(java.util.function.Supplier<String> getter,
            java.util.function.Consumer<String> setter, String newValue) {
        String current = getter.get();
        if (!Objects.equals(trim(current), newValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    private static boolean setMoneyIfChanged(java.util.function.Supplier<BigDecimal> getter,
            java.util.function.Consumer<BigDecimal> setter, BigDecimal newValue) {
        BigDecimal current = getter.get();
        if (current == null || newValue == null
            ? current != newValue
            : current.compareTo(newValue) != 0) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    // ---- mapping to the view response ------------------------------------------------

    private AccountViewResponse toViewResponse(Account account, Customer customer) {
        return new AccountViewResponse(
            account.getAcctId(),
            trim(account.getAcctActiveStatus()),
            account.getAcctCurrBal(),
            account.getAcctCreditLimit(),
            account.getAcctCashCreditLimit(),
            account.getAcctCurrCycCredit(),
            account.getAcctCurrCycDebit(),
            trim(account.getAcctOpenDate()),
            trim(account.getAcctExpirationDate()),
            trim(account.getAcctReissueDate()),
            trim(account.getAcctGroupId()),
            customer.getCustId(),
            trim(customer.getCustFirstName()),
            trim(customer.getCustMiddleName()),
            trim(customer.getCustLastName()),
            trim(customer.getCustAddrLine1()),
            trim(customer.getCustAddrLine2()),
            trim(customer.getCustAddrLine3()),
            trim(customer.getCustAddrStateCd()),
            trim(customer.getCustAddrZip()),
            trim(customer.getCustAddrCountryCd()),
            trim(customer.getCustPhoneNum1()),
            trim(customer.getCustPhoneNum2()),
            formatSsn(customer.getCustSsn()),
            trim(customer.getCustGovtIssuedId()),
            trim(customer.getCustDobYyyyMmDd()),
            trim(customer.getCustEftAccountId()),
            trim(customer.getCustPriCardHolderInd()),
            customer.getCustFicoCreditScore());
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /** Format the three validated component fields into the stored {@code yyyy-MM-dd} form. */
    private static String formatDate(String year, String month, String day) {
        return String.format("%04d-%02d-%02d",
            Integer.parseInt(year.trim()),
            Integer.parseInt(month.trim()),
            Integer.parseInt(day.trim()));
    }

    /** Build the stored {@code (999)999-9999} phone string, or blank when no parts supplied. */
    private static String formatPhone(String area, String prefix, String line) {
        boolean allBlank = isBlank(area) && isBlank(prefix) && isBlank(line);
        if (allBlank) {
            return "";
        }
        return "(" + area.trim() + ")" + prefix.trim() + "-" + line.trim();
    }

    /** Format the stored 9-digit SSN as {@code 999-99-9999} for display. */
    private static String formatSsn(String ssn) {
        if (ssn == null) {
            return null;
        }
        String digits = ssn.trim();
        if (digits.length() != 9) {
            return digits;
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record Resolved(Account account, Customer customer) {
    }
}
