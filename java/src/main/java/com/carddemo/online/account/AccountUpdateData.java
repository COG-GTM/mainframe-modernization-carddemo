package com.carddemo.online.account;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.Customer;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COACTUPC — ACUP-OLD-DETAILS / ACUP-NEW-DETAILS of WS-THIS-PROGCOMMAREA
 * (account data from CVACT01Y, customer data from CVCUS01Y), as presented by BMS map
 * CACTUPA of mapset COACTUP.
 *
 * <p>All fields are kept as the screen keeps them (character strings, dates and phone
 * numbers split into their map parts) so that the COBOL edits and the old/new
 * comparison of 1205-COMPARE-OLD-NEW can be reproduced literally.</p>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateData {

    private String accountId;
    private String activeStatus;
    private String currentBalance;
    private String creditLimit;
    private String cashCreditLimit;
    private String currentCycleCredit;
    private String currentCycleDebit;
    private String openYear;
    private String openMonth;
    private String openDay;
    private String expiryYear;
    private String expiryMonth;
    private String expiryDay;
    private String reissueYear;
    private String reissueMonth;
    private String reissueDay;
    private String groupId;

    private String customerId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateCode;
    private String countryCode;
    private String zipCode;
    private String phone1Area;
    private String phone1Prefix;
    private String phone1Line;
    private String phone2Area;
    private String phone2Prefix;
    private String phone2Line;
    private String ssnPart1;
    private String ssnPart2;
    private String ssnPart3;
    private String governmentIssuedId;
    private String dobYear;
    private String dobMonth;
    private String dobDay;
    private String eftAccountId;
    private String primaryCardHolder;
    private String ficoScore;

    /** 1000-SEND-MAP / 9000-READ-ACCT: fills ACUP-OLD-DETAILS from the two master records. */
    public static AccountUpdateData fromEntities(Account account, Customer customer) {
        return AccountUpdateData.builder()
                .accountId(account.getAccountId() == null ? null : String.format("%011d", account.getAccountId()))
                .activeStatus(account.getActiveStatus())
                .currentBalance(amount(account.getCurrentBalance()))
                .creditLimit(amount(account.getCreditLimit()))
                .cashCreditLimit(amount(account.getCashCreditLimit()))
                .currentCycleCredit(amount(account.getCurrentCycleCredit()))
                .currentCycleDebit(amount(account.getCurrentCycleDebit()))
                .openYear(datePart(account.getOpenDate(), 0))
                .openMonth(datePart(account.getOpenDate(), 1))
                .openDay(datePart(account.getOpenDate(), 2))
                .expiryYear(datePart(account.getExpirationDate(), 0))
                .expiryMonth(datePart(account.getExpirationDate(), 1))
                .expiryDay(datePart(account.getExpirationDate(), 2))
                .reissueYear(datePart(account.getReissueDate(), 0))
                .reissueMonth(datePart(account.getReissueDate(), 1))
                .reissueDay(datePart(account.getReissueDate(), 2))
                .groupId(account.getGroupId())
                .customerId(customer.getCustomerId() == null ? null : String.format("%09d", customer.getCustomerId()))
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .city(customer.getAddressLine3())
                .stateCode(customer.getStateCode())
                .countryCode(customer.getCountryCode())
                .zipCode(customer.getZipCode())
                .phone1Area(phonePart(customer.getPhoneNumber1(), 0))
                .phone1Prefix(phonePart(customer.getPhoneNumber1(), 1))
                .phone1Line(phonePart(customer.getPhoneNumber1(), 2))
                .phone2Area(phonePart(customer.getPhoneNumber2(), 0))
                .phone2Prefix(phonePart(customer.getPhoneNumber2(), 1))
                .phone2Line(phonePart(customer.getPhoneNumber2(), 2))
                .ssnPart1(ssnPart(customer.getSsn(), 0))
                .ssnPart2(ssnPart(customer.getSsn(), 1))
                .ssnPart3(ssnPart(customer.getSsn(), 2))
                .governmentIssuedId(customer.getGovernmentIssuedId())
                .dobYear(datePart(customer.getDateOfBirth(), 0))
                .dobMonth(datePart(customer.getDateOfBirth(), 1))
                .dobDay(datePart(customer.getDateOfBirth(), 2))
                .eftAccountId(customer.getEftAccountId())
                .primaryCardHolder(customer.getPrimaryCardHolderIndicator())
                .ficoScore(customer.getFicoCreditScore() == null
                        ? null : String.format("%03d", customer.getFicoCreditScore()))
                .build();
    }

    /**
     * 1205-COMPARE-OLD-NEW — returns true for NO-CHANGES-DETECTED. Case insensitive
     * where the COBOL uses FUNCTION UPPER-CASE, numeric where the map field is numeric.
     */
    public boolean sameAs(AccountUpdateData other) {
        if (other == null) {
            return false;
        }
        boolean accountSame = exact(accountId, other.accountId)
                && ignoreCase(activeStatus, other.activeStatus)
                && numeric(currentBalance, other.currentBalance)
                && numeric(creditLimit, other.creditLimit)
                && numeric(cashCreditLimit, other.cashCreditLimit)
                && exact(openDate(), other.openDate())
                && exact(expiryDate(), other.expiryDate())
                && exact(reissueDate(), other.reissueDate())
                && numeric(currentCycleCredit, other.currentCycleCredit)
                && numeric(currentCycleDebit, other.currentCycleDebit)
                && ignoreCase(groupId, other.groupId);
        if (!accountSame) {
            return false;
        }
        return ignoreCase(customerId, other.customerId)
                && ignoreCase(firstName, other.firstName)
                && ignoreCase(middleName, other.middleName)
                && ignoreCase(lastName, other.lastName)
                && ignoreCase(addressLine1, other.addressLine1)
                && ignoreCase(addressLine2, other.addressLine2)
                && ignoreCase(city, other.city)
                && ignoreCase(stateCode, other.stateCode)
                && ignoreCase(countryCode, other.countryCode)
                && ignoreCase(zipCode, other.zipCode)
                && exact(phone1Area, other.phone1Area)
                && exact(phone1Prefix, other.phone1Prefix)
                && exact(phone1Line, other.phone1Line)
                && exact(phone2Area, other.phone2Area)
                && exact(phone2Prefix, other.phone2Prefix)
                && exact(phone2Line, other.phone2Line)
                && exact(ssn(), other.ssn())
                && ignoreCase(governmentIssuedId, other.governmentIssuedId)
                && exact(dateOfBirth(), other.dateOfBirth())
                && exact(eftAccountId, other.eftAccountId)
                && ignoreCase(primaryCardHolder, other.primaryCardHolder)
                && exact(ficoScore, other.ficoScore);
    }

    /** ACUP-...-OPEN-DATE as the CCYYMMDD group item. */
    public String openDate() {
        return concat(openYear, openMonth, openDay);
    }

    public String expiryDate() {
        return concat(expiryYear, expiryMonth, expiryDay);
    }

    public String reissueDate() {
        return concat(reissueYear, reissueMonth, reissueDay);
    }

    public String dateOfBirth() {
        return concat(dobYear, dobMonth, dobDay);
    }

    public String ssn() {
        return concat(ssnPart1, ssnPart2, ssnPart3);
    }

    /** ACCT-OPEN-DATE and friends are stored as X(10) CCYY-MM-DD. */
    public String isoOpenDate() {
        return isoDate(openYear, openMonth, openDay);
    }

    public String isoExpiryDate() {
        return isoDate(expiryYear, expiryMonth, expiryDay);
    }

    public String isoReissueDate() {
        return isoDate(reissueYear, reissueMonth, reissueDay);
    }

    public String isoDateOfBirth() {
        return isoDate(dobYear, dobMonth, dobDay);
    }

    /** CUST-PHONE-NUM-x is stored as (999)999-9999. */
    public String phoneNumber1() {
        return phone(phone1Area, phone1Prefix, phone1Line);
    }

    public String phoneNumber2() {
        return phone(phone2Area, phone2Prefix, phone2Line);
    }

    private static String concat(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(part == null ? "" : part.trim());
        }
        return builder.toString();
    }

    private static String isoDate(String year, String month, String day) {
        if (year == null || month == null || day == null) {
            return null;
        }
        return String.format("%s-%s-%s", year.trim(), pad2(month), pad2(day));
    }

    private static String phone(String area, String prefix, String line) {
        if (area == null || prefix == null || line == null
                || area.trim().isEmpty() || prefix.trim().isEmpty() || line.trim().isEmpty()) {
            return "";
        }
        return "(" + area.trim() + ")" + prefix.trim() + "-" + line.trim();
    }

    private static String pad2(String value) {
        String trimmed = value.trim();
        return trimmed.length() == 1 ? "0" + trimmed : trimmed;
    }

    private static String amount(BigDecimal value) {
        return value == null ? null : value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String datePart(String isoDate, int index) {
        if (isoDate == null) {
            return null;
        }
        String[] parts = isoDate.trim().split("-");
        return parts.length == 3 ? parts[index] : null;
    }

    private static String phonePart(String phone, int index) {
        if (phone == null || phone.trim().length() != 13) {
            return null;
        }
        String trimmed = phone.trim();
        switch (index) {
            case 0:
                return trimmed.substring(1, 4);
            case 1:
                return trimmed.substring(5, 8);
            default:
                return trimmed.substring(9, 13);
        }
    }

    private static String ssnPart(Long ssn, int index) {
        if (ssn == null) {
            return null;
        }
        String digits = String.format("%09d", ssn);
        switch (index) {
            case 0:
                return digits.substring(0, 3);
            case 1:
                return digits.substring(3, 5);
            default:
                return digits.substring(5);
        }
    }

    private static boolean exact(String left, String right) {
        return Objects.equals(nullToEmpty(left).trim(), nullToEmpty(right).trim());
    }

    private static boolean ignoreCase(String left, String right) {
        return nullToEmpty(left).trim().equalsIgnoreCase(nullToEmpty(right).trim());
    }

    private static boolean numeric(String left, String right) {
        BigDecimal leftValue = AccountFieldValidator.parseSigned9v2(left);
        BigDecimal rightValue = AccountFieldValidator.parseSigned9v2(right);
        if (leftValue == null || rightValue == null) {
            return exact(left, right);
        }
        return leftValue.compareTo(rightValue) == 0;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
