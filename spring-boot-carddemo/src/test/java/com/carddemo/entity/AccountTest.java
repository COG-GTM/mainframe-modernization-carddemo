package com.carddemo.entity;

import com.carddemo.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for Account entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from acctdata.txt, CRUD operations, and custom finders.
 */
@DataJpaTest
class AccountTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void seedDataLoadsExpectedNumberOfAccounts() {
        List<Account> all = accountRepository.findAll();
        assertThat(all).hasSize(50);
    }

    @Test
    void firstAccountFieldsMatchSeedData() {
        // acctdata.txt record 1: acct_id=1, status=Y, bal=194.00, limit=2020.00, etc.
        Optional<Account> opt = accountRepository.findById(1L);
        assertThat(opt).isPresent();
        Account acct = opt.get();
        assertThat(acct.getAcctActiveStatus()).isEqualTo("Y");
        assertThat(acct.getAcctCurrBal()).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(acct.getAcctCreditLimit()).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(acct.getAcctCashCreditLimit()).isEqualByComparingTo(new BigDecimal("1020.00"));
        assertThat(acct.getAcctOpenDate()).isEqualTo(LocalDate.of(2014, 11, 20));
        assertThat(acct.getAcctExpirationDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(acct.getAcctReissueDate()).isEqualTo(LocalDate.of(2025, 5, 20));
    }

    @Test
    void findByActiveStatusReturnsAllActiveAccounts() {
        List<Account> active = accountRepository.findByAcctActiveStatus("Y");
        assertThat(active).isNotEmpty();
        active.forEach(a -> assertThat(a.getAcctActiveStatus()).isEqualTo("Y"));
    }

    @Test
    void createAccountPersistsAndReadsBack() {
        Account newAcct = new Account();
        newAcct.setAcctId(99999L);
        newAcct.setAcctActiveStatus("Y");
        newAcct.setAcctCurrBal(new BigDecimal("1000.50"));
        newAcct.setAcctCreditLimit(new BigDecimal("5000.00"));
        newAcct.setAcctCashCreditLimit(new BigDecimal("2500.00"));
        newAcct.setAcctOpenDate(LocalDate.of(2024, 1, 1));
        newAcct.setAcctExpirationDate(LocalDate.of(2029, 1, 1));
        newAcct.setAcctReissueDate(LocalDate.of(2029, 1, 1));
        newAcct.setAcctCurrCycCredit(BigDecimal.ZERO);
        newAcct.setAcctCurrCycDebit(BigDecimal.ZERO);
        newAcct.setAcctAddrZip("10001");
        newAcct.setAcctGroupId("GRP001");

        accountRepository.save(newAcct);

        Optional<Account> found = accountRepository.findById(99999L);
        assertThat(found).isPresent();
        assertThat(found.get().getAcctCurrBal()).isEqualByComparingTo(new BigDecimal("1000.50"));
        assertThat(found.get().getAcctGroupId()).isEqualTo("GRP001");
    }

    @Test
    void updateAccountBalance() {
        // Mirrors COBOL REWRITE operation in COACTUPC
        Optional<Account> opt = accountRepository.findById(1L);
        assertThat(opt).isPresent();
        Account acct = opt.get();
        BigDecimal originalBal = acct.getAcctCurrBal();
        acct.setAcctCurrBal(originalBal.add(new BigDecimal("100.00")));
        accountRepository.save(acct);

        Account updated = accountRepository.findById(1L).orElseThrow();
        assertThat(updated.getAcctCurrBal()).isEqualByComparingTo(originalBal.add(new BigDecimal("100.00")));
    }

    @Test
    void deleteAccountRemovesFromDatabase() {
        assertThat(accountRepository.findById(50L)).isPresent();
        accountRepository.deleteById(50L);
        assertThat(accountRepository.findById(50L)).isEmpty();
        assertThat(accountRepository.findAll()).hasSize(49);
    }

    @Test
    void findByGroupIdReturnsMatchingAccounts() {
        List<Account> results = accountRepository.findByAcctGroupId("");
        // Empty group ID is common in seed data
        assertThat(results).isNotEmpty();
    }

    @Test
    void findByZipCodeReturnsMatchingAccounts() {
        List<Account> results = accountRepository.findByAcctAddrZip("A000000000");
        assertThat(results).isNotEmpty();
    }

    @Test
    void accountFieldsAreNotNull() {
        Account acct = accountRepository.findById(1L).orElseThrow();
        assertThat(acct.getAcctId()).isNotNull();
        assertThat(acct.getAcctActiveStatus()).isNotNull();
        assertThat(acct.getAcctCurrBal()).isNotNull();
        assertThat(acct.getAcctCreditLimit()).isNotNull();
        assertThat(acct.getAcctOpenDate()).isNotNull();
    }

    @Test
    void allAccountsHavePositiveCreditLimit() {
        List<Account> all = accountRepository.findAll();
        all.forEach(a -> assertThat(a.getAcctCreditLimit()).isGreaterThan(BigDecimal.ZERO));
    }

    @Test
    void accountIdIsUniqueAcrossAllRecords() {
        List<Account> all = accountRepository.findAll();
        long distinctIds = all.stream().map(Account::getAcctId).distinct().count();
        assertThat(distinctIds).isEqualTo(all.size());
    }
}
