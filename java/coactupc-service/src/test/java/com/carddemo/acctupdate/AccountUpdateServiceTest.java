package com.carddemo.acctupdate;

import com.carddemo.acctupdate.api.*;
import com.carddemo.acctupdate.domain.*;
import com.carddemo.acctupdate.repository.*;
import com.carddemo.acctupdate.service.AccountUpdateService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountUpdateServiceTest {
    @Autowired AccountUpdateService service;
    @Autowired AccountRepository accounts;
    @Autowired CustomerRepository customers;
    @Autowired CardXrefRepository xrefs;
    private AccountUpdateDetails details;
    @BeforeEach void seed() {
        xrefs.deleteAll(); customers.deleteAll(); accounts.deleteAll();
        AccountRecord a = new AccountRecord(); a.setAcctId(12345678901L); a.setActiveStatus("Y"); a.setCurrBal(new BigDecimal("100.00")); a.setCreditLimit(new BigDecimal("1000.00")); a.setCashCreditLimit(new BigDecimal("500.00")); a.setOpenDate("2020-01-02"); a.setExpirationDate("2025-01-02"); a.setReissueDate("2024-01-02"); a.setCurrCycCredit(new BigDecimal("10.00")); a.setCurrCycDebit(new BigDecimal("4.00")); a.setAddrZip("90210"); a.setGroupId("G1"); accounts.save(a);
        CustomerRecord c = new CustomerRecord(); c.setCustId(123456789L); c.setFirstName("Jane"); c.setMiddleName("Q"); c.setLastName("Doe"); c.setAddrLine1("1 Main"); c.setAddrLine2(""); c.setAddrLine3("Beverly"); c.setAddrStateCd("CA"); c.setAddrCountryCd("USA"); c.setAddrZip("90210"); c.setPhoneNum1("(201)555-1212"); c.setPhoneNum2(""); c.setSsn(123456789L); c.setGovtIssuedId("ID"); c.setDobYyyyMmDd("1980-01-01"); c.setEftAccountId("1234567890"); c.setPriCardHolderInd("Y"); c.setFicoCreditScore(700); customers.save(c);
        CardXrefRecord x = new CardXrefRecord(); x.setCardNum("CARD123"); x.setAcctId(a.getAcctId()); x.setCustId(c.getCustId()); xrefs.save(x);
        details = service.readAccount("12345678901").getDetails();
    }
    @Test void readsAndValidatesNoChange() {
        assertEquals("123456789", details.getCustId());
        AccountUpdateRequest q = new AccountUpdateRequest(); q.setOriginal(details); q.setUpdated(details);
        AccountUpdateResponse r = service.editMapInputs(q);
        assertEquals(ChangeAction.SHOW_DETAILS, r.getAction()); assertEquals(Messages.NO_CHANGE, r.getErrorMessage());
    }
    @Test void readsNotFound() {
        assertEquals(Messages.XREF_NOT_FOUND, service.readAccount("11111111111").getErrorMessage());
    }
    @Test void writesChangedGroupAndPreservesZip() {
        AccountUpdateDetails updated = copy(details);
        updated.setGroupId("G2");
        AccountUpdateRequest q = new AccountUpdateRequest(); q.setOriginal(details); q.setUpdated(updated);
        AccountUpdateResponse r = service.writeProcessing(q);
        assertEquals(ChangeAction.CHANGES_OKAYED_AND_DONE, r.getAction());
        assertEquals("G2", accounts.findById(12345678901L).get().getGroupId());
        assertEquals("90210", accounts.findById(12345678901L).get().getAddrZip());
    }
    private AccountUpdateDetails copy(AccountUpdateDetails source) {
        AccountUpdateDetails d = new AccountUpdateDetails();
        d.setAcctId(source.getAcctId()); d.setActiveStatus(source.getActiveStatus()); d.setCurrBal(source.getCurrBal()); d.setCreditLimit(source.getCreditLimit()); d.setCashCreditLimit(source.getCashCreditLimit()); d.setCurrCycCredit(source.getCurrCycCredit()); d.setCurrCycDebit(source.getCurrCycDebit());
        d.setOpenYear(source.getOpenYear()); d.setOpenMon(source.getOpenMon()); d.setOpenDay(source.getOpenDay()); d.setExpYear(source.getExpYear()); d.setExpMon(source.getExpMon()); d.setExpDay(source.getExpDay()); d.setReissueYear(source.getReissueYear()); d.setReissueMon(source.getReissueMon()); d.setReissueDay(source.getReissueDay()); d.setGroupId(source.getGroupId()); d.setCustId(source.getCustId());
        d.setSsn1(source.getSsn1()); d.setSsn2(source.getSsn2()); d.setSsn3(source.getSsn3()); d.setDobYear(source.getDobYear()); d.setDobMon(source.getDobMon()); d.setDobDay(source.getDobDay()); d.setFicoScore(source.getFicoScore()); d.setFirstName(source.getFirstName()); d.setMiddleName(source.getMiddleName()); d.setLastName(source.getLastName()); d.setAddrLine1(source.getAddrLine1()); d.setAddrLine2(source.getAddrLine2()); d.setAddrLine3(source.getAddrLine3()); d.setAddrStateCd(source.getAddrStateCd()); d.setAddrCountryCd(source.getAddrCountryCd()); d.setAddrZip(source.getAddrZip()); d.setPhone1A(source.getPhone1A()); d.setPhone1B(source.getPhone1B()); d.setPhone1C(source.getPhone1C()); d.setPhone2A(source.getPhone2A()); d.setPhone2B(source.getPhone2B()); d.setPhone2C(source.getPhone2C()); d.setGovtIssuedId(source.getGovtIssuedId()); d.setEftAccountId(source.getEftAccountId()); d.setPriHolderInd(source.getPriHolderInd()); d.setCardNum(source.getCardNum()); return d;
    }
}
