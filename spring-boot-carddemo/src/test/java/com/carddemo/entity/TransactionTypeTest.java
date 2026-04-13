package com.carddemo.entity;

import com.carddemo.repository.TransactionTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for TransactionType entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from trantype.txt, CRUD operations.
 */
@DataJpaTest
class TransactionTypeTest {

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Test
    void seedDataLoadsExpectedNumberOfTransactionTypes() {
        List<TransactionType> all = transactionTypeRepository.findAll();
        assertThat(all).hasSize(7);
    }

    @Test
    void firstTransactionTypeFieldsMatchSeedData() {
        List<TransactionType> all = transactionTypeRepository.findAll();
        assertThat(all).isNotEmpty();
        TransactionType first = all.get(0);
        assertThat(first.getTranType()).isNotBlank();
        assertThat(first.getTranTypeDesc()).isNotNull();
    }

    @Test
    void createTransactionTypePersistsAndReadsBack() {
        TransactionType tt = new TransactionType();
        tt.setTranType("ZZ");
        tt.setTranTypeDesc("Test Transaction Type");

        transactionTypeRepository.save(tt);

        Optional<TransactionType> found = transactionTypeRepository.findById("ZZ");
        assertThat(found).isPresent();
        assertThat(found.get().getTranTypeDesc()).isEqualTo("Test Transaction Type");
    }

    @Test
    void updateTransactionTypeDescription() {
        List<TransactionType> all = transactionTypeRepository.findAll();
        TransactionType tt = all.get(0);
        tt.setTranTypeDesc("UPDATED DESCRIPTION");
        transactionTypeRepository.save(tt);

        TransactionType updated = transactionTypeRepository.findById(tt.getTranType()).orElseThrow();
        assertThat(updated.getTranTypeDesc()).isEqualTo("UPDATED DESCRIPTION");
    }

    @Test
    void deleteTransactionTypeRemovesFromDatabase() {
        List<TransactionType> before = transactionTypeRepository.findAll();
        int beforeSize = before.size();
        transactionTypeRepository.deleteById(before.get(0).getTranType());
        assertThat(transactionTypeRepository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    void transactionTypeCodeIsUniqueAcrossAllRecords() {
        List<TransactionType> all = transactionTypeRepository.findAll();
        long distinctTypes = all.stream().map(TransactionType::getTranType).distinct().count();
        assertThat(distinctTypes).isEqualTo(all.size());
    }

    @Test
    void allTransactionTypesHaveDescriptions() {
        List<TransactionType> all = transactionTypeRepository.findAll();
        all.forEach(tt -> assertThat(tt.getTranTypeDesc()).isNotNull());
    }

    @Test
    void transactionTypeCodesAreShortStrings() {
        List<TransactionType> all = transactionTypeRepository.findAll();
        all.forEach(tt -> assertThat(tt.getTranType().trim().length()).isLessThanOrEqualTo(2));
    }
}
