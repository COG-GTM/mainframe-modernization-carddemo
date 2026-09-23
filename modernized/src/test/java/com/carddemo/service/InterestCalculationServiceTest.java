package com.carddemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.AbstractIntegrationTest;
import com.carddemo.domain.Account;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InterestCalculationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private InterestCalculationService interest;
    @Autowired
    private AccountRepository accounts;
    @Autowired
    private DisclosureGroupRepository disclosureGroups;
    @Autowired
    private TransactionCategoryBalanceRepository categoryBalances;
    @Autowired
    private TransactionRepository transactions;

    @BeforeEach
    void seed() {
        transactions.deleteAll();
        categoryBalances.deleteAll();
        disclosureGroups.deleteAll();
        accounts.deleteAll();

        Account account = new Account();
        account.setId(800000001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("120.00"));
        account.setCurrentCycleDebit(new BigDecimal("30.00"));
        account.setGroupId("ZEROAPR");
        accounts.save(account);

        categoryBalances.save(new TransactionCategoryBalance(
                new TransactionCategoryBalanceId(800000001L, "01", 1), new BigDecimal("1200.00")));
        disclosureGroups.save(group("ZEROAPR", new BigDecimal("12.00")));
        disclosureGroups.save(group("DEFAULT", new BigDecimal("24.00")));
    }

    private DisclosureGroup group(String groupId, BigDecimal rate) {
        DisclosureGroup group = new DisclosureGroup();
        group.setId(new DisclosureGroupId(groupId, "01", 1));
        group.setInterestRate(rate);
        return group;
    }

    @Test
    void appliesGroupRateAndResetsCycleTotals() {
        BigDecimal total = interest.calculateForAccount(800000001L);

        // 1200.00 * 12.00 / 1200 = 12.00
        assertThat(total).isEqualByComparingTo("12.00");
        Account account = accounts.findById(800000001L).orElseThrow();
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("1012.00");
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("0.00");
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("0.00");
        assertThat(transactions.count()).isEqualTo(1);
    }

    @Test
    void fallsBackToDefaultDisclosureGroup() {
        Account account = accounts.findById(800000001L).orElseThrow();
        account.setGroupId("UNKNOWN");
        accounts.save(account);

        // 1200.00 * 24.00 / 1200 = 24.00
        assertThat(interest.calculateForAccount(800000001L)).isEqualByComparingTo("24.00");
    }
}
