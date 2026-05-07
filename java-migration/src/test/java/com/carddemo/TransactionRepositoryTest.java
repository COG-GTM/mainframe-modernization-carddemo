package com.carddemo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.carddemo.entity.Transaction;
import com.carddemo.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TransactionRepository} using @DataJpaTest with H2.
 * Tests verify the Flyway-seeded data (300 records from dailytran.txt) and CRUD operations.
 */
@DataJpaTest
@ActiveProfiles("dev")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Seed data loads exactly 300 transaction records")
    void seedDataLoads300Records() {
        long count = transactionRepository.count();
        assertThat(count).isEqualTo(300);
    }

    @Test
    @DisplayName("First record matches expected COBOL field values")
    void firstRecordMatchesExpectedValues() {
        Optional<Transaction> opt = transactionRepository.findById("0000000000683580");
        assertThat(opt).isPresent();

        Transaction txn = opt.get();
        assertThat(txn.getTypeCode()).isEqualTo("01");
        assertThat(txn.getCategoryCode()).isEqualTo(1);
        assertThat(txn.getSource()).isEqualTo("POS TERM");
        assertThat(txn.getDescription()).isEqualTo("Purchase at Abshire-Lowe");
        assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("504.77"));
        assertThat(txn.getMerchantId()).isEqualTo(800000000L);
        assertThat(txn.getMerchantName()).isEqualTo("Abshire-Lowe");
        assertThat(txn.getMerchantCity()).isEqualTo("North Enoshaven");
        assertThat(txn.getMerchantZip()).isEqualTo("72112");
        assertThat(txn.getCardNum()).isEqualTo("4859452612877065");
        assertThat(txn.getOrigTimestamp()).isEqualTo("2022-06-10 19:27:53.000000");
    }

    @Test
    @DisplayName("Negative amount (sign overpunch) parsed correctly for return transaction")
    void negativeAmountParsedCorrectly() {
        // Record 2: TRAN-AMT = 0000009190} -> } is negative 0, so value = -919.00
        Optional<Transaction> opt = transactionRepository.findById("0000000001774260");
        assertThat(opt).isPresent();

        Transaction txn = opt.get();
        assertThat(txn.getTypeCode()).isEqualTo("03");
        assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("-919.00"));
        assertThat(txn.getSource()).isEqualTo("OPERATOR");
        assertThat(txn.getDescription()).startsWith("Return item at");
    }

    @Test
    @DisplayName("findByCardNum returns transactions for a known card number")
    void findByCardNumReturnsResults() {
        List<Transaction> results = transactionRepository.findByCardNum("4859452612877065");
        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(txn ->
                assertThat(txn.getCardNum()).isEqualTo("4859452612877065"));
    }

    @Test
    @DisplayName("findByTypeCode returns transactions for type '01' (purchase)")
    void findByTypeCodeReturnsResults() {
        List<Transaction> purchases = transactionRepository.findByTypeCode("01");
        assertThat(purchases).isNotEmpty();
        assertThat(purchases).allSatisfy(txn ->
                assertThat(txn.getTypeCode()).isEqualTo("01"));
    }

    @Test
    @DisplayName("findByMerchantId returns transactions for merchant 800000000")
    void findByMerchantIdReturnsResults() {
        List<Transaction> results = transactionRepository.findByMerchantId(800000000L);
        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(txn ->
                assertThat(txn.getMerchantId()).isEqualTo(800000000L));
    }

    @Test
    @DisplayName("CRUD: save and retrieve a new transaction")
    void saveAndRetrieveNewTransaction() {
        Transaction newTxn = Transaction.builder()
                .id("9999999999999999")
                .typeCode("01")
                .categoryCode(1)
                .source("ONLINE")
                .description("Test transaction")
                .amount(new BigDecimal("123.45"))
                .merchantId(100000001L)
                .merchantName("Test Merchant")
                .merchantCity("Test City")
                .merchantZip("12345")
                .cardNum("1111222233334444")
                .origTimestamp("2024-01-15 10:30:00.000000")
                .procTimestamp("2024-01-15 10:30:01.000000")
                .build();

        transactionRepository.save(newTxn);
        entityManager.flush();
        entityManager.clear();

        Optional<Transaction> found = transactionRepository.findById("9999999999999999");
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Test transaction");
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));

        // Total count should now be 301
        assertThat(transactionRepository.count()).isEqualTo(301);
    }

    @Test
    @DisplayName("CRUD: delete a transaction")
    void deleteTransaction() {
        // Verify it exists first
        assertThat(transactionRepository.findById("0000000000683580")).isPresent();

        transactionRepository.deleteById("0000000000683580");
        entityManager.flush();
        entityManager.clear();

        assertThat(transactionRepository.findById("0000000000683580")).isNotPresent();
        assertThat(transactionRepository.count()).isEqualTo(299);
    }

    @Test
    @DisplayName("findByCardNum returns empty list for non-existent card")
    void findByCardNumReturnsEmptyForUnknownCard() {
        List<Transaction> results = transactionRepository.findByCardNum("0000000000000000");
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("findById returns empty for non-existent transaction ID")
    void findByIdReturnsEmptyForUnknownId() {
        Optional<Transaction> result = transactionRepository.findById("DOES_NOT_EXIST__");
        assertThat(result).isNotPresent();
    }
}
