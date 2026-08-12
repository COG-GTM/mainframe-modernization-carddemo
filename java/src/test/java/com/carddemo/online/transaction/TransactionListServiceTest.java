package com.carddemo.online.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.dto.TransactionListRequest;
import com.carddemo.online.transaction.dto.TransactionListResponse;
import com.carddemo.online.transaction.dto.TransactionListRow;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** COBOL program: COTRN00C — paging over the TRANSACT file. */
@DataJpaTest
class TransactionListServiceTest {

    @Autowired
    private TransactionBrowseRepository repository;

    private TransactionListService service;
    private TransactionListState state;

    @BeforeEach
    void setUp() {
        service = new TransactionListService(repository);
        state = new TransactionListState();
        for (int i = 1; i <= 25; i++) {
            repository.save(Transaction.builder()
                    .transactionId(TransactionFormats.transactionId(i))
                    .typeCode("01")
                    .categoryCode(1)
                    .source("POS TERM")
                    .description("TRANSACTION " + i)
                    .amount(new BigDecimal(i + ".05"))
                    .cardNumber("4111111111111111")
                    .merchantId(123456789L)
                    .merchantName("MERCHANT")
                    .merchantCity("CITY")
                    .merchantZip("12345")
                    .originTimestamp("2022-06-1" + (i % 10) + " 19:27:53.000000")
                    .processTimestamp("2022-06-1" + (i % 10) + " 19:27:53.000000")
                    .build());
        }
    }

    @Test
    void firstPageShowsTenTransactionsAndOffersANextPage() {
        TransactionListResponse response = service.handle(new TransactionListRequest(), state);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getPageNumber()).isEqualTo(1);
        assertThat(response.isNextPageAvailable()).isTrue();
        assertThat(response.getTransactions()).hasSize(10);

        TransactionListRow first = response.getTransactions().get(0);
        assertThat(first.getTransactionId()).isEqualTo("0000000000000001");
        assertThat(first.getDate()).isEqualTo("06/11/22");
        assertThat(first.getAmount()).isEqualTo("+00000001.05");
        assertThat(response.getTransactions().get(9).getTransactionId()).isEqualTo("0000000000000010");
        assertThat(state.getFirstTransactionId()).isEqualTo("0000000000000001");
        assertThat(state.getLastTransactionId()).isEqualTo("0000000000000010");
    }

    @Test
    void pf8PagesForwardAndPf7PagesBack() {
        service.handle(new TransactionListRequest(), state);

        TransactionListResponse second = page(TransactionListRequest.Action.PF8);
        assertThat(second.getPageNumber()).isEqualTo(2);
        assertThat(ids(second)).startsWith("0000000000000011").endsWith("0000000000000020");
        assertThat(second.isNextPageAvailable()).isTrue();

        TransactionListResponse third = page(TransactionListRequest.Action.PF8);
        assertThat(third.getPageNumber()).isEqualTo(3);
        assertThat(third.getTransactions()).hasSize(5);
        assertThat(third.isNextPageAvailable()).isFalse();
        assertThat(third.getErrorMessage()).isEqualTo(TransactionListService.MSG_BOTTOM_REACHED);

        TransactionListResponse back = page(TransactionListRequest.Action.PF7);
        assertThat(back.getPageNumber()).isEqualTo(2);
        assertThat(ids(back)).startsWith("0000000000000011").endsWith("0000000000000020");
    }

    @Test
    void pf8OnTheLastPageKeepsThePageAndWarns() {
        service.handle(new TransactionListRequest(), state);
        page(TransactionListRequest.Action.PF8);
        page(TransactionListRequest.Action.PF8);

        TransactionListResponse response = page(TransactionListRequest.Action.PF8);

        assertThat(response.getPageNumber()).isEqualTo(3);
        assertThat(response.getErrorMessage()).isEqualTo(TransactionListService.MSG_ALREADY_BOTTOM);
        assertThat(response.getTransactions()).isEmpty();
    }

    @Test
    void pf7OnTheFirstPageKeepsThePageAndWarns() {
        service.handle(new TransactionListRequest(), state);

        TransactionListResponse response = page(TransactionListRequest.Action.PF7);

        assertThat(response.getPageNumber()).isEqualTo(1);
        assertThat(response.getErrorMessage()).isEqualTo(TransactionListService.MSG_ALREADY_TOP);
    }

    @Test
    void transactionIdFilterPositionsTheBrowse() {
        TransactionListResponse response = service.handle(
                TransactionListRequest.builder().transactionIdFilter("18").build(), state);

        assertThat(response.getPageNumber()).isEqualTo(1);
        assertThat(response.getTransactions()).hasSize(8);
        assertThat(ids(response)).startsWith("0000000000000018").endsWith("0000000000000025");
        assertThat(response.getErrorMessage()).isEqualTo(TransactionListService.MSG_BOTTOM_REACHED);
    }

    @Test
    void nonNumericTransactionIdIsRejected() {
        TransactionListResponse response = service.handle(
                TransactionListRequest.builder().transactionIdFilter("ABC").build(), state);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).isEqualTo(TransactionListService.MSG_TRAN_ID_NOT_NUMERIC);
        assertThat(response.getTransactions()).isEmpty();
    }

    @Test
    void selectionWithSHandsOverToTheViewProgram() {
        TransactionListResponse response = service.handle(
                TransactionListRequest.builder()
                        .selectionFlag("S")
                        .selectedTransactionId("0000000000000004")
                        .build(),
                state);

        assertThat(response.getNextProgram()).isEqualTo("COTRN01C");
        assertThat(response.getSelectedTransactionId()).isEqualTo("0000000000000004");
    }

    @Test
    void anySelectionOtherThanSStillListsThePageWithAWarning() {
        TransactionListResponse response = service.handle(
                TransactionListRequest.builder()
                        .selectionFlag("X")
                        .selectedTransactionId("0000000000000004")
                        .build(),
                state);

        assertThat(response.getNextProgram()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo(TransactionListService.MSG_INVALID_SELECTION);
        assertThat(response.getTransactions()).hasSize(10);
    }

    private TransactionListResponse page(TransactionListRequest.Action action) {
        return service.handle(TransactionListRequest.builder().action(action).build(), state);
    }

    private static String[] ids(TransactionListResponse response) {
        List<TransactionListRow> rows = response.getTransactions();
        return rows.stream().map(TransactionListRow::getTransactionId).toArray(String[]::new);
    }
}
