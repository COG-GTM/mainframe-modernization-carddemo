package com.carddemo.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Verifies the Account entity mapping, Flyway seed data, and repository finders
 * against an H2 (PostgreSQL mode) database loaded by the real Flyway migrations.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    @Test
    void seedDataLoadsExpectedRecordCount() {
        assertThat(repository.count()).isEqualTo(50);
    }

    @Test
    void firstSeedRecordFieldsMatchParsedValues() {
        Account acct = repository.findById(1L).orElseThrow();
        assertThat(acct.getAcctActiveStatus()).isEqualTo("Y");
        assertThat(acct.getAcctCurrBal()).isEqualByComparingTo("194.00");
        assertThat(acct.getAcctCreditLimit()).isEqualByComparingTo("2020.00");
        assertThat(acct.getAcctCashCreditLimit()).isEqualByComparingTo("1020.00");
        assertThat(acct.getAcctOpenDate()).isEqualTo(LocalDate.of(2014, 11, 20));
        assertThat(acct.getAcctExpirationDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(acct.getAcctReissueDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(acct.getAcctCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(acct.getAcctCurrCycDebit()).isEqualByComparingTo("0.00");
        assertThat(acct.getAcctAddrZip()).isEqualTo("A000000000");
    }

    @Test
    void findByIdReturnsEmptyForUnknownKey() {
        assertThat(repository.findById(999999999L)).isEmpty();
    }

    @Test
    void findByActiveStatusReturnsAllSeededAccounts() {
        List<Account> active = repository.findByAcctActiveStatus("Y");
        assertThat(active).hasSize(50);
        assertThat(repository.findByAcctActiveStatus("N")).isEmpty();
    }

    @Test
    void createPersistsNewAccount() {
        Account acct = newAccount(70000000001L, " G0001");
        repository.save(acct);

        Optional<Account> reloaded = repository.findById(70000000001L);
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getAcctCurrBal()).isEqualByComparingTo("123.45");
    }

    @Test
    void updateModifiesExistingAccount() {
        Account acct = repository.findById(2L).orElseThrow();
        acct.setAcctCurrBal(new BigDecimal("999.99"));
        repository.saveAndFlush(acct);

        assertThat(repository.findById(2L).orElseThrow().getAcctCurrBal())
                .isEqualByComparingTo("999.99");
    }

    @Test
    void deleteRemovesAccount() {
        repository.deleteById(3L);
        assertThat(repository.findById(3L)).isEmpty();
        assertThat(repository.count()).isEqualTo(49);
    }

    @Test
    void findByGroupIdReturnsMatchingAccounts() {
        repository.save(newAccount(70000000002L, "GRP-A"));
        repository.save(newAccount(70000000003L, "GRP-A"));

        assertThat(repository.findByAcctGroupId("GRP-A")).hasSize(2);
        assertThat(repository.findByAcctGroupId("NOPE")).isEmpty();
    }

    private static Account newAccount(Long id, String groupId) {
        Account acct = new Account();
        acct.setAcctId(id);
        acct.setAcctActiveStatus("Y");
        acct.setAcctCurrBal(new BigDecimal("123.45"));
        acct.setAcctCreditLimit(new BigDecimal("5000.00"));
        acct.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        acct.setAcctOpenDate(LocalDate.of(2020, 1, 1));
        acct.setAcctExpirationDate(LocalDate.of(2030, 1, 1));
        acct.setAcctReissueDate(LocalDate.of(2025, 1, 1));
        acct.setAcctCurrCycCredit(new BigDecimal("0.00"));
        acct.setAcctCurrCycDebit(new BigDecimal("0.00"));
        acct.setAcctAddrZip("99999");
        acct.setAcctGroupId(groupId);
        return acct;
    }
}
