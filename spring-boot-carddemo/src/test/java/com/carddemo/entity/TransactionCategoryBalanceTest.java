package com.carddemo.entity;

import com.carddemo.repository.TransactionCategoryBalanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for TransactionCategoryBalance entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from tcatbal.txt, CRUD operations, and composite key behavior.
 */
@DataJpaTest
class TransactionCategoryBalanceTest {

    @Autowired
    private TransactionCategoryBalanceRepository repository;

    @Test
    void seedDataLoadsExpectedNumberOfBalances() {
        List<TransactionCategoryBalance> all = repository.findAll();
        assertThat(all).hasSize(50);
    }

    @Test
    void firstBalanceFieldsMatchSeedData() {
        List<TransactionCategoryBalance> all = repository.findAll();
        assertThat(all).isNotEmpty();
        TransactionCategoryBalance first = all.get(0);
        assertThat(first.getTrancatAcctId()).isGreaterThan(0);
        assertThat(first.getTrancatTypeCd()).isNotBlank();
        assertThat(first.getTrancatCd()).isGreaterThanOrEqualTo(0);
        assertThat(first.getTranCatBal()).isNotNull();
    }

    @Test
    void findByAcctIdReturnsMatchingBalances() {
        List<TransactionCategoryBalance> all = repository.findAll();
        Long acctId = all.get(0).getTrancatAcctId();
        List<TransactionCategoryBalance> results = repository.findByTrancatAcctId(acctId);
        assertThat(results).isNotEmpty();
        results.forEach(b -> assertThat(b.getTrancatAcctId()).isEqualTo(acctId));
    }

    @Test
    void createBalancePersistsAndReadsBack() {
        TransactionCategoryBalance bal = new TransactionCategoryBalance();
        bal.setTrancatAcctId(99999L);
        bal.setTrancatTypeCd("ZZ");
        bal.setTrancatCd(9999);
        bal.setTranCatBal(new BigDecimal("1234.56"));

        repository.save(bal);

        TransactionCategoryBalance.TransactionCategoryBalanceId id = new TransactionCategoryBalance.TransactionCategoryBalanceId();
        id.setTrancatAcctId(99999L);
        id.setTrancatTypeCd("ZZ");
        id.setTrancatCd(9999);

        assertThat(repository.findById(id)).isPresent();
        assertThat(repository.findById(id).get().getTranCatBal())
                .isEqualByComparingTo(new BigDecimal("1234.56"));
    }

    @Test
    void updateBalanceAmount() {
        List<TransactionCategoryBalance> all = repository.findAll();
        TransactionCategoryBalance bal = all.get(0);
        bal.setTranCatBal(new BigDecimal("9999.99"));
        repository.save(bal);

        TransactionCategoryBalance.TransactionCategoryBalanceId id = new TransactionCategoryBalance.TransactionCategoryBalanceId();
        id.setTrancatAcctId(bal.getTrancatAcctId());
        id.setTrancatTypeCd(bal.getTrancatTypeCd());
        id.setTrancatCd(bal.getTrancatCd());

        TransactionCategoryBalance updated = repository.findById(id).orElseThrow();
        assertThat(updated.getTranCatBal()).isEqualByComparingTo(new BigDecimal("9999.99"));
    }

    @Test
    void deleteBalanceRemovesFromDatabase() {
        List<TransactionCategoryBalance> before = repository.findAll();
        int beforeSize = before.size();
        TransactionCategoryBalance bal = before.get(0);

        TransactionCategoryBalance.TransactionCategoryBalanceId id = new TransactionCategoryBalance.TransactionCategoryBalanceId();
        id.setTrancatAcctId(bal.getTrancatAcctId());
        id.setTrancatTypeCd(bal.getTrancatTypeCd());
        id.setTrancatCd(bal.getTrancatCd());

        repository.deleteById(id);
        assertThat(repository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    void compositeKeyIsUniqueAcrossAllRecords() {
        List<TransactionCategoryBalance> all = repository.findAll();
        long distinctKeys = all.stream()
                .map(b -> b.getTrancatAcctId() + "|" + b.getTrancatTypeCd() + "|" + b.getTrancatCd())
                .distinct().count();
        assertThat(distinctKeys).isEqualTo(all.size());
    }

    @Test
    void allBalancesHaveNonNullAmounts() {
        List<TransactionCategoryBalance> all = repository.findAll();
        all.forEach(b -> assertThat(b.getTranCatBal()).isNotNull());
    }
}
