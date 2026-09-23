package com.carddemo.loader;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.AbstractIntegrationTest;
import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.SecUserRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LegacyDataLoaderTest extends AbstractIntegrationTest {

    @Autowired
    private LegacyDataLoader loader;
    @Autowired
    private AccountRepository accounts;
    @Autowired
    private CardRepository cards;
    @Autowired
    private CardXrefRepository xrefs;
    @Autowired
    private CustomerRepository customers;
    @Autowired
    private SecUserRepository users;
    @Autowired
    private DailyTransactionRepository dailyTransactions;

    @Test
    void loadsEveryLegacyExtract() {
        Map<String, Integer> counts = loader.loadAll();

        assertThat(counts.values()).allSatisfy(count -> assertThat(count).isPositive());
        assertThat(users.count()).isEqualTo(10);
        assertThat(accounts.count()).isEqualTo(cards.count());
        assertThat(xrefs.count()).isPositive();
        assertThat(customers.count()).isPositive();
        assertThat(dailyTransactions.count()).isPositive();

        Account account = accounts.findById(1L).orElseThrow();
        assertThat(account.getActiveStatus()).isEqualTo("Y");
        assertThat(account.getOpenDate()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(account.getCreditLimit()).isGreaterThan(BigDecimal.ZERO);

        CardXref xref = xrefs.findFirstByAccountIdOrderByCardNumber(1L).orElseThrow();
        assertThat(xref.getCardNumber()).hasSize(16);
        assertThat(customers.findById(xref.getCustomerId())).isPresent();
        assertThat(cards.findById(xref.getCardNumber())).isPresent();
    }
}
