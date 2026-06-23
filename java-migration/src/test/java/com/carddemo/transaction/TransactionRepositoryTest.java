package com.carddemo.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Verifies the Transaction entity mapping, Flyway seed data, and repository
 * finders against an H2 (PostgreSQL mode) database loaded by the real Flyway
 * migrations (V7 schema + V8 seed).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Test
    void seedDataLoadsExpectedRecordCount() {
        assertThat(repository.count()).isEqualTo(300);
    }

    @Test
    void firstSeedRecordFieldsMatchParsedValues() {
        Transaction tran = repository.findById("0000000000683580").orElseThrow();
        assertThat(tran.getTranTypeCd()).isEqualTo("01");
        assertThat(tran.getTranCatCd()).isEqualTo(1);
        assertThat(tran.getTranSource()).isEqualTo("POS TERM");
        assertThat(tran.getTranDesc()).isEqualTo("Purchase at Abshire-Lowe");
        assertThat(tran.getTranAmt()).isEqualByComparingTo("504.77");
        assertThat(tran.getTranMerchantId()).isEqualTo(800000000L);
        assertThat(tran.getTranMerchantName()).isEqualTo("Abshire-Lowe");
        assertThat(tran.getTranMerchantCity()).isEqualTo("North Enoshaven");
        assertThat(tran.getTranMerchantZip()).isEqualTo("72112");
        assertThat(tran.getTranCardNum()).isEqualTo("4859452612877065");
        assertThat(tran.getTranOrigTs())
                .isEqualTo(LocalDateTime.of(2022, 6, 10, 19, 27, 53));
        assertThat(tran.getTranProcTs()).isNull();
    }

    @Test
    void negativeAmountOverpunchDecodesCorrectly() {
        Transaction tran = repository.findById("0000000001774260").orElseThrow();
        assertThat(tran.getTranTypeCd()).isEqualTo("03");
        assertThat(tran.getTranAmt()).isEqualByComparingTo("-919.00");
    }

    @Test
    void findByIdReturnsEmptyForUnknownKey() {
        assertThat(repository.findById("9999999999999999")).isEmpty();
    }

    @Test
    void findByTranCardNumReturnsAllTransactionsForCard() {
        List<Transaction> byCard = repository.findByTranCardNum("4859452612877065");
        assertThat(byCard).hasSize(6);
        assertThat(byCard).allMatch(t -> t.getTranCardNum().equals("4859452612877065"));
        assertThat(repository.findByTranCardNum("0000000000000000")).isEmpty();
    }

    @Test
    void findByTranTypeCdPartitionsSeedData() {
        assertThat(repository.findByTranTypeCd("01")).hasSize(250);
        assertThat(repository.findByTranTypeCd("03")).hasSize(50);
        assertThat(repository.findByTranTypeCd("99")).isEmpty();
    }

    @Test
    void createPersistsNewTransaction() {
        repository.save(newTransaction("T0000000000000001", "9999999999999999"));

        Optional<Transaction> reloaded = repository.findById("T0000000000000001");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getTranAmt()).isEqualByComparingTo("123.45");
        assertThat(reloaded.get().getTranProcTs())
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
    }

    @Test
    void updateModifiesExistingTransaction() {
        Transaction tran = repository.findById("0000000000683580").orElseThrow();
        tran.setTranAmt(new BigDecimal("777.77"));
        repository.saveAndFlush(tran);

        assertThat(repository.findById("0000000000683580").orElseThrow().getTranAmt())
                .isEqualByComparingTo("777.77");
    }

    @Test
    void deleteRemovesTransaction() {
        repository.deleteById("0000000006292564");
        assertThat(repository.findById("0000000006292564")).isEmpty();
        assertThat(repository.count()).isEqualTo(299);
    }

    @Test
    void amountsPreserveTwoDecimalScale() {
        Transaction tran = repository.findById("0000000006292564").orElseThrow();
        assertThat(tran.getTranAmt()).isEqualByComparingTo("67.88");
        assertThat(tran.getTranAmt().scale()).isEqualTo(2);
    }

    private static Transaction newTransaction(String id, String cardNum) {
        Transaction tran = new Transaction();
        tran.setTranId(id);
        tran.setTranTypeCd("01");
        tran.setTranCatCd(1);
        tran.setTranSource("POS TERM");
        tran.setTranDesc("Synthetic test transaction");
        tran.setTranAmt(new BigDecimal("123.45"));
        tran.setTranMerchantId(800000000L);
        tran.setTranMerchantName("Test Merchant");
        tran.setTranMerchantCity("Testville");
        tran.setTranMerchantZip("00000");
        tran.setTranCardNum(cardNum);
        tran.setTranOrigTs(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
        tran.setTranProcTs(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        return tran;
    }
}
