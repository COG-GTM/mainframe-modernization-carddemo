package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository integration tests using H2 in-memory database.
 *
 * Tests the JPA repository methods that replace VSAM file operations:
 *   - save()                              -> WRITE TRANSACT
 *   - findFirstByOrderByTransactionIdDesc -> STARTBR HIGH-VALUES + READPREV
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Save and retrieve a transaction")
    void saveAndFindById() {
        Transaction transaction = createTransaction("01", 5411, new BigDecimal("-125.50"));
        Transaction saved = transactionRepository.save(transaction);

        assertNotNull(saved.getTransactionId());
        Optional<Transaction> found = transactionRepository.findById(saved.getTransactionId());
        assertTrue(found.isPresent());
        assertEquals("01", found.get().getTypeCode());
        assertEquals(5411, found.get().getCategoryCode());
    }

    @Test
    @DisplayName("findFirstByOrderByTransactionIdDesc returns latest transaction")
    void findLatestTransaction() {
        // Replaces: STARTBR HIGH-VALUES, READPREV, ENDBR
        transactionRepository.save(createTransaction("01", 5411, new BigDecimal("10.00")));
        transactionRepository.save(createTransaction("02", 5412, new BigDecimal("20.00")));
        Transaction latest = transactionRepository.save(
                createTransaction("03", 5413, new BigDecimal("30.00")));

        Optional<Transaction> result = transactionRepository.findFirstByOrderByTransactionIdDesc();

        assertTrue(result.isPresent());
        assertEquals(latest.getTransactionId(), result.get().getTransactionId());
        assertEquals("03", result.get().getTypeCode());
    }

    @Test
    @DisplayName("findFirstByOrderByTransactionIdDesc returns empty when no transactions")
    void findLatestTransaction_empty() {
        // Replaces: DFHRESP(ENDFILE) in READPREV
        Optional<Transaction> result = transactionRepository.findFirstByOrderByTransactionIdDesc();
        assertTrue(result.isEmpty());
    }

    private Transaction createTransaction(String typeCode, int categoryCode, BigDecimal amount) {
        Transaction t = new Transaction();
        t.setTypeCode(typeCode);
        t.setCategoryCode(categoryCode);
        t.setSource("ONLINE");
        t.setDescription("Test transaction");
        t.setAmount(amount);
        t.setMerchantId(123456789L);
        t.setMerchantName("Test Store");
        t.setMerchantCity("Seattle");
        t.setMerchantZip("98101");
        t.setCardNumber("4000123456789010");
        t.setOriginatedTs(LocalDateTime.of(2026, 2, 24, 0, 0));
        t.setProcessedTs(LocalDateTime.of(2026, 2, 24, 0, 0));
        return t;
    }
}
