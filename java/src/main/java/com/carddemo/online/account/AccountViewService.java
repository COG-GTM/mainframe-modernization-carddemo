package com.carddemo.online.account;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.util.CobolUtils;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COACTVWC — "View Account" online transaction CAVW,
 * mapset COACTVW, map CACTVWA.
 *
 * <p>Files: CXACAIX (CARD-XREF-RECORD, CVACT03Y), ACCTDAT (ACCOUNT-RECORD, CVACT01Y),
 * CUSTDAT (CUSTOMER-RECORD, CVCUS01Y). Work areas from CVCRD01Y, COMMAREA from COCOM01Y.</p>
 *
 * <p>Reproduces 2200-EDIT-MAP-INPUTS, 2210-EDIT-ACCOUNT and 9000-READ-ACCT
 * (9200-GETCARDXREF-BYACCT, 9300-GETACCTDATA-BYACCT, 9400-GETCUSTDATA-BYCUST).</p>
 */
@Service
public class AccountViewService {

    /** WS-INFO-MSG 88 WS-PROMPT-FOR-INPUT. */
    public static final String PROMPT_FOR_INPUT = "Enter or update id of account to display";
    /** WS-INFO-MSG 88 WS-INFORM-OUTPUT. */
    public static final String INFORM_OUTPUT = "Displaying details of given Account";
    /** WS-RETURN-MSG 88 WS-PROMPT-FOR-ACCT. */
    public static final String PROMPT_FOR_ACCT = "Account number not provided";
    /** WS-RETURN-MSG set in 2210-EDIT-ACCOUNT. */
    public static final String ACCT_FILTER_NOT_VALID = "Account Filter must  be a non-zero 11 digit number";
    /** WS-RETURN-MSG 88 NO-SEARCH-CRITERIA-RECEIVED. */
    public static final String NO_SEARCH_CRITERIA = "No input received";

    /** DFHRESP(NOTFND). */
    private static final int RESP_NOTFND = 13;
    private static final int REAS_NOTFND = 0;

    private static final String ACCOUNT_ID_FIELD = "accountId";
    private static final String CUSTOMER_ID_FIELD = "customerId";

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountViewService(CardXrefRepository cardXrefRepository, AccountRepository accountRepository,
            CustomerRepository customerRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * 2200-EDIT-MAP-INPUTS followed by 9000-READ-ACCT; the commarea is updated with
     * the account, customer and card context exactly as COACTVWC does before XCTL.
     */
    public AccountViewResponse view(AccountViewRequest request, CardDemoCommarea commarea) {
        AccountValidationResult result = new AccountValidationResult();
        String searchKey = normalizeSearchKey(request == null ? null : request.getAccountId());

        AccountViewResponse.AccountViewResponseBuilder response = AccountViewResponse.builder();

        if (searchKey == null) {
            result.addError(ACCOUNT_ID_FIELD, PROMPT_FOR_ACCT);
            result.setReturnMessage(NO_SEARCH_CRITERIA);
            return response.infoMessage(PROMPT_FOR_INPUT)
                    .errorMessage(result.getReturnMessage())
                    .fieldErrors(result.getFieldErrors())
                    .build();
        }

        if (!searchKey.chars().allMatch(Character::isDigit) || Long.parseLong(searchKey) == 0L) {
            result.addError(ACCOUNT_ID_FIELD, ACCT_FILTER_NOT_VALID);
            return response.accountId(searchKey)
                    .infoMessage(PROMPT_FOR_INPUT)
                    .errorMessage(result.getReturnMessage())
                    .fieldErrors(result.getFieldErrors())
                    .build();
        }

        long accountId = Long.parseLong(searchKey);
        String paddedAccountId = CobolUtils.padRight(searchKey, 11);
        response.accountId(searchKey);

        Optional<CardXref> xref = cardXrefRepository.findFirstByAccountId(accountId);
        if (xref.isEmpty()) {
            result.addError(ACCOUNT_ID_FIELD, "Account:" + paddedAccountId
                    + " not found in Cross ref file.  Resp:" + resp(RESP_NOTFND) + " Reas:" + resp(REAS_NOTFND));
            return response.infoMessage(PROMPT_FOR_INPUT)
                    .errorMessage(result.getReturnMessage())
                    .fieldErrors(result.getFieldErrors())
                    .build();
        }

        Optional<Account> account = accountRepository.findById(accountId);
        if (account.isEmpty()) {
            result.addError(ACCOUNT_ID_FIELD, "Account:" + paddedAccountId
                    + " not found in Acct Master file.Resp:" + resp(RESP_NOTFND) + " Reas:" + resp(REAS_NOTFND));
            return response.infoMessage(PROMPT_FOR_INPUT)
                    .errorMessage(result.getReturnMessage())
                    .fieldErrors(result.getFieldErrors())
                    .build();
        }

        Long customerId = xref.get().getCustomerId();
        Optional<Customer> customer = customerId == null
                ? Optional.empty()
                : customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            result.addError(CUSTOMER_ID_FIELD, "CustId:" + CobolUtils.padRight(format9(customerId), 9)
                    + " not found in customer master.Resp: " + resp(RESP_NOTFND) + " REAS:" + resp(REAS_NOTFND));
            return applyAccount(response, account.get())
                    .accountFound(true)
                    .infoMessage(PROMPT_FOR_INPUT)
                    .errorMessage(result.getReturnMessage())
                    .fieldErrors(result.getFieldErrors())
                    .build();
        }

        applyAccount(response, account.get());
        applyCustomer(response, customer.get());
        response.accountFound(true)
                .customerFound(true)
                .cardNumber(xref.get().getCardNumber())
                .infoMessage(INFORM_OUTPUT)
                .fieldErrors(result.getFieldErrors());

        if (commarea != null) {
            commarea.setAccountId(accountId);
            commarea.setAccountStatus(account.get().getActiveStatus());
            commarea.setCustomerId(customerId);
            commarea.setCustomerFirstName(customer.get().getFirstName());
            commarea.setCustomerMiddleName(customer.get().getMiddleName());
            commarea.setCustomerLastName(customer.get().getLastName());
            commarea.setCardNumber(xref.get().getCardNumber());
            commarea.setLastMapset("COACTVW");
            commarea.setLastMap("CACTVWA");
        }

        return response.build();
    }

    /** 2200-EDIT-MAP-INPUTS: '*' and spaces are treated as LOW-VALUES. */
    static String normalizeSearchKey(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "*".equals(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private static String resp(int code) {
        return String.format("%09d", code);
    }

    private static String format9(Long value) {
        return value == null ? "" : String.format("%09d", value);
    }

    private AccountViewResponse.AccountViewResponseBuilder applyAccount(
            AccountViewResponse.AccountViewResponseBuilder builder, Account account) {
        return builder.activeStatus(account.getActiveStatus())
                .currentBalance(account.getCurrentBalance())
                .creditLimit(account.getCreditLimit())
                .cashCreditLimit(account.getCashCreditLimit())
                .currentCycleCredit(account.getCurrentCycleCredit())
                .currentCycleDebit(account.getCurrentCycleDebit())
                .openDate(account.getOpenDate())
                .expirationDate(account.getExpirationDate())
                .reissueDate(account.getReissueDate())
                .groupId(account.getGroupId());
    }

    private void applyCustomer(AccountViewResponse.AccountViewResponseBuilder builder, Customer customer) {
        builder.customerId(format9(customer.getCustomerId()))
                .customerSsn(formatSsn(customer.getSsn()))
                .ficoScore(customer.getFicoCreditScore())
                .dateOfBirth(customer.getDateOfBirth())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .city(customer.getAddressLine3())
                .stateCode(customer.getStateCode())
                .zipCode(customer.getZipCode())
                .countryCode(customer.getCountryCode())
                .phoneNumber1(customer.getPhoneNumber1())
                .phoneNumber2(customer.getPhoneNumber2())
                .governmentIssuedId(customer.getGovernmentIssuedId())
                .eftAccountId(customer.getEftAccountId())
                .primaryCardHolder(customer.getPrimaryCardHolderIndicator());
    }

    /** STRING CUST-SSN(1:3) '-' CUST-SSN(4:2) '-' CUST-SSN(6:4). */
    static String formatSsn(Long ssn) {
        if (ssn == null) {
            return null;
        }
        String digits = String.format("%09d", ssn);
        return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
    }
}
