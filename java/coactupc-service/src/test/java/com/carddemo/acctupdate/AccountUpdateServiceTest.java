package com.carddemo.acctupdate;

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
import com.carddemo.acctupdate.service.AccountUpdateService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AccountUpdateServiceTest {
    private static final long ACCOUNT_ID = 12345678901L;
    private static final long CUSTOMER_ID = 123456789L;

    @Autowired
    private AccountUpdateService service;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private CustomerRepository customers;

    @Autowired
    private CardXrefRepository xrefs;

    private AccountUpdateDetails details;

    @BeforeEach
    void seed() {
        xrefs.deleteAll();
        customers.deleteAll();
        accounts.deleteAll();

        AccountRecord account = new AccountRecord();
        account.setAcctId(ACCOUNT_ID);
        account.setActiveStatus("Y");
        account.setCurrBal(new BigDecimal("100.00"));
        account.setCreditLimit(new BigDecimal("1000.00"));
        account.setCashCreditLimit(new BigDecimal("500.00"));
        account.setOpenDate("2020-01-02");
        account.setExpirationDate("2025-01-02");
        account.setReissueDate("2024-01-02");
        account.setCurrCycCredit(new BigDecimal("10.00"));
        account.setCurrCycDebit(new BigDecimal("4.00"));
        account.setAddrZip("90210");
        account.setGroupId("G1");
        accounts.save(account);

        CustomerRecord customer = new CustomerRecord();
        customer.setCustId(CUSTOMER_ID);
        customer.setFirstName("Jane");
        customer.setMiddleName("Q");
        customer.setLastName("Doe");
        customer.setAddrLine1("1 Main");
        customer.setAddrLine2("");
        customer.setAddrLine3("Beverly");
        customer.setAddrStateCd("CA");
        customer.setAddrCountryCd("USA");
        customer.setAddrZip("90210");
        customer.setPhoneNum1("(201)555-1212");
        customer.setPhoneNum2("");
        customer.setSsn(123456789L);
        customer.setGovtIssuedId("ID");
        customer.setDobYyyyMmDd("1980-01-01");
        customer.setEftAccountId("1234567890");
        customer.setPriCardHolderInd("Y");
        customer.setFicoCreditScore(700);
        customers.save(customer);

        CardXrefRecord xref = new CardXrefRecord();
        xref.setCardNum("CARD123");
        xref.setAcctId(ACCOUNT_ID);
        xref.setCustId(CUSTOMER_ID);
        xrefs.save(xref);

        details = service.readAccount("12345678901").getDetails();
    }

    @Test
    void readAccountHappyPathSplitsCopybookFields() {
        assertEquals("12345678901", details.getAcctId());
        assertEquals("123456789", details.getCustId());
        assertEquals("123", details.getSsn1());
        assertEquals("45", details.getSsn2());
        assertEquals("6789", details.getSsn3());
        assertEquals("201", details.getPhone1A());
        assertEquals("555", details.getPhone1B());
        assertEquals("1212", details.getPhone1C());
        assertEquals("", details.getPhone2A());
        assertEquals("", details.getPhone2B());
        assertEquals("", details.getPhone2C());
        assertEquals("700", details.getFicoScore());
        assertEquals("2020", details.getOpenYear());
        assertEquals("01", details.getOpenMon());
        assertEquals("02", details.getOpenDay());
        assertEquals("100.00", details.getCurrBal());
        assertEquals("CARD123", details.getCardNum());
    }

    @Test
    void readAccountReturnsXrefNotFound() {
        xrefs.deleteAll();

        AccountUpdateResponse response = service.readAccount("12345678901");

        assertEquals(Messages.XREF_NOT_FOUND, response.getErrorMessage());
        assertEquals(Messages.ENTER_ACCOUNT, response.getInfoMessage());
    }

    @Test
    void readAccountReturnsAccountNotFound() {
        accounts.deleteAll();

        AccountUpdateResponse response = service.readAccount("12345678901");

        assertEquals(Messages.ACCOUNT_NOT_FOUND, response.getErrorMessage());
    }

    @Test
    void readAccountReturnsCustomerNotFound() {
        customers.deleteAll();

        AccountUpdateResponse response = service.readAccount("12345678901");

        assertEquals(Messages.CUSTOMER_NOT_FOUND, response.getErrorMessage());
    }

    @Test
    void readAccountRejectsInvalidId() {
        AccountUpdateResponse response = service.readAccount("abc");

        assertEquals(Messages.ACCOUNT_INVALID, response.getErrorMessage());
        assertEquals(Messages.ENTER_ACCOUNT, response.getInfoMessage());
    }

    @Test
    void readAccountRejectsBlankId() {
        AccountUpdateResponse response = service.readAccount("");

        assertEquals(Messages.ACCOUNT_NOT_PROVIDED, response.getErrorMessage());
    }

    @Test
    void validateIdenticalDetailsReturnsNoChange() {
        AccountUpdateResponse response = service.editMapInputs(request(details));

        assertEquals(ChangeAction.SHOW_DETAILS, response.getAction());
        assertEquals(Messages.NO_CHANGE, response.getErrorMessage());
        assertEquals(Messages.UPDATE_PRESENTED, response.getInfoMessage());
    }

    @Test
    void validateRejectsInvalidAccountStatus() {
        AccountUpdateDetails updated = details.copy();
        updated.setActiveStatus("X");

        AccountUpdateResponse response = service.editMapInputs(request(updated));

        assertEquals(ChangeAction.CHANGES_NOT_OK, response.getAction());
        assertEquals("Account Status must be Y or N.", response.getErrorMessage());
        assertEquals(FieldFlag.NOT_OK, response.getFieldFlags().get("Account Status"));
    }

    @Test
    void validateRejectsOutOfRangeFico() {
        AccountUpdateDetails updated = details.copy();
        updated.setFicoScore("299");

        AccountUpdateResponse response = service.editMapInputs(request(updated));

        assertEquals("FICO Score: should be between 300 and 850", response.getErrorMessage());
        assertEquals(FieldFlag.NOT_OK, response.getFieldFlags().get("FICO Score"));
    }

    @Test
    void validateRejectsInvalidSsnPartOne() {
        AccountUpdateDetails updated = details.copy();
        updated.setSsn1("666");

        AccountUpdateResponse response = service.editMapInputs(request(updated));

        assertEquals(
            "SSN: First 3 chars: should not be 000, 666, or between 900 and 999",
            response.getErrorMessage()
        );
        assertEquals(FieldFlag.NOT_OK, response.getFieldFlags().get("SSN: First 3 chars"));
    }

    @Test
    void validateKeepsFirstMessageWhileCollectingAllFlags() {
        AccountUpdateDetails updated = details.copy();
        updated.setActiveStatus("X");
        updated.setFicoScore("299");

        AccountUpdateResponse response = service.editMapInputs(request(updated));

        assertEquals("Account Status must be Y or N.", response.getErrorMessage());
        assertEquals(FieldFlag.NOT_OK, response.getFieldFlags().get("Account Status"));
        assertEquals(FieldFlag.NOT_OK, response.getFieldFlags().get("FICO Score"));
    }

    @Test
    void validateAcceptsGoodLastNameChange() {
        AccountUpdateDetails updated = details.copy();
        updated.setLastName("Smith");

        AccountUpdateResponse response = service.editMapInputs(request(updated));

        assertEquals(ChangeAction.CHANGES_OK_NOT_CONFIRMED, response.getAction());
        assertEquals(Messages.CHANGES_VALIDATED, response.getInfoMessage());
    }

    @Test
    void writeProcessingUpdatesBothRowsAndPreservesZip() {
        AccountUpdateDetails updated = details.copy();
        updated.setGroupId("G2");
        updated.setLastName("Smith");
        updated.setPhone2A("201");
        updated.setPhone2B("555");
        updated.setPhone2C("9999");
        updated.setCreditLimit("2,500.00");
        updated.setExpYear("2030");
        updated.setExpMon("12");
        updated.setExpDay("31");

        AccountUpdateResponse response = service.writeProcessing(request(updated));

        assertEquals(ChangeAction.CHANGES_OKAYED_AND_DONE, response.getAction());
        assertEquals("CARD123", response.getDetails().getCardNum());
        assertEquals(new BigDecimal("2500.00"), accounts.findById(ACCOUNT_ID).get().getCreditLimit());
        assertEquals("2030-12-31", accounts.findById(ACCOUNT_ID).get().getExpirationDate());
        assertEquals("G2", accounts.findById(ACCOUNT_ID).get().getGroupId());
        assertEquals("90210", accounts.findById(ACCOUNT_ID).get().getAddrZip());
        assertEquals("Smith", customers.findById(CUSTOMER_ID).get().getLastName());
        assertEquals("(201)555-9999", customers.findById(CUSTOMER_ID).get().getPhoneNum2());
    }

    @Test
    void writeProcessingDetectsChangedAccount() {
        AccountRecord account = accounts.findById(ACCOUNT_ID).get();
        account.setCreditLimit(new BigDecimal("999.00"));
        accounts.saveAndFlush(account);

        AccountUpdateDetails updated = details.copy();
        updated.setLastName("Smith");
        AccountUpdateResponse response = service.writeProcessing(request(updated));

        assertEquals(ChangeAction.SHOW_DETAILS, response.getAction());
        assertEquals(Messages.DATA_CHANGED, response.getErrorMessage());
        assertEquals(new BigDecimal("999.00"), accounts.findById(ACCOUNT_ID).get().getCreditLimit());
        assertEquals("Doe", customers.findById(CUSTOMER_ID).get().getLastName());
    }

    @Test
    void writeProcessingDetectsChangedCustomer() {
        CustomerRecord customer = customers.findById(CUSTOMER_ID).get();
        customer.setLastName("Jones");
        customers.saveAndFlush(customer);

        AccountUpdateDetails updated = details.copy();
        updated.setGroupId("G2");
        AccountUpdateResponse response = service.writeProcessing(request(updated));

        assertEquals(ChangeAction.SHOW_DETAILS, response.getAction());
        assertEquals(Messages.DATA_CHANGED, response.getErrorMessage());
        assertEquals("Jones", customers.findById(CUSTOMER_ID).get().getLastName());
    }

    @Test
    void writeProcessingReturnsAccountLockErrorWhenAccountDeleted() {
        accounts.deleteById(ACCOUNT_ID);

        AccountUpdateDetails updated = details.copy();
        updated.setGroupId("G2");
        AccountUpdateResponse response = service.writeProcessing(request(updated));

        assertEquals(ChangeAction.CHANGES_OKAYED_LOCK_ERROR, response.getAction());
        assertEquals(Messages.LOCK_ACCOUNT, response.getErrorMessage());
        assertEquals(Messages.UNSUCCESSFUL, response.getInfoMessage());
    }

    @Test
    void writeProcessingReturnsCustomerLockErrorWhenCustomerDeleted() {
        customers.deleteById(CUSTOMER_ID);

        AccountUpdateDetails updated = details.copy();
        updated.setGroupId("G2");
        AccountUpdateResponse response = service.writeProcessing(request(updated));

        assertEquals(ChangeAction.CHANGES_OKAYED_LOCK_ERROR, response.getAction());
        assertEquals(Messages.LOCK_CUSTOMER, response.getErrorMessage());
    }

    @Test
    void writeProcessingReturnsValidationResponseWithoutWriting() {
        AccountUpdateDetails updated = details.copy();
        updated.setActiveStatus("X");

        AccountUpdateResponse response = service.writeProcessing(request(updated));

        assertEquals(ChangeAction.CHANGES_NOT_OK, response.getAction());
        assertEquals("Y", accounts.findById(ACCOUNT_ID).get().getActiveStatus());
        assertEquals("Doe", customers.findById(CUSTOMER_ID).get().getLastName());
    }

    @Test
    void writeProcessingTreatsNullStoredPhoneAsBlankSnapshot() {
        CustomerRecord customer = customers.findById(CUSTOMER_ID).get();
        customer.setPhoneNum2(null);
        customers.saveAndFlush(customer);

        AccountUpdateDetails updated = details.copy();
        updated.setGroupId("G2");
        AccountUpdateResponse response = service.writeProcessing(request(updated));

        assertEquals(ChangeAction.CHANGES_OKAYED_AND_DONE, response.getAction());
        assertEquals("()-", customers.findById(CUSTOMER_ID).get().getPhoneNum2());
    }

    private AccountUpdateRequest request(AccountUpdateDetails updated) {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setOriginal(details);
        request.setUpdated(updated);
        return request;
    }
}
