package com.carddemo.online.account;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: COACTUPC — "Update Account" online transaction CAUP,
 * mapset COACTUP, map CACTUPA.
 *
 * <p>Files: CXACAIX (CARD-XREF-RECORD, CVACT03Y), ACCTDAT (ACCOUNT-RECORD, CVACT01Y),
 * CUSTDAT (CUSTOMER-RECORD, CVCUS01Y). Copybooks: CVCRD01Y (work areas), COCOM01Y
 * (COMMAREA), CSUTLDPY/CSUTLDWY (date edits), CSLKPCDY (lookup tables).</p>
 *
 * <p>Reproduces 0000-MAIN, 1000-PROCESS-INPUTS, 1200-EDIT-MAP-INPUTS,
 * 1205-COMPARE-OLD-NEW, 2000-DECIDE-ACTION, 3250-SETUP-INFOMSG, 9000-READ-ACCT,
 * 9600-WRITE-PROCESSING and 9700-CHECK-CHANGE-IN-REC.</p>
 */
@Service
public class AccountUpdateService {

    /** WS-INFO-MSG 88 levels. */
    public static final String FOUND_ACCOUNT_DATA = "Details of selected account shown above";
    public static final String PROMPT_FOR_SEARCH_KEYS = "Enter or update id of account to update";
    public static final String PROMPT_FOR_CHANGES = "Update account details presented above.";
    public static final String PROMPT_FOR_CONFIRMATION = "Changes validated.Press F5 to save";
    public static final String CONFIRM_UPDATE_SUCCESS = "Changes committed to database";
    public static final String INFORM_FAILURE = "Changes unsuccessful. Please try again";

    /** WS-RETURN-MSG 88 levels. */
    public static final String EXIT_MESSAGE = "PF03 pressed.Exiting";
    public static final String NO_SEARCH_CRITERIA_RECEIVED = "No input received";
    public static final String NO_CHANGES_DETECTED = "No change detected with respect to values fetched.";
    public static final String DID_NOT_FIND_ACCT_IN_CARDXREF = "Did not find this account in account card xref file";
    public static final String DID_NOT_FIND_ACCT_IN_ACCTDAT = "Did not find this account in account master file";
    public static final String DID_NOT_FIND_CUST_IN_CUSTDAT = "Did not find associated customer in master file";
    public static final String COULD_NOT_LOCK_ACCT_FOR_UPDATE = "Could not lock account record for update";
    public static final String COULD_NOT_LOCK_CUST_FOR_UPDATE = "Could not lock customer record for update";
    public static final String DATA_WAS_CHANGED_BEFORE_UPDATE = "Record changed by some one else. Please review";
    public static final String LOCKED_BUT_UPDATE_FAILED = "Update of record failed";

    /** LIT-MENUPGM used when the caller is unknown. */
    public static final String MENU_PROGRAM = "COMEN01C";
    public static final String THIS_PROGRAM = "COACTUPC";
    public static final String THIS_TRANSACTION = "CAUP";
    public static final String THIS_MAPSET = "COACTUP";
    public static final String THIS_MAP = "CACTUPA";

    private static final String F_ACCOUNT_ID = "accountId";
    private static final String F_ACTIVE_STATUS = "activeStatus";
    private static final String F_OPEN_DATE = "openDate";
    private static final String F_CREDIT_LIMIT = "creditLimit";
    private static final String F_EXPIRY_DATE = "expiryDate";
    private static final String F_CASH_CREDIT_LIMIT = "cashCreditLimit";
    private static final String F_REISSUE_DATE = "reissueDate";
    private static final String F_CURRENT_BALANCE = "currentBalance";
    private static final String F_CURR_CYC_CREDIT = "currentCycleCredit";
    private static final String F_CURR_CYC_DEBIT = "currentCycleDebit";
    private static final String F_SSN = "ssn";
    private static final String F_DOB = "dateOfBirth";
    private static final String F_FICO = "ficoScore";
    private static final String F_FIRST_NAME = "firstName";
    private static final String F_MIDDLE_NAME = "middleName";
    private static final String F_LAST_NAME = "lastName";
    private static final String F_ADDRESS_LINE_1 = "addressLine1";
    private static final String F_STATE = "stateCode";
    private static final String F_ZIP = "zipCode";
    private static final String F_CITY = "city";
    private static final String F_COUNTRY = "countryCode";
    private static final String F_PHONE_1 = "phoneNumber1";
    private static final String F_PHONE_2 = "phoneNumber2";
    private static final String F_EFT_ACCOUNT_ID = "eftAccountId";
    private static final String F_PRIMARY_HOLDER = "primaryCardHolder";

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountUpdateService(CardXrefRepository cardXrefRepository, AccountRepository accountRepository,
            CustomerRepository customerRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    /** 0000-MAIN of COACTUPC for a single pseudo-conversational turn. */
    @Transactional
    public AccountUpdateResponse process(AccountUpdateRequest request, AccountUpdateState state,
            CardDemoCommarea commarea) {
        AccountValidationResult result = new AccountValidationResult();
        String action = resolveAid(request, state);

        if (AccountUpdateRequest.ACTION_PF3.equals(action)) {
            return exit(state, commarea);
        }

        // 0000-MAIN: a completed or failed update resets the program area and reprompts.
        if (AccountUpdateState.CHANGES_OKAYED_AND_DONE.equals(state.getChangeAction()) || state.isChangesFailed()) {
            state.setChangeAction(AccountUpdateState.DETAILS_NOT_FETCHED);
            state.setOldDetails(null);
            state.setNewDetails(null);
            return respond(state, result, PROMPT_FOR_SEARCH_KEYS, null);
        }

        AccountUpdateData input = receiveMap(request, state);
        state.setNewDetails(input);

        if (state.isDetailsNotFetched() || AccountUpdateRequest.ACTION_PF12.equals(action)) {
            return fetchDetails(state, input, result, commarea);
        }

        editMapInputs(state, input, result);

        // 2000-DECIDE-ACTION
        if (AccountUpdateState.SHOW_DETAILS.equals(state.getChangeAction())) {
            if (!result.isInputError() && !NO_CHANGES_DETECTED.equals(result.getReturnMessage())) {
                state.setChangeAction(AccountUpdateState.CHANGES_OK_NOT_CONFIRMED);
            }
        } else if (AccountUpdateState.CHANGES_OK_NOT_CONFIRMED.equals(state.getChangeAction())
                && AccountUpdateRequest.ACTION_PF5.equals(action)) {
            writeProcessing(state, input, result, commarea);
        }

        return respond(state, result, null, null);
    }

    /** Validates the AID key: F3, F5 (only when changes await confirmation) and F12 after a fetch. */
    private String resolveAid(AccountUpdateRequest request, AccountUpdateState state) {
        String action = request == null || request.getAction() == null
                ? AccountUpdateRequest.ACTION_ENTER
                : request.getAction().trim().toUpperCase();
        boolean valid = AccountUpdateRequest.ACTION_ENTER.equals(action)
                || AccountUpdateRequest.ACTION_PF3.equals(action)
                || (AccountUpdateRequest.ACTION_PF5.equals(action)
                        && AccountUpdateState.CHANGES_OK_NOT_CONFIRMED.equals(state.getChangeAction()))
                || (AccountUpdateRequest.ACTION_PF12.equals(action) && !state.isDetailsNotFetched());
        return valid ? action : AccountUpdateRequest.ACTION_ENTER;
    }

    /** 1100-RECEIVE-MAP: '*' and spaces are LOW-VALUES, everything else is taken as keyed. */
    private AccountUpdateData receiveMap(AccountUpdateRequest request, AccountUpdateState state) {
        AccountUpdateData data = request == null || request.getData() == null
                ? new AccountUpdateData()
                : request.getData().toBuilder().build();
        data.setAccountId(AccountViewService.normalizeSearchKey(data.getAccountId()));
        if (state.isDetailsNotFetched()) {
            return data;
        }
        if (state.getOldDetails() != null) {
            // Fields that are display-only on the map keep the values that were fetched.
            data.setCustomerId(state.getOldDetails().getCustomerId());
        }
        return data;
    }

    /** 9000-READ-ACCT: cross reference, account master and customer master, in that order. */
    private AccountUpdateResponse fetchDetails(AccountUpdateState state, AccountUpdateData input,
            AccountValidationResult result, CardDemoCommarea commarea) {
        if (!AccountFieldValidator.editAccountFilter(result, F_ACCOUNT_ID, input.getAccountId())) {
            if (input.getAccountId() == null) {
                result.setReturnMessage(NO_SEARCH_CRITERIA_RECEIVED);
            }
            state.setChangeAction(AccountUpdateState.DETAILS_NOT_FETCHED);
            return respond(state, result, PROMPT_FOR_SEARCH_KEYS, null);
        }

        long accountId = Long.parseLong(input.getAccountId().trim());
        Optional<CardXref> xref = cardXrefRepository.findFirstByAccountId(accountId);
        if (xref.isEmpty()) {
            result.addError(F_ACCOUNT_ID, DID_NOT_FIND_ACCT_IN_CARDXREF);
            state.setChangeAction(AccountUpdateState.DETAILS_NOT_FETCHED);
            return respond(state, result, PROMPT_FOR_SEARCH_KEYS, null);
        }

        Optional<Account> account = accountRepository.findById(accountId);
        if (account.isEmpty()) {
            result.addError(F_ACCOUNT_ID, DID_NOT_FIND_ACCT_IN_ACCTDAT);
            state.setChangeAction(AccountUpdateState.DETAILS_NOT_FETCHED);
            return respond(state, result, PROMPT_FOR_SEARCH_KEYS, null);
        }

        Long customerId = xref.get().getCustomerId();
        Optional<Customer> customer = customerId == null
                ? Optional.empty()
                : customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            result.addError(F_ACCOUNT_ID, DID_NOT_FIND_CUST_IN_CUSTDAT);
            state.setChangeAction(AccountUpdateState.DETAILS_NOT_FETCHED);
            return respond(state, result, PROMPT_FOR_SEARCH_KEYS, null);
        }

        // 9500-STORE-FETCHED-DATA
        AccountUpdateData fetched = AccountUpdateData.fromEntities(account.get(), customer.get());
        state.setOldDetails(fetched);
        state.setNewDetails(fetched.toBuilder().build());
        state.setChangeAction(AccountUpdateState.SHOW_DETAILS);

        if (commarea != null) {
            commarea.setAccountId(accountId);
            commarea.setAccountStatus(account.get().getActiveStatus());
            commarea.setCustomerId(customerId);
            commarea.setCustomerFirstName(customer.get().getFirstName());
            commarea.setCustomerMiddleName(customer.get().getMiddleName());
            commarea.setCustomerLastName(customer.get().getLastName());
            commarea.setCardNumber(xref.get().getCardNumber());
            commarea.setLastMapset(THIS_MAPSET);
            commarea.setLastMap(THIS_MAP);
        }
        return respond(state, result, FOUND_ACCOUNT_DATA, null);
    }

    /** 1200-EDIT-MAP-INPUTS including 1205-COMPARE-OLD-NEW. */
    private void editMapInputs(AccountUpdateState state, AccountUpdateData input, AccountValidationResult result) {
        boolean noChanges = input.sameAs(state.getOldDetails());
        if (noChanges) {
            result.setReturnMessage(NO_CHANGES_DETECTED);
            return;
        }
        if (AccountUpdateState.CHANGES_OK_NOT_CONFIRMED.equals(state.getChangeAction())) {
            return;
        }

        state.setChangeAction(AccountUpdateState.CHANGES_NOT_OK);
        validate(input, result);
        if (!result.isInputError()) {
            state.setChangeAction(AccountUpdateState.CHANGES_OK_NOT_CONFIRMED);
        }
    }

    /** The edit sequence of 1200-EDIT-MAP-INPUTS, in the order the COBOL performs it. */
    void validate(AccountUpdateData input, AccountValidationResult result) {
        AccountFieldValidator.editYesNo(result, F_ACTIVE_STATUS, "Account Status", input.getActiveStatus());
        AccountFieldValidator.editDateCcyymmdd(result, F_OPEN_DATE, "Open Date",
                input.getOpenYear(), input.getOpenMonth(), input.getOpenDay());
        AccountFieldValidator.editSigned9v2(result, F_CREDIT_LIMIT, "Credit Limit", input.getCreditLimit());
        AccountFieldValidator.editDateCcyymmdd(result, F_EXPIRY_DATE, "Expiry Date",
                input.getExpiryYear(), input.getExpiryMonth(), input.getExpiryDay());
        AccountFieldValidator.editSigned9v2(result, F_CASH_CREDIT_LIMIT, "Cash Credit Limit",
                input.getCashCreditLimit());
        AccountFieldValidator.editDateCcyymmdd(result, F_REISSUE_DATE, "Reissue Date",
                input.getReissueYear(), input.getReissueMonth(), input.getReissueDay());
        AccountFieldValidator.editSigned9v2(result, F_CURRENT_BALANCE, "Current Balance", input.getCurrentBalance());
        AccountFieldValidator.editSigned9v2(result, F_CURR_CYC_CREDIT, "Current Cycle Credit Limit",
                input.getCurrentCycleCredit());
        AccountFieldValidator.editSigned9v2(result, F_CURR_CYC_DEBIT, "Current Cycle Debit Limit",
                input.getCurrentCycleDebit());
        AccountFieldValidator.editUsSsn(result, F_SSN, input.getSsnPart1(), input.getSsnPart2(), input.getSsnPart3());

        boolean dobValid = AccountFieldValidator.editDateCcyymmdd(result, F_DOB, "Date of Birth",
                input.getDobYear(), input.getDobMonth(), input.getDobDay());
        if (dobValid) {
            AccountFieldValidator.editDateOfBirth(result, F_DOB, "Date of Birth",
                    input.getDobYear(), input.getDobMonth(), input.getDobDay(), LocalDate.now());
        }

        if (AccountFieldValidator.editNumericRequired(result, F_FICO, "FICO Score", input.getFicoScore())) {
            AccountFieldValidator.editFicoScore(result, F_FICO, "FICO Score", input.getFicoScore());
        }

        AccountFieldValidator.editAlphaRequired(result, F_FIRST_NAME, "First Name", input.getFirstName());
        AccountFieldValidator.editAlphaOptional(result, F_MIDDLE_NAME, "Middle Name", input.getMiddleName());
        AccountFieldValidator.editAlphaRequired(result, F_LAST_NAME, "Last Name", input.getLastName());
        AccountFieldValidator.editMandatory(result, F_ADDRESS_LINE_1, "Address Line 1", input.getAddressLine1());

        boolean stateAlphaOk = AccountFieldValidator.editAlphaRequired(result, F_STATE, "State", input.getStateCode());
        if (stateAlphaOk) {
            AccountFieldValidator.editUsStateCode(result, F_STATE, "State", input.getStateCode());
        }
        boolean zipOk = AccountFieldValidator.editNumericRequired(result, F_ZIP, "Zip", input.getZipCode());
        AccountFieldValidator.editAlphaRequired(result, F_CITY, "City", input.getCity());
        AccountFieldValidator.editAlphaRequired(result, F_COUNTRY, "Country", input.getCountryCode());
        AccountFieldValidator.editUsPhoneNumber(result, F_PHONE_1, "Phone Number 1",
                input.getPhone1Area(), input.getPhone1Prefix(), input.getPhone1Line());
        AccountFieldValidator.editUsPhoneNumber(result, F_PHONE_2, "Phone Number 2",
                input.getPhone2Area(), input.getPhone2Prefix(), input.getPhone2Line());
        AccountFieldValidator.editNumericRequired(result, F_EFT_ACCOUNT_ID, "EFT Account Id",
                input.getEftAccountId());
        AccountFieldValidator.editYesNo(result, F_PRIMARY_HOLDER, "Primary Card Holder",
                input.getPrimaryCardHolder());

        // 1280-EDIT-US-STATE-ZIP-CD is a cross field edit run only when both fields are valid.
        if (!result.hasError(F_STATE) && zipOk) {
            AccountFieldValidator.editStateZipCombination(result, F_STATE, F_ZIP, input.getStateCode(),
                    input.getZipCode());
        }
    }

    /** 9600-WRITE-PROCESSING and 9700-CHECK-CHANGE-IN-REC. */
    private void writeProcessing(AccountUpdateState state, AccountUpdateData input, AccountValidationResult result,
            CardDemoCommarea commarea) {
        long accountId = Long.parseLong(state.getOldDetails().getAccountId().trim());
        Optional<Account> account = accountRepository.findById(accountId);
        if (account.isEmpty()) {
            result.addError(F_ACCOUNT_ID, COULD_NOT_LOCK_ACCT_FOR_UPDATE);
            state.setChangeAction(AccountUpdateState.CHANGES_OKAYED_LOCK_ERROR);
            return;
        }

        long customerId = Long.parseLong(state.getOldDetails().getCustomerId().trim());
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            result.addError(F_ACCOUNT_ID, COULD_NOT_LOCK_CUST_FOR_UPDATE);
            state.setChangeAction(AccountUpdateState.CHANGES_OKAYED_LOCK_ERROR);
            return;
        }

        AccountUpdateData persisted = AccountUpdateData.fromEntities(account.get(), customer.get());
        if (!persisted.sameAs(state.getOldDetails())) {
            result.addError(F_ACCOUNT_ID, DATA_WAS_CHANGED_BEFORE_UPDATE);
            state.setChangeAction(AccountUpdateState.SHOW_DETAILS);
            return;
        }

        try {
            accountRepository.save(applyToAccount(account.get(), input));
            customerRepository.save(applyToCustomer(customer.get(), input));
        } catch (RuntimeException e) {
            result.addError(F_ACCOUNT_ID, LOCKED_BUT_UPDATE_FAILED);
            state.setChangeAction(AccountUpdateState.CHANGES_OKAYED_BUT_FAILED);
            return;
        }

        state.setChangeAction(AccountUpdateState.CHANGES_OKAYED_AND_DONE);
        state.setOldDetails(input.toBuilder().build());
        if (commarea != null) {
            commarea.setAccountStatus(input.getActiveStatus());
        }
    }

    private Account applyToAccount(Account account, AccountUpdateData input) {
        account.setActiveStatus(input.getActiveStatus());
        account.setCurrentBalance(amount(input.getCurrentBalance()));
        account.setCreditLimit(amount(input.getCreditLimit()));
        account.setCashCreditLimit(amount(input.getCashCreditLimit()));
        account.setCurrentCycleCredit(amount(input.getCurrentCycleCredit()));
        account.setCurrentCycleDebit(amount(input.getCurrentCycleDebit()));
        account.setOpenDate(input.isoOpenDate());
        account.setExpirationDate(input.isoExpiryDate());
        account.setReissueDate(input.isoReissueDate());
        account.setGroupId(input.getGroupId());
        return account;
    }

    private Customer applyToCustomer(Customer customer, AccountUpdateData input) {
        customer.setFirstName(input.getFirstName());
        customer.setMiddleName(input.getMiddleName());
        customer.setLastName(input.getLastName());
        customer.setAddressLine1(input.getAddressLine1());
        customer.setAddressLine2(input.getAddressLine2());
        customer.setAddressLine3(input.getCity());
        customer.setStateCode(input.getStateCode());
        customer.setCountryCode(input.getCountryCode());
        customer.setZipCode(input.getZipCode());
        customer.setPhoneNumber1(input.phoneNumber1());
        customer.setPhoneNumber2(input.phoneNumber2());
        customer.setSsn(Long.parseLong(input.ssn()));
        customer.setGovernmentIssuedId(input.getGovernmentIssuedId());
        customer.setDateOfBirth(input.isoDateOfBirth());
        customer.setEftAccountId(input.getEftAccountId());
        customer.setPrimaryCardHolderIndicator(input.getPrimaryCardHolder());
        customer.setFicoCreditScore(Integer.parseInt(input.getFicoScore().trim()));
        return customer;
    }

    private static BigDecimal amount(String value) {
        BigDecimal parsed = AccountFieldValidator.parseSigned9v2(value);
        return parsed == null ? null : parsed.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /** PF03: hand control back to the calling program, or to the main menu. */
    private AccountUpdateResponse exit(AccountUpdateState state, CardDemoCommarea commarea) {
        String target = MENU_PROGRAM;
        if (commarea != null) {
            if (commarea.getFromProgram() != null && !commarea.getFromProgram().trim().isEmpty()) {
                target = commarea.getFromProgram();
            }
            commarea.setToProgram(target);
            commarea.setToTransactionId(commarea.getFromTransactionId() == null
                    || commarea.getFromTransactionId().trim().isEmpty() ? "CM00" : commarea.getFromTransactionId());
            commarea.setFromProgram(THIS_PROGRAM);
            commarea.setFromTransactionId(THIS_TRANSACTION);
            commarea.setLastMapset(THIS_MAPSET);
            commarea.setLastMap(THIS_MAP);
        }
        state.setChangeAction(AccountUpdateState.DETAILS_NOT_FETCHED);
        state.setOldDetails(null);
        state.setNewDetails(null);
        AccountValidationResult result = new AccountValidationResult();
        result.setReturnMessage(EXIT_MESSAGE);
        return respond(state, result, PROMPT_FOR_SEARCH_KEYS, target);
    }

    /** 3250-SETUP-INFOMSG plus the map send. */
    private AccountUpdateResponse respond(AccountUpdateState state, AccountValidationResult result,
            String infoMessage, String nextProgram) {
        String info = infoMessage != null ? infoMessage : infoMessageFor(state.getChangeAction());
        return AccountUpdateResponse.builder()
                .changeAction(state.getChangeAction())
                .infoMessage(info)
                .errorMessage(result.getReturnMessage())
                .fieldErrors(result.getFieldErrors())
                .data(state.getNewDetails())
                .nextProgram(nextProgram)
                .build();
    }

    private String infoMessageFor(String changeAction) {
        if (changeAction == null) {
            return PROMPT_FOR_SEARCH_KEYS;
        }
        switch (changeAction) {
            case AccountUpdateState.SHOW_DETAILS:
            case AccountUpdateState.CHANGES_NOT_OK:
                return PROMPT_FOR_CHANGES;
            case AccountUpdateState.CHANGES_OK_NOT_CONFIRMED:
                return PROMPT_FOR_CONFIRMATION;
            case AccountUpdateState.CHANGES_OKAYED_AND_DONE:
                return CONFIRM_UPDATE_SUCCESS;
            case AccountUpdateState.CHANGES_OKAYED_LOCK_ERROR:
            case AccountUpdateState.CHANGES_OKAYED_BUT_FAILED:
                return INFORM_FAILURE;
            default:
                return PROMPT_FOR_SEARCH_KEYS;
        }
    }
}
