package com.carddemo.repository;

import com.carddemo.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AccountRepository}.
 * <p>
 * Uses the H2 in-memory database with Flyway migrations (V1 schema + V2 seed data).
 */
@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    // ---------------------------------------------------------------
    // Seed data verification
    // ---------------------------------------------------------------

    @Test
    void seedDataShouldLoadAllRecords() {
        List<Account> all = accountRepository.findAll();
        assertThat(all).hasSize(50);
    }

    @Test
    void firstRecordShouldMatchExpectedValues() {
        Optional<Account> opt = accountRepository.findById(1L);
        assertThat(opt).isPresent();

        Account acct = opt.get();
        assertThat(acct.getAcctId()).isEqualTo(1L);
        assertThat(acct.getActiveStatus()).isEqualTo("Y");
        assertThat(acct.getCurrBal()).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(acct.getCreditLimit()).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(acct.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("1020.00"));
        assertThat(acct.getOpenDate()).isEqualTo(LocalDate.of(2014, 11, 20));
        assertThat(acct.getExpirationDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(acct.getReissueDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(acct.getCurrCycCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(acct.getCurrCycDebit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(acct.getGroupId()).isEqualTo("A000000000");
    }

    @Test
    void lastRecordShouldExist() {
        Optional<Account> opt = accountRepository.findById(50L);
        assertThat(opt).isPresent();

        Account acct = opt.get();
        assertThat(acct.getActiveStatus()).isEqualTo("Y");
        assertThat(acct.getCurrBal()).isEqualByComparingTo(new BigDecimal("492.00"));
        assertThat(acct.getGroupId()).isEqualTo("A000000000");
    }

    // ---------------------------------------------------------------
    // CRUD operations
    // ---------------------------------------------------------------

    @Test
    void shouldCreateNewAccount() {
        Account newAcct = new Account();
        newAcct.setAcctId(99999L);
        newAcct.setActiveStatus("Y");
        newAcct.setCurrBal(new BigDecimal("500.00"));
        newAcct.setCreditLimit(new BigDecimal("10000.00"));
        newAcct.setCashCreditLimit(new BigDecimal("5000.00"));
        newAcct.setOpenDate(LocalDate.of(2024, 1, 1));
        newAcct.setExpirationDate(LocalDate.of(2027, 1, 1));
        newAcct.setReissueDate(LocalDate.of(2027, 1, 1));
        newAcct.setCurrCycCredit(BigDecimal.ZERO);
        newAcct.setCurrCycDebit(BigDecimal.ZERO);
        newAcct.setGroupId("B000000001");

        Account saved = accountRepository.save(newAcct);
        assertThat(saved.getAcctId()).isEqualTo(99999L);

        Optional<Account> found = accountRepository.findById(99999L);
        assertThat(found).isPresent();
        assertThat(found.get().getCurrBal()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldUpdateExistingAccount() {
        Optional<Account> opt = accountRepository.findById(1L);
        assertThat(opt).isPresent();

        Account acct = opt.get();
        acct.setCurrBal(new BigDecimal("999.99"));
        accountRepository.save(acct);

        Account updated = accountRepository.findById(1L).orElseThrow();
        assertThat(updated.getCurrBal()).isEqualByComparingTo(new BigDecimal("999.99"));
    }

    @Test
    void shouldDeleteAccount() {
        accountRepository.deleteById(1L);
        Optional<Account> opt = accountRepository.findById(1L);
        assertThat(opt).isEmpty();
    }

    // ---------------------------------------------------------------
    // Custom finder methods
    // ---------------------------------------------------------------

    @Test
    void findByActiveStatusShouldReturnMatchingRecords() {
        List<Account> activeAccounts = accountRepository.findByActiveStatus("Y");
        assertThat(activeAccounts).isNotEmpty();
        assertThat(activeAccounts).allMatch(a -> "Y".equals(a.getActiveStatus()));
    }

    @Test
    void findByActiveStatusShouldReturnEmptyForInactive() {
        // All seed records have status 'Y', so 'N' should return empty
        List<Account> inactive = accountRepository.findByActiveStatus("N");
        assertThat(inactive).isEmpty();
    }

    @Test
    void findByGroupIdShouldReturnMatchingRecords() {
        List<Account> groupAccounts = accountRepository.findByGroupId("A000000000");
        assertThat(groupAccounts).isNotEmpty();
        assertThat(groupAccounts).allMatch(a -> "A000000000".equals(a.getGroupId()));
    }

    @Test
    void findByGroupIdShouldReturnEmptyForUnknownGroup() {
        List<Account> unknown = accountRepository.findByGroupId("ZZZZZZZZZZ");
        assertThat(unknown).isEmpty();
    }
}
