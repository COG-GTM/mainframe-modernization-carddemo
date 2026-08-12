package com.carddemo.online.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.dto.TransactionViewResponse;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** COBOL program: COTRN01C — viewing one TRANSACT record. */
@DataJpaTest
class TransactionViewServiceTest {

    @Autowired
    private TransactionRepository repository;

    private TransactionViewService service;

    @BeforeEach
    void setUp() {
        service = new TransactionViewService(repository);
        repository.save(Transaction.builder()
                .transactionId("0000000000000042")
                .typeCode("02")
                .categoryCode(2)
                .source("POS TERM")
                .description("BILL PAYMENT - ONLINE")
                .amount(new BigDecimal("-1234.56"))
                .cardNumber("4111111111111111")
                .merchantId(999999999L)
                .merchantName("BILL PAYMENT")
                .merchantCity("N/A")
                .merchantZip("N/A")
                .originTimestamp("2022-06-10 19:27:53.000000")
                .processTimestamp("2022-06-11 19:27:53.000000")
                .build());
    }

    @Test
    void showsEveryFieldOfTheTransaction() {
        TransactionViewResponse response = service.view("0000000000000042");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(response.getTypeCode()).isEqualTo("02");
        assertThat(response.getCategoryCode()).isEqualTo(2);
        assertThat(response.getSource()).isEqualTo("POS TERM");
        assertThat(response.getAmount()).isEqualTo("-00001234.56");
        assertThat(response.getDescription()).isEqualTo("BILL PAYMENT - ONLINE");
        assertThat(response.getOriginTimestamp()).isEqualTo("2022-06-10 19:27:53.000000");
        assertThat(response.getProcessTimestamp()).isEqualTo("2022-06-11 19:27:53.000000");
        assertThat(response.getMerchantId()).isEqualTo(999999999L);
        assertThat(response.getMerchantZip()).isEqualTo("N/A");
    }

    @Test
    void rejectsAnEmptyTransactionId() {
        TransactionViewResponse response = service.view("   ");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).isEqualTo(TransactionViewService.MSG_TRAN_ID_EMPTY);
    }

    @Test
    void reportsAnUnknownTransactionId() {
        TransactionViewResponse response = service.view("0000000000009999");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).isEqualTo(TransactionViewService.MSG_TRAN_ID_NOT_FOUND);
    }
}
