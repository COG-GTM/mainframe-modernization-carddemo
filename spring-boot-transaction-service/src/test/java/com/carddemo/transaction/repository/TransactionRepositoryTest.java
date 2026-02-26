package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Transaction;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for TransactionRepository.
 * Tests data access patterns that replace CICS VSAM file control commands
 * (READ, WRITE, STARTBR, READNEXT, READPREV, ENDBR).
 *
 * Uses H2 in-memory database with Flyway migrations.
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        // Flyway seeds test data from V2__seed_test_data.sql
    }

    /**
     * Tests finding the last transaction by ID (descending order).
     * Replaces: STARTBR at HIGH-VALUES + READPREV pattern in COTRN02C.
     */
    @Test
    void findTopByOrderByTranIdDesc_returnsLastTransaction() {
        Optional<Transaction> lastTxn = transactionRepository.findTopByOrderByTranIdDesc();

        assertTrue(lastTxn.isPresent());
        assertEquals("0000000000000003", lastTxn.get().getTranId());
    }

    /**
     * Tests paginated transaction listing.
     * Replaces: STARTBR/READNEXT browse pattern in COTRN00C with page size 10.
     */
    @Test
    void findAllByOrderByTranIdAsc_returnsPaginated() {
        Page<Transaction> page = transactionRepository.findAllByOrderByTranIdAsc(
                PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals("0000000000000001", page.getContent().get(0).getTranId());
        assertEquals("0000000000000002", page.getContent().get(1).getTranId());
        assertTrue(page.hasNext());
    }

    /**
     * Tests reading a specific transaction by ID.
     * Replaces: EXEC CICS READ DATASET('TRANSACT') RIDFLD(TRAN-ID).
     */
    @Test
    void findById_existingTransaction() {
        Optional<Transaction> txn = transactionRepository.findById("0000000000000001");

        assertTrue(txn.isPresent());
        assertEquals("01", txn.get().getTranTypeCd());
        assertEquals("ONLINE", txn.get().getTranSource());
        assertEquals(new BigDecimal("-250.99"), txn.get().getTranAmt());
    }

    /**
     * Tests that a non-existent transaction ID returns empty.
     * Replaces: DFHRESP(NOTFND) in READ-TRANSACT-FILE.
     */
    @Test
    void findById_nonExistent() {
        Optional<Transaction> txn = transactionRepository.findById("9999999999999999");

        assertFalse(txn.isPresent());
    }

    /**
     * Tests writing a new transaction.
     * Replaces: EXEC CICS WRITE DATASET('TRANSACT') in WRITE-TRANSACT-FILE.
     */
    @Test
    void save_newTransaction() {
        Transaction newTxn = new Transaction();
        newTxn.setTranId("0000000000000004");
        newTxn.setTranTypeCd("01");
        newTxn.setTranCatCd(5001);
        newTxn.setTranSource("ONLINE");
        newTxn.setTranDesc("New test transaction");
        newTxn.setTranAmt(new BigDecimal("-75.25"));
        newTxn.setTranMerchantId(123456789);
        newTxn.setTranMerchantName("Test Store");
        newTxn.setTranMerchantCity("Boston");
        newTxn.setTranMerchantZip("02101");
        newTxn.setTranCardNum("4111111111111111");
        newTxn.setTranOrigTs("2024-02-01");
        newTxn.setTranProcTs("2024-02-01");

        transactionRepository.save(newTxn);

        Optional<Transaction> saved = transactionRepository.findById("0000000000000004");
        assertTrue(saved.isPresent());
        assertEquals("New test transaction", saved.get().getTranDesc());

        // Verify it's now the last transaction
        Optional<Transaction> last = transactionRepository.findTopByOrderByTranIdDesc();
        assertTrue(last.isPresent());
        assertEquals("0000000000000004", last.get().getTranId());
    }
}
