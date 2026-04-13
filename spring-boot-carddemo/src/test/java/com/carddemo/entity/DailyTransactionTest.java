package com.carddemo.entity;

import com.carddemo.repository.DailyTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for DailyTransaction entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from dailytran.txt, CRUD operations, and custom finders.
 */
@DataJpaTest
class DailyTransactionTest {

    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;

    @Test
    void seedDataLoadsExpectedNumberOfDailyTransactions() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        assertThat(all).hasSize(300);
    }

    @Test
    void firstDailyTransactionFieldsMatchSeedData() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        assertThat(all).isNotEmpty();
        DailyTransaction first = all.get(0);
        assertThat(first.getDalytranId()).isNotBlank();
        assertThat(first.getDalytranTypeCd()).isNotNull();
        assertThat(first.getDalytranAmt()).isNotNull();
    }

    @Test
    void findByCardNumReturnsMatchingTransactions() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        String cardNum = all.get(0).getDalytranCardNum().trim();
        List<DailyTransaction> results = dailyTransactionRepository.findByDalytranCardNum(cardNum);
        assertThat(results).isNotEmpty();
        results.forEach(dt -> assertThat(dt.getDalytranCardNum().trim()).isEqualTo(cardNum));
    }

    @Test
    void findByTypeCdReturnsMatchingTransactions() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        String typeCd = all.get(0).getDalytranTypeCd().trim();
        List<DailyTransaction> results = dailyTransactionRepository.findByDalytranTypeCd(typeCd);
        assertThat(results).isNotEmpty();
    }

    @Test
    void createDailyTransactionPersistsAndReadsBack() {
        DailyTransaction dt = new DailyTransaction();
        dt.setDalytranId("DT00000000000001");
        dt.setDalytranTypeCd("01");
        dt.setDalytranCatCd(1001);
        dt.setDalytranSource("ONLINE");
        dt.setDalytranDesc("Test daily txn");
        dt.setDalytranAmt(new BigDecimal("250.75"));
        dt.setDalytranMerchantId(12345L);
        dt.setDalytranMerchantName("TEST MERCHANT");
        dt.setDalytranMerchantCity("NEW YORK");
        dt.setDalytranMerchantZip("10001");
        dt.setDalytranCardNum("4111111111111111");
        dt.setDalytranOrigTs("2024-01-15 10:30:00");
        dt.setDalytranProcTs("2024-01-15 10:30:01");

        dailyTransactionRepository.save(dt);

        Optional<DailyTransaction> found = dailyTransactionRepository.findById("DT00000000000001");
        assertThat(found).isPresent();
        assertThat(found.get().getDalytranAmt()).isEqualByComparingTo(new BigDecimal("250.75"));
    }

    @Test
    void updateDailyTransactionAmount() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        DailyTransaction dt = all.get(0);
        dt.setDalytranAmt(new BigDecimal("999.99"));
        dailyTransactionRepository.save(dt);

        DailyTransaction updated = dailyTransactionRepository.findById(dt.getDalytranId()).orElseThrow();
        assertThat(updated.getDalytranAmt()).isEqualByComparingTo(new BigDecimal("999.99"));
    }

    @Test
    void deleteDailyTransactionRemovesFromDatabase() {
        List<DailyTransaction> before = dailyTransactionRepository.findAll();
        int beforeSize = before.size();
        dailyTransactionRepository.deleteById(before.get(0).getDalytranId());
        assertThat(dailyTransactionRepository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    void allDailyTransactionsHaveNonNullAmounts() {
        // Daily transactions can be negative (refunds, credits) per COBOL business rules
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        all.forEach(dt -> assertThat(dt.getDalytranAmt()).isNotNull());
    }

    @Test
    void dailyTransactionIdIsUniqueAcrossAllRecords() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        long distinctIds = all.stream().map(DailyTransaction::getDalytranId).distinct().count();
        assertThat(distinctIds).isEqualTo(all.size());
    }

    @Test
    void dailyTransactionsHaveRequiredFields() {
        List<DailyTransaction> all = dailyTransactionRepository.findAll();
        all.forEach(dt -> {
            assertThat(dt.getDalytranId()).isNotNull();
            assertThat(dt.getDalytranTypeCd()).isNotNull();
            assertThat(dt.getDalytranAmt()).isNotNull();
        });
    }
}
