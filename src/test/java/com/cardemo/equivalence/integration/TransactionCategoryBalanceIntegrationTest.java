package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionCategoryBalanceId;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.service.TransactionCategoryBalanceService;
import com.cardemo.batch.service.TransactionCategoryBalanceService.UpdateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for transaction category balance with real Spring context.
 * Business rule 10: Composite key (ACCT-ID, TYPE-CD, CAT-CD).
 * Create new if not found, update if found.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Transaction Category Balance Integration Tests")
class TransactionCategoryBalanceIntegrationTest {

    @Autowired
    private TransactionCategoryBalanceService catBalService;
    @Autowired
    private TransactionCategoryBalanceRepository catBalRepository;

    @BeforeEach
    void setUp() {
        catBalRepository.deleteAll();
    }

    private DailyTransaction createTxn(String typeCd, int catCd, BigDecimal amount) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("IT_CATBAL");
        dt.setTypeCd(typeCd);
        dt.setCatCd(catCd);
        dt.setAmount(amount);
        return dt;
    }

    @Test
    @DisplayName("IT: Creates new category balance when not found")
    void createNew_whenNotFound() {
        UpdateResult result = catBalService.updateCategoryBalance(50001L,
                createTxn("SA", 1, new BigDecimal("100.00")));

        assertTrue(result.created());
        catBalRepository.save(result.categoryBalance());

        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(50001L, "SA", 1);
        Optional<TransactionCategoryBalance> saved = catBalRepository.findById(id);
        assertTrue(saved.isPresent());
        assertEquals(new BigDecimal("100.00"), saved.get().getBalance());
    }

    @Test
    @DisplayName("IT: Updates existing category balance when found")
    void updateExisting_whenFound() {
        // Create initial record
        UpdateResult createResult = catBalService.updateCategoryBalance(50002L,
                createTxn("SA", 1, new BigDecimal("500.00")));
        catBalRepository.save(createResult.categoryBalance());

        // Update it
        UpdateResult result = catBalService.updateCategoryBalance(50002L,
                createTxn("SA", 1, new BigDecimal("200.00")));
        catBalRepository.save(result.categoryBalance());

        assertFalse(result.created());

        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(50002L, "SA", 1);
        TransactionCategoryBalance updated = catBalRepository.findById(id).orElseThrow();
        assertEquals(new BigDecimal("700.00"), updated.getBalance());
    }

    @Test
    @DisplayName("IT: Different composite keys are independent records")
    void differentCompositeKeys_independent() {
        catBalRepository.save(catBalService.updateCategoryBalance(50003L,
                createTxn("SA", 1, new BigDecimal("100.00"))).categoryBalance());
        catBalRepository.save(catBalService.updateCategoryBalance(50003L,
                createTxn("SA", 2, new BigDecimal("200.00"))).categoryBalance());
        catBalRepository.save(catBalService.updateCategoryBalance(50003L,
                createTxn("PR", 1, new BigDecimal("300.00"))).categoryBalance());

        assertEquals(new BigDecimal("100.00"),
                catBalRepository.findById(new TransactionCategoryBalanceId(50003L, "SA", 1))
                        .orElseThrow().getBalance());
        assertEquals(new BigDecimal("200.00"),
                catBalRepository.findById(new TransactionCategoryBalanceId(50003L, "SA", 2))
                        .orElseThrow().getBalance());
        assertEquals(new BigDecimal("300.00"),
                catBalRepository.findById(new TransactionCategoryBalanceId(50003L, "PR", 1))
                        .orElseThrow().getBalance());
    }

    @Test
    @DisplayName("IT: Multiple updates accumulate in database")
    void multipleUpdates_accumulate() {
        catBalRepository.save(catBalService.updateCategoryBalance(50004L,
                createTxn("SA", 1, new BigDecimal("100.00"))).categoryBalance());
        catBalRepository.save(catBalService.updateCategoryBalance(50004L,
                createTxn("SA", 1, new BigDecimal("200.00"))).categoryBalance());
        catBalRepository.save(catBalService.updateCategoryBalance(50004L,
                createTxn("SA", 1, new BigDecimal("-50.00"))).categoryBalance());

        TransactionCategoryBalance result = catBalRepository.findById(
                new TransactionCategoryBalanceId(50004L, "SA", 1)).orElseThrow();
        assertEquals(new BigDecimal("250.00"), result.getBalance());
    }

    @Test
    @DisplayName("IT: Negative amount creates record with negative balance")
    void negativeAmount_createsNegativeBalance() {
        catBalRepository.save(catBalService.updateCategoryBalance(50005L,
                createTxn("SA", 1, new BigDecimal("-100.00"))).categoryBalance());

        TransactionCategoryBalance result = catBalRepository.findById(
                new TransactionCategoryBalanceId(50005L, "SA", 1)).orElseThrow();
        assertEquals(new BigDecimal("-100.00"), result.getBalance());
    }

    @Test
    @DisplayName("IT: Different account IDs are separate records")
    void differentAcctIds_separate() {
        catBalRepository.save(catBalService.updateCategoryBalance(60001L,
                createTxn("SA", 1, new BigDecimal("100.00"))).categoryBalance());
        catBalRepository.save(catBalService.updateCategoryBalance(60002L,
                createTxn("SA", 1, new BigDecimal("200.00"))).categoryBalance());

        assertEquals(new BigDecimal("100.00"),
                catBalRepository.findById(new TransactionCategoryBalanceId(60001L, "SA", 1))
                        .orElseThrow().getBalance());
        assertEquals(new BigDecimal("200.00"),
                catBalRepository.findById(new TransactionCategoryBalanceId(60002L, "SA", 1))
                        .orElseThrow().getBalance());
    }
}
