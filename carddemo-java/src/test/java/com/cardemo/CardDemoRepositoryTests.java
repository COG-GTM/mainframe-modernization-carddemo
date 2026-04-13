package com.cardemo;

import com.cardemo.entity.*;
import com.cardemo.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive repository tests for all CardDemo entities.
 * Uses @DataJpaTest with H2 — Flyway migrations and seed data
 * (V1-V12) are applied automatically before each test class.
 */
@DataJpaTest
class CardDemoRepositoryTests {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private CardXrefRepository cardXrefRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private DailyTransactionRepository dailyTransactionRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private TransactionCategoryRepository transactionCategoryRepository;
    @Autowired private DisclosureGroupRepository disclosureGroupRepository;
    @Autowired private TransactionCategoryBalanceRepository transactionCategoryBalanceRepository;
    @Autowired private UserSecurityRepository userSecurityRepository;

    // ===================== ACCOUNT TESTS =====================

    @Test
    void accountSeedDataLoaded() {
        List<Account> accounts = accountRepository.findAll();
        assertEquals(50, accounts.size(), "Expected 50 accounts from acctdata.txt");
    }

    @Test
    void accountFirstRecordFieldValues() {
        Optional<Account> opt = accountRepository.findById(1L);
        assertTrue(opt.isPresent(), "Account with ID 1 should exist");
        Account a = opt.get();
        assertEquals("Y", a.getAcctActiveStatus());
        assertNotNull(a.getAcctCurrBal());
        assertNotNull(a.getAcctOpenDate());
    }

    @Test
    void accountFindByActiveStatus() {
        List<Account> active = accountRepository.findByAcctActiveStatus("Y");
        assertFalse(active.isEmpty(), "Should find active accounts");
    }

    @Test
    void accountCrud() {
        Account acct = new Account();
        acct.setAcctId(99999L);
        acct.setAcctActiveStatus("Y");
        acct.setAcctCurrBal(new BigDecimal("1000.50"));
        acct.setAcctCreditLimit(new BigDecimal("5000.00"));
        acct.setAcctCashCreditLimit(new BigDecimal("2500.00"));
        acct.setAcctOpenDate(LocalDate.of(2024, 1, 1));
        acct.setAcctExpirationDate(LocalDate.of(2029, 12, 31));
        acct.setAcctReissueDate(LocalDate.of(2029, 12, 31));
        acct.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct.setAcctCurrCycDebit(BigDecimal.ZERO);
        acct.setAcctAddrZip("12345");
        acct.setAcctGroupId("GRP001");

        Account saved = accountRepository.save(acct);
        assertEquals(99999L, saved.getAcctId());

        // Update
        saved.setAcctActiveStatus("N");
        accountRepository.save(saved);
        assertEquals("N", accountRepository.findById(99999L).get().getAcctActiveStatus());

        // Delete
        accountRepository.deleteById(99999L);
        assertFalse(accountRepository.findById(99999L).isPresent());
    }

    @Test
    void accountFindByGroupId() {
        List<Account> grouped = accountRepository.findByAcctGroupId("");
        // Most seed accounts have empty group ID
        assertNotNull(grouped);
    }

    // ===================== CARD TESTS =====================

    @Test
    void cardSeedDataLoaded() {
        List<Card> cards = cardRepository.findAll();
        assertEquals(50, cards.size(), "Expected 50 cards from carddata.txt");
    }

    @Test
    void cardFindByAcctId() {
        List<Card> cards = cardRepository.findByCardAcctId(1L);
        assertFalse(cards.isEmpty(), "Account 1 should have at least one card");
    }

    @Test
    void cardFindByActiveStatus() {
        List<Card> active = cardRepository.findByCardActiveStatus("Y");
        assertFalse(active.isEmpty(), "Should find active cards");
    }

    @Test
    void cardCrud() {
        Card card = new Card();
        card.setCardNum("9999888877776666");
        card.setCardAcctId(1L);
        card.setCardCvvCd(123);
        card.setCardEmbossedName("TEST USER");
        card.setCardExpirationDate(LocalDate.of(2029, 12, 31));
        card.setCardActiveStatus("Y");

        Card saved = cardRepository.save(card);
        assertEquals("9999888877776666", saved.getCardNum());

        saved.setCardActiveStatus("N");
        cardRepository.save(saved);
        assertEquals("N", cardRepository.findById("9999888877776666").get().getCardActiveStatus());

        cardRepository.deleteById("9999888877776666");
        assertFalse(cardRepository.findById("9999888877776666").isPresent());
    }

    // ===================== CARD XREF TESTS =====================

    @Test
    void cardXrefSeedDataLoaded() {
        List<CardXref> xrefs = cardXrefRepository.findAll();
        assertEquals(50, xrefs.size(), "Expected 50 card xrefs from cardxref.txt");
    }

    @Test
    void cardXrefFindByCustId() {
        List<CardXref> xrefs = cardXrefRepository.findByXrefCustId(1L);
        assertFalse(xrefs.isEmpty(), "Customer 1 should have card xref");
    }

    @Test
    void cardXrefFindByAcctId() {
        List<CardXref> xrefs = cardXrefRepository.findByXrefAcctId(1L);
        assertFalse(xrefs.isEmpty(), "Account 1 should have card xref");
    }

    // ===================== CUSTOMER TESTS =====================

    @Test
    void customerSeedDataLoaded() {
        List<Customer> customers = customerRepository.findAll();
        assertEquals(50, customers.size(), "Expected 50 customers from custdata.txt");
    }

    @Test
    void customerFirstRecordFieldValues() {
        Optional<Customer> opt = customerRepository.findById(1L);
        assertTrue(opt.isPresent(), "Customer with ID 1 should exist");
        Customer c = opt.get();
        assertNotNull(c.getCustFirstName());
        assertNotNull(c.getCustLastName());
    }

    @Test
    void customerFindByLastName() {
        // Get the first customer's last name to search
        Customer first = customerRepository.findById(1L).get();
        List<Customer> found = customerRepository.findByCustLastName(first.getCustLastName());
        assertFalse(found.isEmpty());
    }

    @Test
    void customerCrud() {
        Customer cust = new Customer();
        cust.setCustId(99999L);
        cust.setCustFirstName("TEST");
        cust.setCustMiddleName("M");
        cust.setCustLastName("USER");
        cust.setCustAddrLine1("123 Test St");
        cust.setCustAddrLine2("");
        cust.setCustAddrLine3("");
        cust.setCustAddrStateCd("TX");
        cust.setCustAddrCountryCd("US");
        cust.setCustAddrZip("75001");
        cust.setCustPhoneNum1("5551234567");
        cust.setCustPhoneNum2("");
        cust.setCustSsn(123456789L);
        cust.setCustGovtIssuedId("DL123456");
        cust.setCustDob(LocalDate.of(1990, 5, 15));
        cust.setCustEftAccountId("EFT001");
        cust.setCustPriCardHolderInd("Y");
        cust.setCustFicoCreditScore(750);

        Customer saved = customerRepository.save(cust);
        assertEquals(99999L, saved.getCustId());

        saved.setCustFicoCreditScore(800);
        customerRepository.save(saved);
        assertEquals(800, customerRepository.findById(99999L).get().getCustFicoCreditScore());

        customerRepository.deleteById(99999L);
        assertFalse(customerRepository.findById(99999L).isPresent());
    }

    // ===================== TRANSACTION TESTS =====================

    @Test
    void transactionRepositoryExists() {
        assertNotNull(transactionRepository);
        // Transactions may have 0 records in seed data (dailytran.txt goes to daily_transactions)
        List<Transaction> transactions = transactionRepository.findAll();
        assertNotNull(transactions);
    }

    // ===================== DAILY TRANSACTION TESTS =====================

    @Test
    void dailyTransactionSeedDataLoaded() {
        List<DailyTransaction> dailyTrans = dailyTransactionRepository.findAll();
        assertTrue(dailyTrans.size() > 0, "Expected daily transactions from dailytran.txt");
    }

    @Test
    void dailyTransactionFirstRecordFields() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        DailyTransaction first = all.get(0);
        assertNotNull(first.getDalytranId());
        assertNotNull(first.getDalytranTypeCd());
    }

    // ===================== TRANSACTION TYPE TESTS =====================

    @Test
    void transactionTypeSeedDataLoaded() {
        List<TransactionType> types = transactionTypeRepository.findAll();
        assertEquals(7, types.size(), "Expected 7 transaction types from trantype.txt");
    }

    @Test
    void transactionTypeFirstRecord() {
        List<TransactionType> all = transactionTypeRepository.findAll();
        assertFalse(all.isEmpty());
        TransactionType t = all.get(0);
        assertNotNull(t.getTranType());
        assertNotNull(t.getTranTypeDesc());
    }

    @Test
    void transactionTypeCrud() {
        TransactionType tt = new TransactionType();
        tt.setTranType("ZZ");
        tt.setTranTypeDesc("Test Type");

        transactionTypeRepository.save(tt);
        assertTrue(transactionTypeRepository.findById("ZZ").isPresent());

        transactionTypeRepository.deleteById("ZZ");
        assertFalse(transactionTypeRepository.findById("ZZ").isPresent());
    }

    // ===================== TRANSACTION CATEGORY TESTS =====================

    @Test
    void transactionCategorySeedDataLoaded() {
        List<TransactionCategory> cats = transactionCategoryRepository.findAll();
        assertEquals(18, cats.size(), "Expected 18 transaction categories from trancatg.txt");
    }

    @Test
    void transactionCategoryFindByTypeCd() {
        TransactionCategory first = transactionCategoryRepository.findAll().get(0);
        List<TransactionCategory> byType = transactionCategoryRepository.findByTranTypeCd(first.getTranTypeCd());
        assertFalse(byType.isEmpty());
    }

    // ===================== DISCLOSURE GROUP TESTS =====================

    @Test
    void disclosureGroupSeedDataLoaded() {
        List<DisclosureGroup> groups = disclosureGroupRepository.findAll();
        assertTrue(groups.size() > 0, "Expected disclosure groups from discgrp.txt");
    }

    @Test
    void disclosureGroupFindByAcctGroupId() {
        DisclosureGroup first = disclosureGroupRepository.findAll().get(0);
        List<DisclosureGroup> found = disclosureGroupRepository.findByDisAcctGroupId(first.getDisAcctGroupId());
        assertFalse(found.isEmpty());
    }

    // ===================== TRANSACTION CATEGORY BALANCE TESTS =====================

    @Test
    void transactionCategoryBalanceSeedDataLoaded() {
        List<TransactionCategoryBalance> balances = transactionCategoryBalanceRepository.findAll();
        assertTrue(balances.size() > 0, "Expected category balances from tcatbal.txt");
    }

    @Test
    void transactionCategoryBalanceFindByAcctId() {
        TransactionCategoryBalance first = transactionCategoryBalanceRepository.findAll().get(0);
        List<TransactionCategoryBalance> found = transactionCategoryBalanceRepository.findByTrancatAcctId(first.getTrancatAcctId());
        assertFalse(found.isEmpty());
    }

    // ===================== USER SECURITY TESTS =====================

    @Test
    void userSecuritySeedDataLoaded() {
        List<UserSecurity> users = userSecurityRepository.findAll();
        assertEquals(4, users.size(), "Expected 4 default users (3 regular + 1 admin)");
    }

    @Test
    void userSecurityFindById() {
        Optional<UserSecurity> opt = userSecurityRepository.findById("USER0001");
        assertTrue(opt.isPresent());
        assertEquals("FIRST01", opt.get().getSecUsrFname());
    }

    @Test
    void userSecurityFindByUserType() {
        List<UserSecurity> admins = userSecurityRepository.findBySecUsrType("A");
        assertEquals(1, admins.size(), "Expected 1 admin user");
        assertEquals("ADMIN001", admins.get(0).getSecUsrId());
    }

    @Test
    void userSecurityCrud() {
        UserSecurity user = new UserSecurity();
        user.setSecUsrId("TEST0001");
        user.setSecUsrFname("TESTFIRST");
        user.setSecUsrLname("TESTLAST");
        user.setSecUsrPwd("TESTPWD");
        user.setSecUsrType("U");

        userSecurityRepository.save(user);
        assertTrue(userSecurityRepository.findById("TEST0001").isPresent());

        user.setSecUsrType("A");
        userSecurityRepository.save(user);
        assertEquals("A", userSecurityRepository.findById("TEST0001").get().getSecUsrType());

        userSecurityRepository.deleteById("TEST0001");
        assertFalse(userSecurityRepository.findById("TEST0001").isPresent());
    }

    // ===================== CONTEXT LOAD TEST =====================

    @Test
    void contextLoads() {
        // Verifies Spring context loads, Flyway migrations succeed, and seed data is applied
        assertNotNull(accountRepository);
        assertNotNull(cardRepository);
        assertNotNull(customerRepository);
        assertNotNull(transactionTypeRepository);
        assertNotNull(userSecurityRepository);
    }
}
