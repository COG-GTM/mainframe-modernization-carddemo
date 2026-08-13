package com.carddemo.online.account;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COACTVWC — account view, output fields of BMS map CACTVWA
 * (mapset COACTVW), populated in 1200-SETUP-SCREEN-VARS from ACCOUNT-RECORD (CVACT01Y),
 * CUSTOMER-RECORD (CVCUS01Y) and CARD-XREF-RECORD (CVACT03Y).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountViewResponse {

    /** WS-INFO-MSG (Enter or update id of account to display / Displaying details of given Account). */
    private String infoMessage;

    /** WS-RETURN-MSG — the first error raised, if any. */
    private String errorMessage;

    /** Field level flags FLG-ACCTFILTER-NOT-OK / FLG-CUSTFILTER-NOT-OK. */
    private Map<String, String> fieldErrors;

    /** FOUND-ACCT-IN-MASTER and FOUND-CUST-IN-MASTER. */
    private boolean accountFound;
    private boolean customerFound;

    private String accountId;
    private String activeStatus;
    private BigDecimal currentBalance;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private String groupId;

    /** CDEMO-CARD-NUM as set from XREF-CARD-NUM. */
    private String cardNumber;

    private String customerId;
    /** CUST-SSN formatted as 999-99-9999 by the STRING in 1200-SETUP-SCREEN-VARS. */
    private String customerSsn;
    private Integer ficoScore;
    private String dateOfBirth;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateCode;
    private String zipCode;
    private String countryCode;
    private String phoneNumber1;
    private String phoneNumber2;
    private String governmentIssuedId;
    private String eftAccountId;
    private String primaryCardHolder;
}
