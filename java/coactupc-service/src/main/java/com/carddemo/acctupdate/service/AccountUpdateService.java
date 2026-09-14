package com.carddemo.acctupdate.service;

import com.carddemo.acctupdate.api.AccountUpdateDetails;
import com.carddemo.acctupdate.api.AccountUpdateRequest;
import com.carddemo.acctupdate.api.AccountUpdateResponse;
import com.carddemo.acctupdate.api.ChangeAction;
import com.carddemo.acctupdate.api.FieldFlag;
import com.carddemo.acctupdate.api.Messages;
import com.carddemo.acctupdate.domain.AccountRecord;
import com.carddemo.acctupdate.domain.CardXrefRecord;
import com.carddemo.acctupdate.domain.CustomerRecord;
import com.carddemo.acctupdate.repository.AccountRepository;
import com.carddemo.acctupdate.repository.CardXrefRepository;
import com.carddemo.acctupdate.repository.CustomerRepository;
import com.carddemo.acctupdate.service.edit.CobolNumeric;
import com.carddemo.acctupdate.service.edit.DateFlags;
import com.carddemo.acctupdate.service.edit.EditContext;
import com.carddemo.acctupdate.service.edit.FieldEditor;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class AccountUpdateService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\((\\d{3})\\)(\\d{3})-(\\d{4})$");

    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final CardXrefRepository xrefs;
    private final FieldEditor editor;

    public AccountUpdateService(
        AccountRepository accounts,
        CustomerRepository customers,
        CardXrefRepository xrefs,
        Clock clock
    ) {
        this.accounts = accounts;
        this.customers = customers;
        this.xrefs = xrefs;
        this.editor = new FieldEditor(clock);
    }

    public AccountUpdateResponse readAccount(String acctIdText) {
        AccountUpdateResponse response = new AccountUpdateResponse();
        if (acctIdText == null || acctIdText.trim().isEmpty()) {
            return error(response, Messages.ACCOUNT_NOT_PROVIDED, ChangeAction.DETAILS_NOT_FETCHED);
        }
        if (!acctIdText.matches("\\d{11}") || acctIdText.matches("0{11}")) {
            return error(response, Messages.ACCOUNT_INVALID, ChangeAction.DETAILS_NOT_FETCHED);
        }

        Long accountId = Long.valueOf(acctIdText);
        Optional<CardXrefRecord> xref = xrefs.findFirstByAcctId(accountId);
        if (!xref.isPresent()) {
            return error(response, Messages.XREF_NOT_FOUND, ChangeAction.DETAILS_NOT_FETCHED);
        }

        Optional<AccountRecord> account = accounts.findById(accountId);
        if (!account.isPresent()) {
            return error(response, Messages.ACCOUNT_NOT_FOUND, ChangeAction.DETAILS_NOT_FETCHED);
        }

        Optional<CustomerRecord> customer = customers.findById(xref.get().getCustId());
        if (!customer.isPresent()) {
            return error(response, Messages.CUSTOMER_NOT_FOUND, ChangeAction.DETAILS_NOT_FETCHED);
        }

        response.setAction(ChangeAction.SHOW_DETAILS);
        response.setInfoMessage(Messages.DETAILS_SHOWN);
        response.setDetails(from(account.get(), customer.get(), xref.get().getCardNum()));
        return response;
    }

    public AccountUpdateResponse editMapInputs(AccountUpdateRequest request) {
        AccountUpdateDetails original = request == null ? null : request.getOriginal();
        AccountUpdateDetails updated = request == null ? null : request.getUpdated();
        AccountUpdateResponse response = new AccountUpdateResponse();

        if (original == null || updated == null) {
            return error(response, Messages.NO_CHANGE, ChangeAction.SHOW_DETAILS);
        }
        if (compareOldNew(original, updated) == CompareResult.NO_CHANGES_FOUND) {
            return error(response, Messages.NO_CHANGE, ChangeAction.SHOW_DETAILS);
        }

        EditContext context = new EditContext();
        editor.editYesNo("Account Status", updated.getActiveStatus(), context);
        editor.editDateCcyymmdd(
            "Open Date",
            updated.getOpenYear(),
            updated.getOpenMon(),
            updated.getOpenDay(),
            context
        );
        editor.editSigned9V2("Credit Limit", updated.getCreditLimit(), context);
        editor.editDateCcyymmdd(
            "Expiry Date",
            updated.getExpYear(),
            updated.getExpMon(),
            updated.getExpDay(),
            context
        );
        editor.editSigned9V2("Cash Credit Limit", updated.getCashCreditLimit(), context);
        editor.editDateCcyymmdd(
            "Reissue Date",
            updated.getReissueYear(),
            updated.getReissueMon(),
            updated.getReissueDay(),
            context
        );
        editor.editSigned9V2("Current Balance", updated.getCurrBal(), context);
        editor.editSigned9V2("Current Cycle Credit Limit", updated.getCurrCycCredit(), context);
        editor.editSigned9V2("Current Cycle Debit Limit", updated.getCurrCycDebit(), context);

        FieldFlag ssnPart1 = editor.editNumReqd(
            "SSN: First 3 chars",
            updated.getSsn1(),
            3,
            context
        );
        if (ssnPart1 == FieldFlag.VALID) {
            int part1 = Integer.parseInt(updated.getSsn1());
            if (part1 == 0 || part1 == 666 || part1 >= 900) {
                context.flag(
                    "SSN: First 3 chars",
                    FieldFlag.NOT_OK,
                    "SSN: First 3 chars: should not be 000, 666, or between 900 and 999"
                );
            }
        }
        editor.editNumReqd("SSN 4th & 5th chars", updated.getSsn2(), 2, context);
        editor.editNumReqd("SSN Last 4 chars", updated.getSsn3(), 4, context);

        DateFlags dateOfBirth = editor.editDateCcyymmdd(
            "Date of Birth",
            updated.getDobYear(),
            updated.getDobMon(),
            updated.getDobDay(),
            context
        );
        if (dateOfBirth.isValid()) {
            editor.editDateOfBirth(
                "Date of Birth",
                updated.getDobYear(),
                updated.getDobMon(),
                updated.getDobDay(),
                context
            );
        }

        FieldFlag fico = editor.editNumReqd("FICO Score", updated.getFicoScore(), 3, context);
        if (fico == FieldFlag.VALID) {
            int score = Integer.parseInt(updated.getFicoScore());
            if (score < 300 || score > 850) {
                context.flag(
                    "FICO Score",
                    FieldFlag.NOT_OK,
                    "FICO Score: should be between 300 and 850"
                );
            }
        }

        editor.editAlphaReqd("First Name", updated.getFirstName(), context);
        editor.editAlphaOpt("Middle Name", updated.getMiddleName(), context);
        editor.editAlphaReqd("Last Name", updated.getLastName(), context);
        editor.editMandatory("Address Line 1", updated.getAddrLine1(), context);

        FieldFlag state = editor.editAlphaReqd("State", updated.getAddrStateCd(), context);
        if (state == FieldFlag.VALID && !editor.stateValid(updated.getAddrStateCd())) {
            context.flag("State", FieldFlag.NOT_OK, "State: is not a valid state code");
        }
        FieldFlag zip = editor.editNumReqd("Zip", updated.getAddrZip(), 5, context);
        editor.editAlphaReqd("City", updated.getAddrLine3(), context);
        editor.editAlphaReqd("Country", updated.getAddrCountryCd(), context);
        editPhone("Phone 1", updated.getPhone1A(), updated.getPhone1B(), updated.getPhone1C(), context);
        editPhone("Phone 2", updated.getPhone2A(), updated.getPhone2B(), updated.getPhone2C(), context);
        editor.editNumReqd("EFT Account Id", updated.getEftAccountId(), 10, context);
        editor.editYesNo("Primary Card Holder", updated.getPriHolderInd(), context);

        if (state == FieldFlag.VALID
            && zip == FieldFlag.VALID
            && !editor.zipStateValid(updated.getAddrStateCd(), updated.getAddrZip())) {
            context.flag("State", FieldFlag.NOT_OK, Messages.INVALID_ZIP);
            context.flag("Zip", FieldFlag.NOT_OK, Messages.INVALID_ZIP);
        }

        response.setAction(
            context.isInputError()
                ? ChangeAction.CHANGES_NOT_OK
                : ChangeAction.CHANGES_OK_NOT_CONFIRMED
        );
        response.setInfoMessage(
            context.isInputError() ? Messages.UPDATE_PRESENTED : Messages.CHANGES_VALIDATED
        );
        response.setErrorMessage(context.getReturnMsg());
        response.setFieldFlags(context.getFieldFlags());
        response.setDetails(updated);
        return response;
    }

    private void editPhone(
        String label,
        String area,
        String prefix,
        String line,
        EditContext context
    ) {
        if (blank(area) && blank(prefix) && blank(line)) {
            context.flag(label + ".A", FieldFlag.VALID, null);
            context.flag(label + ".B", FieldFlag.VALID, null);
            context.flag(label + ".C", FieldFlag.VALID, null);
            return;
        }

        editor.editPhonePart(label + ".A", area, 3, true, context);
        editor.editPhonePart(label + ".B", prefix, 3, false, context);
        editor.editPhonePart(label + ".C", line, 4, false, context);
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public CompareResult compareOldNew(AccountUpdateDetails original, AccountUpdateDetails updated) {
        if (original == null || updated == null) {
            return CompareResult.CHANGE_HAS_OCCURRED;
        }

        if (!eq(original.getAcctId(), updated.getAcctId())
            || !sameNormalized(original.getActiveStatus(), updated.getActiveStatus())
            || !moneyEq(original.getCurrBal(), updated.getCurrBal())
            || !moneyEq(original.getCreditLimit(), updated.getCreditLimit())
            || !moneyEq(original.getCashCreditLimit(), updated.getCashCreditLimit())
            || !moneyEq(original.getCurrCycCredit(), updated.getCurrCycCredit())
            || !moneyEq(original.getCurrCycDebit(), updated.getCurrCycDebit())
            || !eq(original.getOpenYear(), updated.getOpenYear())
            || !eq(original.getOpenMon(), updated.getOpenMon())
            || !eq(original.getOpenDay(), updated.getOpenDay())
            || !eq(original.getExpYear(), updated.getExpYear())
            || !eq(original.getExpMon(), updated.getExpMon())
            || !eq(original.getExpDay(), updated.getExpDay())
            || !eq(original.getReissueYear(), updated.getReissueYear())
            || !eq(original.getReissueMon(), updated.getReissueMon())
            || !eq(original.getReissueDay(), updated.getReissueDay())
            || !sameNormalized(original.getGroupId(), updated.getGroupId())
            || !sameNormalized(original.getCustId(), updated.getCustId())
            || !eq(original.getSsn1(), updated.getSsn1())
            || !eq(original.getSsn2(), updated.getSsn2())
            || !eq(original.getSsn3(), updated.getSsn3())
            || !eq(original.getDobYear(), updated.getDobYear())
            || !eq(original.getDobMon(), updated.getDobMon())
            || !eq(original.getDobDay(), updated.getDobDay())
            || !eq(original.getFicoScore(), updated.getFicoScore())
            || !eq(original.getPhone1A(), updated.getPhone1A())
            || !eq(original.getPhone1B(), updated.getPhone1B())
            || !eq(original.getPhone1C(), updated.getPhone1C())
            || !eq(original.getPhone2A(), updated.getPhone2A())
            || !eq(original.getPhone2B(), updated.getPhone2B())
            || !eq(original.getPhone2C(), updated.getPhone2C())
            || !sameNormalized(original.getFirstName(), updated.getFirstName())
            || !sameNormalized(original.getMiddleName(), updated.getMiddleName())
            || !sameNormalized(original.getLastName(), updated.getLastName())
            || !sameNormalized(original.getAddrLine1(), updated.getAddrLine1())
            || !sameNormalized(original.getAddrLine2(), updated.getAddrLine2())
            || !sameNormalized(original.getAddrLine3(), updated.getAddrLine3())
            || !sameNormalized(original.getAddrStateCd(), updated.getAddrStateCd())
            || !sameNormalized(original.getAddrCountryCd(), updated.getAddrCountryCd())
            || !sameNormalized(original.getAddrZip(), updated.getAddrZip())
            || !sameNormalized(original.getGovtIssuedId(), updated.getGovtIssuedId())
            || !sameNormalized(original.getEftAccountId(), updated.getEftAccountId())
            || !sameNormalized(original.getPriHolderInd(), updated.getPriHolderInd())) {
            return CompareResult.CHANGE_HAS_OCCURRED;
        }
        return CompareResult.NO_CHANGES_FOUND;
    }

    public enum CompareResult {
        NO_CHANGES_FOUND,
        CHANGE_HAS_OCCURRED
    }

    @Transactional
    public AccountUpdateResponse writeProcessing(AccountUpdateRequest request) {
        AccountUpdateResponse validation = editMapInputs(request);
        if (validation.getAction() != ChangeAction.CHANGES_OK_NOT_CONFIRMED) {
            return validation;
        }

        AccountUpdateDetails original = request.getOriginal();
        AccountUpdateDetails updated = request.getUpdated();
        Long accountId = Long.valueOf(original.getAcctId());
        Long customerId = Long.valueOf(original.getCustId());

        Optional<AccountRecord> account;
        Optional<CustomerRecord> customer;
        try {
            account = accounts.findByIdForUpdate(accountId);
        } catch (RuntimeException exception) {
            return error(
                new AccountUpdateResponse(),
                Messages.LOCK_ACCOUNT,
                ChangeAction.CHANGES_OKAYED_LOCK_ERROR
            );
        }
        if (!account.isPresent()) {
            return error(
                new AccountUpdateResponse(),
                Messages.LOCK_ACCOUNT,
                ChangeAction.CHANGES_OKAYED_LOCK_ERROR
            );
        }

        try {
            customer = customers.findByIdForUpdate(customerId);
        } catch (RuntimeException exception) {
            return error(
                new AccountUpdateResponse(),
                Messages.LOCK_CUSTOMER,
                ChangeAction.CHANGES_OKAYED_LOCK_ERROR
            );
        }
        if (!customer.isPresent()) {
            return error(
                new AccountUpdateResponse(),
                Messages.LOCK_CUSTOMER,
                ChangeAction.CHANGES_OKAYED_LOCK_ERROR
            );
        }

        if (!accountUnchanged(account.get(), original)
            || !customerUnchanged(customer.get(), original)) {
            return error(
                new AccountUpdateResponse(),
                Messages.DATA_CHANGED,
                ChangeAction.SHOW_DETAILS
            );
        }

        try {
            applyAccountUpdate(account.get(), updated);
            applyCustomerUpdate(customer.get(), updated);
            accounts.saveAndFlush(account.get());
            customers.saveAndFlush(customer.get());

            AccountUpdateResponse response = new AccountUpdateResponse();
            response.setAction(ChangeAction.CHANGES_OKAYED_AND_DONE);
            response.setInfoMessage(Messages.COMMITTED);
            response.setDetails(from(account.get(), customer.get(), original.getCardNum()));
            return response;
        } catch (RuntimeException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return error(
                new AccountUpdateResponse(),
                Messages.UPDATE_FAILED,
                ChangeAction.CHANGES_OKAYED_BUT_FAILED
            );
        }
    }

    private boolean accountUnchanged(AccountRecord account, AccountUpdateDetails original) {
        return eq(account.getActiveStatus(), original.getActiveStatus())
            && moneyEq(account.getCurrBal(), original.getCurrBal())
            && moneyEq(account.getCreditLimit(), original.getCreditLimit())
            && moneyEq(account.getCashCreditLimit(), original.getCashCreditLimit())
            && moneyEq(account.getCurrCycCredit(), original.getCurrCycCredit())
            && moneyEq(account.getCurrCycDebit(), original.getCurrCycDebit())
            && dateParts(account.getOpenDate(), original.getOpenYear(), original.getOpenMon(), original.getOpenDay())
            && dateParts(
                account.getExpirationDate(),
                original.getExpYear(),
                original.getExpMon(),
                original.getExpDay()
            )
            && dateParts(
                account.getReissueDate(),
                original.getReissueYear(),
                original.getReissueMon(),
                original.getReissueDay()
            )
            && sameNormalized(account.getGroupId(), original.getGroupId());
    }

    private boolean customerUnchanged(CustomerRecord customer, AccountUpdateDetails original) {
        String ssn = customer.getSsn() == null ? null : String.format("%09d", customer.getSsn());
        String fico = customer.getFicoCreditScore() == null
            ? null
            : String.format("%03d", customer.getFicoCreditScore());

        return sameNormalized(customer.getFirstName(), original.getFirstName())
            && sameNormalized(customer.getMiddleName(), original.getMiddleName())
            && sameNormalized(customer.getLastName(), original.getLastName())
            && sameNormalized(customer.getAddrLine1(), original.getAddrLine1())
            && sameNormalized(customer.getAddrLine2(), original.getAddrLine2())
            && sameNormalized(customer.getAddrLine3(), original.getAddrLine3())
            && sameNormalized(customer.getAddrStateCd(), original.getAddrStateCd())
            && sameNormalized(customer.getAddrCountryCd(), original.getAddrCountryCd())
            && eq(customer.getAddrZip(), original.getAddrZip())
            && eq(
                customer.getPhoneNum1(),
                phoneSnapshot(original.getPhone1A(), original.getPhone1B(), original.getPhone1C())
            )
            && eq(
                customer.getPhoneNum2(),
                phoneSnapshot(original.getPhone2A(), original.getPhone2B(), original.getPhone2C())
            )
            && eq(ssn, original.getSsn1() + original.getSsn2() + original.getSsn3())
            && sameNormalized(customer.getGovtIssuedId(), original.getGovtIssuedId())
            && dateParts(customer.getDobYyyyMmDd(), original.getDobYear(), original.getDobMon(), original.getDobDay())
            && eq(customer.getEftAccountId(), original.getEftAccountId())
            && eq(customer.getPriCardHolderInd(), original.getPriHolderInd())
            && eq(fico, original.getFicoScore());
    }

    private void applyAccountUpdate(AccountRecord account, AccountUpdateDetails updated) {
        account.setActiveStatus(updated.getActiveStatus());
        account.setCurrBal(CobolNumeric.numvalC(updated.getCurrBal()));
        account.setCreditLimit(CobolNumeric.numvalC(updated.getCreditLimit()));
        account.setCashCreditLimit(CobolNumeric.numvalC(updated.getCashCreditLimit()));
        account.setCurrCycCredit(CobolNumeric.numvalC(updated.getCurrCycCredit()));
        account.setCurrCycDebit(CobolNumeric.numvalC(updated.getCurrCycDebit()));
        account.setOpenDate(date(updated.getOpenYear(), updated.getOpenMon(), updated.getOpenDay()));
        account.setExpirationDate(date(updated.getExpYear(), updated.getExpMon(), updated.getExpDay()));
        account.setReissueDate(date(updated.getReissueYear(), updated.getReissueMon(), updated.getReissueDay()));
        account.setGroupId(updated.getGroupId());
    }

    private void applyCustomerUpdate(CustomerRecord customer, AccountUpdateDetails updated) {
        customer.setFirstName(updated.getFirstName());
        customer.setMiddleName(updated.getMiddleName());
        customer.setLastName(updated.getLastName());
        customer.setAddrLine1(updated.getAddrLine1());
        customer.setAddrLine2(updated.getAddrLine2());
        customer.setAddrLine3(updated.getAddrLine3());
        customer.setAddrStateCd(updated.getAddrStateCd());
        customer.setAddrCountryCd(updated.getAddrCountryCd());
        customer.setAddrZip(updated.getAddrZip());
        customer.setPhoneNum1(phone(updated.getPhone1A(), updated.getPhone1B(), updated.getPhone1C()));
        customer.setPhoneNum2(phone(updated.getPhone2A(), updated.getPhone2B(), updated.getPhone2C()));
        customer.setSsn(Long.valueOf(updated.getSsn1() + updated.getSsn2() + updated.getSsn3()));
        customer.setGovtIssuedId(updated.getGovtIssuedId());
        customer.setDobYyyyMmDd(date(updated.getDobYear(), updated.getDobMon(), updated.getDobDay()));
        customer.setEftAccountId(updated.getEftAccountId());
        customer.setPriCardHolderInd(updated.getPriHolderInd());
        customer.setFicoCreditScore(Integer.valueOf(updated.getFicoScore()));
    }

    private boolean dateParts(String date, String year, String month, String day) {
        return date != null
            && date.length() >= 10
            && eq(date.substring(0, 4), year)
            && eq(date.substring(5, 7), month)
            && eq(date.substring(8, 10), day);
    }

    private String date(String year, String month, String day) {
        return year + "-" + month + "-" + day;
    }

    private String phone(String area, String prefix, String line) {
        return "(" + area + ")" + prefix + "-" + line;
    }

    private String phoneSnapshot(String area, String prefix, String line) {
        if (blank(area) && blank(prefix) && blank(line)) {
            return "";
        }
        return phone(area, prefix, line);
    }

    private AccountUpdateResponse error(
        AccountUpdateResponse response,
        String message,
        ChangeAction action
    ) {
        response.setAction(action);
        response.setInfoMessage(infoMessage(action));
        response.setErrorMessage(message);
        return response;
    }

    private String infoMessage(ChangeAction action) {
        if (action == ChangeAction.DETAILS_NOT_FETCHED) {
            return Messages.ENTER_ACCOUNT;
        }
        if (action == ChangeAction.SHOW_DETAILS || action == ChangeAction.CHANGES_NOT_OK) {
            return Messages.UPDATE_PRESENTED;
        }
        if (action == ChangeAction.CHANGES_OKAYED_LOCK_ERROR
            || action == ChangeAction.CHANGES_OKAYED_BUT_FAILED) {
            return Messages.UNSUCCESSFUL;
        }
        return null;
    }

    public AccountUpdateDetails from(AccountRecord account, CustomerRecord customer, String cardNum) {
        AccountUpdateDetails details = new AccountUpdateDetails();
        details.setAcctId(String.format("%011d", account.getAcctId()));
        details.setActiveStatus(account.getActiveStatus());
        details.setCurrBal(money(account.getCurrBal()));
        details.setCreditLimit(money(account.getCreditLimit()));
        details.setCashCreditLimit(money(account.getCashCreditLimit()));
        details.setCurrCycCredit(money(account.getCurrCycCredit()));
        details.setCurrCycDebit(money(account.getCurrCycDebit()));
        splitDate(account.getOpenDate(), details::setOpenYear, details::setOpenMon, details::setOpenDay);
        splitDate(
            account.getExpirationDate(),
            details::setExpYear,
            details::setExpMon,
            details::setExpDay
        );
        splitDate(
            account.getReissueDate(),
            details::setReissueYear,
            details::setReissueMon,
            details::setReissueDay
        );
        details.setGroupId(account.getGroupId());
        details.setCustId(String.format("%09d", customer.getCustId()));
        details.setFirstName(customer.getFirstName());
        details.setMiddleName(customer.getMiddleName());
        details.setLastName(customer.getLastName());
        details.setAddrLine1(customer.getAddrLine1());
        details.setAddrLine2(customer.getAddrLine2());
        details.setAddrLine3(customer.getAddrLine3());
        details.setAddrStateCd(customer.getAddrStateCd());
        details.setAddrCountryCd(customer.getAddrCountryCd());
        details.setAddrZip(customer.getAddrZip());

        PhoneParts phone1 = phoneParts(customer.getPhoneNum1());
        details.setPhone1A(phone1.a);
        details.setPhone1B(phone1.b);
        details.setPhone1C(phone1.c);
        PhoneParts phone2 = phoneParts(customer.getPhoneNum2());
        details.setPhone2A(phone2.a);
        details.setPhone2B(phone2.b);
        details.setPhone2C(phone2.c);

        String ssn = String.format("%09d", customer.getSsn());
        details.setSsn1(ssn.substring(0, 3));
        details.setSsn2(ssn.substring(3, 5));
        details.setSsn3(ssn.substring(5));
        details.setGovtIssuedId(customer.getGovtIssuedId());
        splitDate(
            customer.getDobYyyyMmDd(),
            details::setDobYear,
            details::setDobMon,
            details::setDobDay
        );
        details.setEftAccountId(customer.getEftAccountId());
        details.setPriHolderInd(customer.getPriCardHolderInd());
        details.setFicoScore(String.format("%03d", customer.getFicoCreditScore()));
        details.setCardNum(cardNum);
        return details;
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2).toPlainString();
    }

    private void splitDate(
        String value,
        Consumer<String> year,
        Consumer<String> month,
        Consumer<String> day
    ) {
        if (value != null && value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            year.accept(value.substring(0, 4));
            month.accept(value.substring(5, 7));
            day.accept(value.substring(8, 10));
        }
    }

    private PhoneParts phoneParts(String value) {
        Matcher matcher = PHONE_PATTERN.matcher(value == null ? "" : value);
        if (!matcher.matches()) {
            return new PhoneParts(value == null ? "" : value, "", "");
        }
        return new PhoneParts(matcher.group(1), matcher.group(2), matcher.group(3));
    }

    private boolean eq(String first, String second) {
        String normalizedFirst = first == null || first.isEmpty() ? " " : first;
        String normalizedSecond = second == null || second.isEmpty() ? " " : second;
        return Objects.equals(normalizedFirst, normalizedSecond);
    }

    private boolean sameNormalized(String first, String second) {
        return normalize(first).equals(normalize(second));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private boolean moneyEq(String first, String second) {
        try {
            return CobolNumeric.numvalC(first).compareTo(CobolNumeric.numvalC(second)) == 0;
        } catch (RuntimeException exception) {
            return eq(first, second);
        }
    }

    private boolean moneyEq(BigDecimal value, String snapshot) {
        return moneyEq(value == null ? "0.00" : value.toPlainString(), snapshot);
    }

    private static class PhoneParts {
        private final String a;
        private final String b;
        private final String c;

        private PhoneParts(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
