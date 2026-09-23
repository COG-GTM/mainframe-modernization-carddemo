package com.carddemo.service;

import com.carddemo.exception.BusinessRuleException;
import com.carddemo.util.CobolDateValidator;
import com.carddemo.util.LookupTables;
import com.carddemo.web.dto.AccountUpdateRequest;
import com.carddemo.web.dto.CustomerView;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Field edits of COACTUPC (paragraphs 1200-EDIT-MAP-INPUTS and its 1200-x sub paragraphs).
 *
 * <p>The COBOL program collected every failure and displayed the first one; the messages below are
 * verbatim copies so the modernized API stays behaviour compatible.
 */
@Component
public class AccountUpdateValidator {

    private static final Pattern ALPHABETIC = Pattern.compile("[A-Za-z ]+");
    private static final Pattern NUMERIC = Pattern.compile("\\d+");
    private static final Pattern SSN_DIGITS = Pattern.compile("\\d{9}");
    private static final Pattern PHONE = Pattern.compile("\\(?(\\d{3})\\)?[ -]?(\\d{3})[ -]?(\\d{4})");

    public void validate(AccountUpdateRequest request) {
        validateAccountFields(request);
        validateCustomerFields(request.customer());
    }

    private void validateAccountFields(AccountUpdateRequest request) {
        String status = trim(request.activeStatus());
        if (status.isEmpty()) {
            throw new BusinessRuleException("Account Active Status must be supplied");
        }
        if (!status.equalsIgnoreCase("Y") && !status.equalsIgnoreCase("N")) {
            throw new BusinessRuleException("Account Active Status must be Y or N");
        }

        requireAmount(request.creditLimit(), "Credit Limit");
        requireAmount(request.cashCreditLimit(), "Cash Credit Limit");
        requireAmount(request.currentBalance(), "Current Balance");
        requireAmount(request.currentCycleCredit(), "Current Cycle Credit");
        requireAmount(request.currentCycleDebit(), "Current Cycle Debit");

        requireDate(request.openDate(), "Account Open Date");
        requireDate(request.expirationDate(), "Account Expiry Date");
        requireDate(request.reissueDate(), "Account Reissue Date");
    }

    private void validateCustomerFields(CustomerView customer) {
        if (customer == null) {
            throw new BusinessRuleException("Customer details must be supplied");
        }

        requireAlphabetic(customer.firstName(), "First Name");
        requireAlphabetic(customer.lastName(), "Last Name");
        if (notBlank(customer.middleName()) && !ALPHABETIC.matcher(customer.middleName()).matches()) {
            throw new BusinessRuleException("Middle Name can only contain alphabets and spaces");
        }

        requireSupplied(customer.addressLine1(), "Address Line 1");
        requireSupplied(customer.addressLine3(), "City");
        requireSupplied(customer.countryCode(), "Country");
        requireSupplied(customer.eftAccountId(), "EFT Account Id");

        String state = trim(customer.stateCode());
        if (state.isEmpty()) {
            throw new BusinessRuleException("State must be supplied");
        }
        if (!LookupTables.isValidStateCode(state)) {
            throw new BusinessRuleException("Invalid State Code");
        }

        String zip = trim(customer.zipCode());
        if (zip.isEmpty()) {
            throw new BusinessRuleException("Zip code must be supplied");
        }
        if (!NUMERIC.matcher(zip).matches()) {
            throw new BusinessRuleException("Zip code must be numeric");
        }
        if (!LookupTables.isValidStateZipCombination(state, zip)) {
            throw new BusinessRuleException("Invalid Zip Code for the State");
        }

        requirePhone(customer.phoneNumber1(), "Phone Number 1");
        if (notBlank(customer.phoneNumber2())) {
            requirePhone(customer.phoneNumber2(), "Phone Number 2");
        }

        if (customer.ssn() == null || !SSN_DIGITS.matcher(String.format("%09d", customer.ssn())).matches()) {
            throw new BusinessRuleException("SSN must be a 9 digit number");
        }

        requireDate(customer.dateOfBirth(), "Date of Birth");

        Integer fico = customer.ficoCreditScore();
        if (fico == null || fico < 300 || fico > 850) {
            throw new BusinessRuleException("FICO Score should be between 300 and 850");
        }

        String primary = trim(customer.primaryCardHolderIndicator());
        if (!primary.isEmpty() && !primary.equalsIgnoreCase("Y") && !primary.equalsIgnoreCase("N")) {
            throw new BusinessRuleException("Primary Card Holder Indicator must be Y or N");
        }
    }

    private void requirePhone(String phone, String label) {
        String value = trim(phone);
        if (value.isEmpty()) {
            throw new BusinessRuleException(label + " must be supplied");
        }
        var matcher = PHONE.matcher(value);
        if (!matcher.matches()) {
            throw new BusinessRuleException(label + " must be a valid US phone number");
        }
        if (!LookupTables.isValidPhoneAreaCode(matcher.group(1))) {
            throw new BusinessRuleException(label + " area code is invalid");
        }
    }

    private void requireAlphabetic(String value, String label) {
        if (!notBlank(value)) {
            throw new BusinessRuleException(label + " must be supplied");
        }
        if (!ALPHABETIC.matcher(value).matches()) {
            throw new BusinessRuleException(label + " can only contain alphabets and spaces");
        }
    }

    private void requireSupplied(String value, String label) {
        if (!notBlank(value)) {
            throw new BusinessRuleException(label + " must be supplied");
        }
    }

    private void requireAmount(BigDecimal amount, String label) {
        if (amount == null) {
            throw new BusinessRuleException(label + " must be supplied");
        }
        if (amount.scale() > 2 || amount.precision() - amount.scale() > 10) {
            throw new BusinessRuleException(label + " is not valid");
        }
    }

    private void requireDate(String date, String label) {
        if (!notBlank(date)) {
            throw new BusinessRuleException(label + " must be supplied");
        }
        CobolDateValidator.Result result = CobolDateValidator.validate(date, "YYYY-MM-DD");
        if (!result.isValid()) {
            throw new BusinessRuleException(label + " is not a valid date: " + result.message());
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
