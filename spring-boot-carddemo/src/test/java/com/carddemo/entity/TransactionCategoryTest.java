package com.carddemo.entity;

import com.carddemo.repository.TransactionCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for TransactionCategory entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from trancatg.txt, CRUD operations, and composite key behavior.
 */
@DataJpaTest
class TransactionCategoryTest {

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    @Test
    void seedDataLoadsExpectedNumberOfCategories() {
        List<TransactionCategory> all = transactionCategoryRepository.findAll();
        assertThat(all).hasSize(18);
    }

    @Test
    void firstCategoryFieldsMatchSeedData() {
        List<TransactionCategory> all = transactionCategoryRepository.findAll();
        assertThat(all).isNotEmpty();
        TransactionCategory first = all.get(0);
        assertThat(first.getTranTypeCd()).isNotBlank();
        assertThat(first.getTranCatCd()).isGreaterThanOrEqualTo(0);
        assertThat(first.getTranCatTypeDesc()).isNotNull();
    }

    @Test
    void findByTypeCdReturnsMatchingCategories() {
        List<TransactionCategory> all = transactionCategoryRepository.findAll();
        String typeCd = all.get(0).getTranTypeCd().trim();
        List<TransactionCategory> results = transactionCategoryRepository.findByTranTypeCd(typeCd);
        assertThat(results).isNotEmpty();
        results.forEach(tc -> assertThat(tc.getTranTypeCd().trim()).isEqualTo(typeCd));
    }

    @Test
    void createCategoryPersistsAndReadsBack() {
        TransactionCategory tc = new TransactionCategory();
        tc.setTranTypeCd("ZZ");
        tc.setTranCatCd(9999);
        tc.setTranCatTypeDesc("Test Category");

        transactionCategoryRepository.save(tc);

        TransactionCategory.TransactionCategoryId id = new TransactionCategory.TransactionCategoryId();
        id.setTranTypeCd("ZZ");
        id.setTranCatCd(9999);

        assertThat(transactionCategoryRepository.findById(id)).isPresent();
    }

    @Test
    void updateCategoryDescription() {
        List<TransactionCategory> all = transactionCategoryRepository.findAll();
        TransactionCategory tc = all.get(0);
        tc.setTranCatTypeDesc("UPDATED DESC");
        transactionCategoryRepository.save(tc);

        TransactionCategory.TransactionCategoryId id = new TransactionCategory.TransactionCategoryId();
        id.setTranTypeCd(tc.getTranTypeCd());
        id.setTranCatCd(tc.getTranCatCd());

        TransactionCategory updated = transactionCategoryRepository.findById(id).orElseThrow();
        assertThat(updated.getTranCatTypeDesc()).isEqualTo("UPDATED DESC");
    }

    @Test
    void deleteCategoryRemovesFromDatabase() {
        List<TransactionCategory> before = transactionCategoryRepository.findAll();
        int beforeSize = before.size();
        TransactionCategory tc = before.get(0);

        TransactionCategory.TransactionCategoryId id = new TransactionCategory.TransactionCategoryId();
        id.setTranTypeCd(tc.getTranTypeCd());
        id.setTranCatCd(tc.getTranCatCd());

        transactionCategoryRepository.deleteById(id);
        assertThat(transactionCategoryRepository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    void compositeKeyIsUniqueAcrossAllRecords() {
        List<TransactionCategory> all = transactionCategoryRepository.findAll();
        long distinctKeys = all.stream()
                .map(tc -> tc.getTranTypeCd() + "|" + tc.getTranCatCd())
                .distinct().count();
        assertThat(distinctKeys).isEqualTo(all.size());
    }

    @Test
    void allCategoriesHaveDescriptions() {
        List<TransactionCategory> all = transactionCategoryRepository.findAll();
        all.forEach(tc -> assertThat(tc.getTranCatTypeDesc()).isNotNull());
    }
}
