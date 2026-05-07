package com.carddemo;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldLoadAll50SeedRecords() {
        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts).hasSize(50);
    }

    @Test
    void shouldVerifyFirstRecordFieldValues() {
        Optional<Account> opt = accountRepository.findById(1L);
        assertThat(opt).isPresent();

        Account account = opt.get();
        assertThat(account.getId()).isEqualTo(1L);
        assertThat(account.getActiveStatus()).isEqualTo("Y");
        assertThat(account.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(account.getCreditLimit()).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(account.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("1020.00"));
        assertThat(account.getOpenDate()).isEqualTo(LocalDate.of(2014, 11, 20));
        assertThat(account.getExpirationDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(account.getReissueDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getAddressZip()).isEqualTo("A000000000");
        assertThat(account.getGroupId()).isNull();
    }

    @Test
    void shouldVerifyLastRecordFieldValues() {
        Optional<Account> opt = accountRepository.findById(50L);
        assertThat(opt).isPresent();

        Account account = opt.get();
        assertThat(account.getId()).isEqualTo(50L);
        assertThat(account.getActiveStatus()).isEqualTo("Y");
        assertThat(account.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("492.00"));
        assertThat(account.getCreditLimit()).isEqualByComparingTo(new BigDecimal("6169.00"));
        assertThat(account.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("4587.00"));
        assertThat(account.getOpenDate()).isEqualTo(LocalDate.of(2011, 4, 22));
        assertThat(account.getExpirationDate()).isEqualTo(LocalDate.of(2023, 3, 9));
        assertThat(account.getReissueDate()).isEqualTo(LocalDate.of(2023, 3, 9));
    }

    @Test
    void shouldCreateNewAccount() {
        Account newAccount = new Account();
        newAccount.setId(999L);
        newAccount.setActiveStatus("Y");
        newAccount.setCurrentBalance(new BigDecimal("500.00"));
        newAccount.setCreditLimit(new BigDecimal("10000.00"));
        newAccount.setCashCreditLimit(new BigDecimal("5000.00"));
        newAccount.setOpenDate(LocalDate.of(2024, 1, 1));
        newAccount.setExpirationDate(LocalDate.of(2029, 1, 1));
        newAccount.setReissueDate(LocalDate.of(2029, 1, 1));
        newAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        newAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        newAccount.setAddressZip("12345");
        newAccount.setGroupId("GRP001");

        Account saved = accountRepository.save(newAccount);
        assertThat(saved.getId()).isEqualTo(999L);

        Optional<Account> found = accountRepository.findById(999L);
        assertThat(found).isPresent();
        assertThat(found.get().getActiveStatus()).isEqualTo("Y");
        assertThat(found.get().getGroupId()).isEqualTo("GRP001");
    }

    @Test
    void shouldUpdateExistingAccount() {
        Optional<Account> opt = accountRepository.findById(1L);
        assertThat(opt).isPresent();

        Account account = opt.get();
        account.setActiveStatus("N");
        account.setCurrentBalance(new BigDecimal("999.99"));
        accountRepository.save(account);

        Optional<Account> updated = accountRepository.findById(1L);
        assertThat(updated).isPresent();
        assertThat(updated.get().getActiveStatus()).isEqualTo("N");
        assertThat(updated.get().getCurrentBalance()).isEqualByComparingTo(new BigDecimal("999.99"));
    }

    @Test
    void shouldDeleteAccount() {
        assertThat(accountRepository.findById(1L)).isPresent();

        accountRepository.deleteById(1L);

        assertThat(accountRepository.findById(1L)).isEmpty();
        assertThat(accountRepository.findAll()).hasSize(49);
    }

    @Test
    void shouldFindByActiveStatus() {
        List<Account> activeAccounts = accountRepository.findByActiveStatus("Y");
        assertThat(activeAccounts).hasSize(50);

        List<Account> inactiveAccounts = accountRepository.findByActiveStatus("N");
        assertThat(inactiveAccounts).isEmpty();
    }

    @Test
    void shouldFindByGroupId() {
        Account account = accountRepository.findById(1L).orElseThrow();
        account.setGroupId("TESTGRP");
        accountRepository.save(account);

        List<Account> found = accountRepository.findByGroupId("TESTGRP");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void shouldReturnEmptyForNonexistentId() {
        Optional<Account> opt = accountRepository.findById(99999L);
        assertThat(opt).isEmpty();
    }

    @Test
    void shouldVerifyAllFieldsRoundTrip() {
        Account account = new Account(
                1000L,
                "N",
                new BigDecimal("12345.67"),
                new BigDecimal("99999.99"),
                new BigDecimal("50000.00"),
                LocalDate.of(2020, 6, 15),
                LocalDate.of(2030, 6, 15),
                LocalDate.of(2025, 6, 15),
                new BigDecimal("100.50"),
                new BigDecimal("200.75"),
                "90210",
                "PREMIUM"
        );

        accountRepository.save(account);

        Account loaded = accountRepository.findById(1000L).orElseThrow();
        assertThat(loaded.getId()).isEqualTo(1000L);
        assertThat(loaded.getActiveStatus()).isEqualTo("N");
        assertThat(loaded.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("12345.67"));
        assertThat(loaded.getCreditLimit()).isEqualByComparingTo(new BigDecimal("99999.99"));
        assertThat(loaded.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(loaded.getOpenDate()).isEqualTo(LocalDate.of(2020, 6, 15));
        assertThat(loaded.getExpirationDate()).isEqualTo(LocalDate.of(2030, 6, 15));
        assertThat(loaded.getReissueDate()).isEqualTo(LocalDate.of(2025, 6, 15));
        assertThat(loaded.getCurrentCycleCredit()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(loaded.getCurrentCycleDebit()).isEqualByComparingTo(new BigDecimal("200.75"));
        assertThat(loaded.getAddressZip()).isEqualTo("90210");
        assertThat(loaded.getGroupId()).isEqualTo("PREMIUM");
    }
}
