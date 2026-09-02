package com.carddemo.repository;

import com.carddemo.CardDemoApplication;
import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.User;
import com.carddemo.repository.memory.Reseedable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CardDemoApplication.class)
class InMemoryRepositoriesTest {

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private CardRepository cards;

    @Autowired
    private CardXrefRepository xrefs;

    @Autowired
    private CustomerRepository customers;

    @Autowired
    private TransactionRepository transactions;

    @Autowired
    private UserRepository users;

    @Autowired
    private DisclosureGroupRepository disclosureGroups;

    @Autowired
    private TransactionCategoryBalanceRepository categoryBalances;

    @Autowired
    private List<Reseedable> reseedables;

    @AfterEach
    void restoreMockData() {
        reseedables.forEach(Reseedable::reseed);
    }

    @Test
    void seedsEveryRepositoryFromTheSampleData() {
        assertThat(accounts.findAll()).hasSize(50);
        assertThat(cards.findAll()).hasSize(50);
        assertThat(xrefs.findAll()).hasSize(50);
        assertThat(customers.findAll()).hasSize(50);
        assertThat(transactions.findAll()).hasSize(300);
        assertThat(users.findAll()).hasSize(10);
        assertThat(disclosureGroups.findAll()).hasSize(51);
        assertThat(categoryBalances.findAll()).hasSize(50);
    }

    @Test
    void looksUpRecordsByUnpaddedKeys() {
        assertThat(accounts.findById("1")).isPresent();
        assertThat(accounts.findById("00000000001")).isPresent();
        assertThat(customers.findById("1")).isPresent();
        assertThat(accounts.findById("99999999999")).isEmpty();
    }

    @Test
    void resolvesCardToAccountAndCustomerThroughTheCrossReference() {
        CardXref xref = xrefs.findAll().get(0);
        assertThat(cards.findByCardNumber(xref.getCardNumber())).isPresent();
        assertThat(accounts.findById(xref.getAccountId())).isPresent();
        assertThat(customers.findById(xref.getCustomerId())).isPresent();
        assertThat(cards.findByAccountId(xref.getAccountId())).isNotEmpty();
    }

    @Test
    void savesMutatedAccountsAndReseedsBetweenTests() {
        Account account = accounts.findById("1").orElseThrow();
        account.setCurrentBalance(new BigDecimal("1.23"));
        accounts.save(account);
        assertThat(accounts.findById("1").orElseThrow().getCurrentBalance()).isEqualByComparingTo("1.23");

        reseedables.forEach(Reseedable::reseed);
        assertThat(accounts.findById("1").orElseThrow().getCurrentBalance()).isEqualByComparingTo("194.00");
    }

    @Test
    void supportsUserCrud() {
        users.save(new User("USER9999", "TEST", "USER", "PASSWORD", User.TYPE_REGULAR));
        assertThat(users.existsById("USER9999")).isTrue();
        assertThat(users.deleteById("USER9999")).isTrue();
        assertThat(users.deleteById("USER9999")).isFalse();
    }
}
