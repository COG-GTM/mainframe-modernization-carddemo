package com.carddemo.entity;

import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for Transaction entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies CRUD operations and custom finders mirroring COBOL TRANSACT VSAM access.
 */
@DataJpaTest
class TransactionTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void transactionTableIsEmpty() {
        // No seed data for transactions (transact.txt not in ASCII seed data)
        List<Transaction> all = transactionRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void createTransactionPersistsAndReadsBack() {
        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000001");
        txn.setTranTypeCd("01");
        txn.setTranCatCd(1001);
        txn.setTranSource("ONLINE");
        txn.setTranDesc("Test purchase");
        txn.setTranAmt(new BigDecimal("125.50"));
        txn.setTranMerchantId(12345L);
        txn.setTranMerchantName("TEST MERCHANT");
        txn.setTranMerchantCity("NEW YORK");
        txn.setTranMerchantZip("10001");
        txn.setTranCardNum("4111111111111111");
        txn.setTranOrigTs("2024-01-15 10:30:00");
        txn.setTranProcTs("2024-01-15 10:30:01");

        transactionRepository.save(txn);

        Optional<Transaction> found = transactionRepository.findById("TXN0000000000001");
        assertThat(found).isPresent();
        assertThat(found.get().getTranAmt()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(found.get().getTranMerchantName()).isEqualTo("TEST MERCHANT");
    }

    @Test
    void findByCardNumReturnsMatchingTransactions() {
        Transaction txn1 = new Transaction();
        txn1.setTranId("TXN0000000000002");
        txn1.setTranTypeCd("01");
        txn1.setTranCatCd(1001);
        txn1.setTranSource("ONLINE");
        txn1.setTranDesc("Purchase 1");
        txn1.setTranAmt(new BigDecimal("50.00"));
        txn1.setTranMerchantId(1L);
        txn1.setTranMerchantName("STORE A");
        txn1.setTranMerchantCity("NYC");
        txn1.setTranMerchantZip("10001");
        txn1.setTranCardNum("4111111111111111");
        txn1.setTranOrigTs("2024-01-15 10:30:00");
        txn1.setTranProcTs("2024-01-15 10:30:01");

        Transaction txn2 = new Transaction();
        txn2.setTranId("TXN0000000000003");
        txn2.setTranTypeCd("02");
        txn2.setTranCatCd(1002);
        txn2.setTranSource("POS");
        txn2.setTranDesc("Purchase 2");
        txn2.setTranAmt(new BigDecimal("75.00"));
        txn2.setTranMerchantId(2L);
        txn2.setTranMerchantName("STORE B");
        txn2.setTranMerchantCity("LA");
        txn2.setTranMerchantZip("90001");
        txn2.setTranCardNum("4111111111111111");
        txn2.setTranOrigTs("2024-01-16 11:00:00");
        txn2.setTranProcTs("2024-01-16 11:00:01");

        transactionRepository.save(txn1);
        transactionRepository.save(txn2);

        List<Transaction> results = transactionRepository.findByTranCardNum("4111111111111111");
        assertThat(results).hasSize(2);
    }

    @Test
    void findByTypeCdReturnsMatchingTransactions() {
        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000004");
        txn.setTranTypeCd("SA");
        txn.setTranCatCd(2001);
        txn.setTranSource("ATM");
        txn.setTranDesc("ATM Withdrawal");
        txn.setTranAmt(new BigDecimal("200.00"));
        txn.setTranMerchantId(3L);
        txn.setTranMerchantName("ATM LOCATION");
        txn.setTranMerchantCity("CHICAGO");
        txn.setTranMerchantZip("60601");
        txn.setTranCardNum("4222222222222222");
        txn.setTranOrigTs("2024-02-01 09:00:00");
        txn.setTranProcTs("2024-02-01 09:00:01");

        transactionRepository.save(txn);

        List<Transaction> results = transactionRepository.findByTranTypeCd("SA");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTranDesc()).isEqualTo("ATM Withdrawal");
    }

    @Test
    void findByMerchantIdReturnsMatchingTransactions() {
        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000005");
        txn.setTranTypeCd("01");
        txn.setTranCatCd(1001);
        txn.setTranSource("ONLINE");
        txn.setTranDesc("Online purchase");
        txn.setTranAmt(new BigDecimal("99.99"));
        txn.setTranMerchantId(55555L);
        txn.setTranMerchantName("AMAZON");
        txn.setTranMerchantCity("SEATTLE");
        txn.setTranMerchantZip("98101");
        txn.setTranCardNum("4333333333333333");
        txn.setTranOrigTs("2024-03-01 14:00:00");
        txn.setTranProcTs("2024-03-01 14:00:01");

        transactionRepository.save(txn);

        List<Transaction> results = transactionRepository.findByTranMerchantId(55555L);
        assertThat(results).hasSize(1);
    }

    @Test
    void updateTransactionAmount() {
        // Mirrors COBOL REWRITE for transaction correction
        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000006");
        txn.setTranTypeCd("01");
        txn.setTranCatCd(1001);
        txn.setTranSource("POS");
        txn.setTranDesc("Purchase");
        txn.setTranAmt(new BigDecimal("100.00"));
        txn.setTranMerchantId(1L);
        txn.setTranMerchantName("STORE");
        txn.setTranMerchantCity("NYC");
        txn.setTranMerchantZip("10001");
        txn.setTranCardNum("4444444444444444");
        txn.setTranOrigTs("2024-01-01 00:00:00");
        txn.setTranProcTs("2024-01-01 00:00:01");

        transactionRepository.save(txn);

        txn.setTranAmt(new BigDecimal("150.00"));
        transactionRepository.save(txn);

        Transaction updated = transactionRepository.findById("TXN0000000000006").orElseThrow();
        assertThat(updated.getTranAmt()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void deleteTransactionRemovesFromDatabase() {
        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000007");
        txn.setTranTypeCd("01");
        txn.setTranCatCd(1001);
        txn.setTranSource("POS");
        txn.setTranDesc("To delete");
        txn.setTranAmt(new BigDecimal("10.00"));
        txn.setTranMerchantId(1L);
        txn.setTranMerchantName("STORE");
        txn.setTranMerchantCity("NYC");
        txn.setTranMerchantZip("10001");
        txn.setTranCardNum("4555555555555555");
        txn.setTranOrigTs("2024-01-01 00:00:00");
        txn.setTranProcTs("2024-01-01 00:00:01");

        transactionRepository.save(txn);
        assertThat(transactionRepository.findById("TXN0000000000007")).isPresent();

        transactionRepository.deleteById("TXN0000000000007");
        assertThat(transactionRepository.findById("TXN0000000000007")).isEmpty();
    }

    @Test
    void transactionAmountPrecisionIsCorrect() {
        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000008");
        txn.setTranTypeCd("01");
        txn.setTranCatCd(1001);
        txn.setTranSource("POS");
        txn.setTranDesc("Precision test");
        txn.setTranAmt(new BigDecimal("12345.67"));
        txn.setTranMerchantId(1L);
        txn.setTranMerchantName("STORE");
        txn.setTranMerchantCity("NYC");
        txn.setTranMerchantZip("10001");
        txn.setTranCardNum("4666666666666666");
        txn.setTranOrigTs("2024-01-01 00:00:00");
        txn.setTranProcTs("2024-01-01 00:00:01");

        transactionRepository.save(txn);

        Transaction found = transactionRepository.findById("TXN0000000000008").orElseThrow();
        assertThat(found.getTranAmt()).isEqualByComparingTo(new BigDecimal("12345.67"));
    }
}
