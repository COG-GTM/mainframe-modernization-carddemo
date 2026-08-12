package com.carddemo.batch.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCategory;
import com.carddemo.model.entity.TransactionCategoryId;
import com.carddemo.model.entity.TransactionType;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionTypeRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** COBOL program: CBTRN03C — page, account and grand total bookkeeping of the report. */
class TransactionDetailReportServiceTest {

    private static final ReportDateRange RANGE = new ReportDateRange("2022-01-01", "2022-07-06");

    private CardXrefRepository cardXrefRepository;
    private TransactionDetailReportService service;

    @BeforeEach
    void setUp() {
        cardXrefRepository = Mockito.mock(CardXrefRepository.class);
        TransactionTypeRepository transactionTypeRepository =
                Mockito.mock(TransactionTypeRepository.class);
        TransactionCategoryRepository transactionCategoryRepository =
                Mockito.mock(TransactionCategoryRepository.class);

        when(cardXrefRepository.findById(any())).thenAnswer(call -> {
            String cardNumber = call.getArgument(0, String.class);
            return Optional.of(CardXref.builder()
                    .cardNumber(cardNumber)
                    .accountId(Long.parseLong(cardNumber.substring(12)))
                    .customerId(1L)
                    .build());
        });
        when(transactionTypeRepository.findById(any())).thenAnswer(call -> Optional.of(
                TransactionType.builder()
                        .typeCode(call.getArgument(0, String.class))
                        .description("Purchase")
                        .build()));
        when(transactionCategoryRepository.findById(any())).thenAnswer(call -> Optional.of(
                TransactionCategory.builder()
                        .id(call.getArgument(0, TransactionCategoryId.class))
                        .description("Retail")
                        .build()));

        service = new TransactionDetailReportService(cardXrefRepository, transactionTypeRepository,
                transactionCategoryRepository, new TransactionReportFormatter());
    }

    @Test
    void printsHeadingsDetailAndTotalsForASingleCard() {
        List<String> lines = service.generate(List.of(
                transaction("T1", "4000000000000011", "10.00", "2022-03-01"),
                transaction("T2", "4000000000000011", "25.50", "2022-03-02")), RANGE);

        assertThat(lines).hasSize(9);
        assertThat(lines.get(0)).startsWith("DALYREPT");
        assertThat(lines.get(1)).isBlank();
        assertThat(lines.get(2)).startsWith("Transaction ID");
        assertThat(lines.get(3)).isEqualTo("-".repeat(133));
        assertThat(lines.get(4)).startsWith("T1              ");
        assertThat(lines.get(5)).startsWith("T2              ");
        // End of file: the last amount (25.50) is counted a second time by the EOF branch.
        assertThat(lines.get(6).stripTrailing()).endsWith("+         61.00");
        assertThat(lines.get(7)).isEqualTo("-".repeat(133));
        assertThat(lines.get(8).stripTrailing()).endsWith("+         61.00");
        assertThat(lines).allSatisfy(line -> assertThat(line).hasSize(133));
    }

    @Test
    void writesAnAccountSubtotalOnEveryCardChange() {
        List<String> lines = service.generate(List.of(
                transaction("T1", "4000000000000011", "10.00", "2022-03-01"),
                transaction("T2", "4000000000000022", "40.00", "2022-03-02")), RANGE);

        // Headings (4), first detail, account total + rule, second detail, page total + rule,
        // grand total.
        assertThat(lines).hasSize(11);
        assertThat(lines.get(5).stripTrailing())
                .isEqualTo("Account Total" + ".".repeat(84) + "+         10.00");
        assertThat(lines.get(6)).isEqualTo("-".repeat(133));
        assertThat(lines.get(8).stripTrailing()).endsWith("+         90.00");
        assertThat(lines.get(10).stripTrailing()).endsWith("+         90.00");
    }

    @Test
    void breaksThePageAfterSixteenDetailLinesBecauseTheHeadingsAreCounted() {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 1; i <= 17; i++) {
            transactions.add(transaction("T" + i, "4000000000000011", "1.00", "2022-03-01"));
        }

        List<String> lines = service.generate(transactions, RANGE);

        // 4 headings + 16 details, then the page break: totals + rule + 4 headings.
        assertThat(lines.get(20).stripTrailing())
                .isEqualTo("Page Total " + ".".repeat(86) + "+         16.00");
        assertThat(lines.get(21)).isEqualTo("-".repeat(133));
        assertThat(lines.get(22)).startsWith("DALYREPT");
        assertThat(lines.get(26)).startsWith("T17");
        // Second page total is the 17th transaction counted twice by the EOF branch.
        assertThat(lines.get(27).stripTrailing()).endsWith("+          2.00");
        assertThat(lines.get(29).stripTrailing()).endsWith("+         18.00");
    }

    @Test
    void endsTheReportOnTheFirstOutOfRangeTransaction() {
        List<Transaction> transactions = List.of(
                transaction("T1", "4000000000000011", "10.00", "2022-03-01"),
                transaction("T2", "4000000000000011", "20.00", "2022-09-30"),
                transaction("T3", "4000000000000011", "30.00", "2022-04-01"));

        List<String> lines = service.generate(transactions, RANGE);

        assertThat(lines).hasSize(5);
        assertThat(lines.get(4)).startsWith("T1");
    }

    @Test
    void skipsOutOfRangeTransactionsWhenTheNextSentenceExitIsDisabled() {
        List<Transaction> transactions = List.of(
                transaction("T1", "4000000000000011", "10.00", "2022-03-01"),
                transaction("T2", "4000000000000011", "20.00", "2022-09-30"),
                transaction("T3", "4000000000000011", "30.00", "2022-04-01"));

        List<String> lines = service.generate(transactions, RANGE, false);

        assertThat(lines).hasSize(9);
        assertThat(lines.get(5)).startsWith("T3");
        // 10.00 + 30.00 with the last in-range amount counted twice at end of file.
        assertThat(lines.get(6).stripTrailing()).endsWith("+         70.00");
    }

    @Test
    void abendsOnAnUnknownCardNumber() {
        Mockito.doReturn(Optional.empty()).when(cardXrefRepository).findById(any());

        assertThatThrownBy(() -> service.generate(
                List.of(transaction("T1", "4000000000000011", "10.00", "2022-03-01")), RANGE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INVALID CARD NUMBER : 4000000000000011");
    }

    @Test
    void producesNothingForAnEmptyExtract() {
        assertThat(service.generate(List.of(), RANGE)).isEmpty();
    }

    private static Transaction transaction(String id, String cardNumber, String amount, String date) {
        return Transaction.builder()
                .transactionId(id)
                .typeCode("01")
                .categoryCode(1)
                .source("POS TERM")
                .description("Purchase")
                .amount(new BigDecimal(amount))
                .cardNumber(cardNumber)
                .originTimestamp(date + "-00.00.00.000000")
                .processTimestamp(date + "-00.00.00.000000")
                .build();
    }
}
