package com.carddemo.batch.statement;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.Transaction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Verifies the WS-TRNX-TABLE build (8500-READTRNX-READ) and scan (4000-TRNXFILE-GET). */
class StatementTransactionTableTest {

    private Transaction transaction(String card, String id) {
        return Transaction.builder()
                .transactionId(id)
                .cardNumber(card)
                .amount(BigDecimal.ONE)
                .build();
    }

    @Test
    void groupsConsecutiveRecordsOfTheSameCard() {
        StatementTransactionTable table = StatementTransactionTable.load(List.of(
                transaction("1111111111111111", "0000000000000001"),
                transaction("1111111111111111", "0000000000000002"),
                transaction("2222222222222222", "0000000000000003")));

        assertThat(table.transactionsFor("1111111111111111"))
                .extracting(Transaction::getTransactionId)
                .containsExactly("0000000000000001", "0000000000000002");
        assertThat(table.transactionsFor("2222222222222222")).hasSize(1);
        assertThat(table.transactionsFor("3333333333333333")).isEmpty();
    }

    @Test
    void keepsAtMostTenTransactionsPerCardAsTheCobolTableDoes() {
        List<Transaction> file = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            file.add(transaction("1111111111111111", String.format("%016d", i)));
        }

        assertThat(StatementTransactionTable.load(file).transactionsFor("1111111111111111"))
                .hasSize(StatementTransactionTable.MAX_TRANSACTIONS_PER_CARD)
                .last()
                .extracting(Transaction::getTransactionId)
                .isEqualTo("0000000000000010");
    }

    @Test
    void keepsAtMostFiftyOneCardsAsTheCobolTableDoes() {
        List<Transaction> file = new ArrayList<>();
        for (int card = 1; card <= 55; card++) {
            file.add(transaction(String.format("%016d", card), String.format("%016d", card)));
        }

        StatementTransactionTable table = StatementTransactionTable.load(file);

        assertThat(table.transactionsFor(String.format("%016d", StatementTransactionTable.MAX_CARDS)))
                .hasSize(1);
        assertThat(table.transactionsFor(String.format("%016d", StatementTransactionTable.MAX_CARDS + 1)))
                .isEmpty();
    }
}
