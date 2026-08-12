package com.carddemo.batch.interest;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** COBOL program: CBACT04C — arithmetic of 1300-COMPUTE-INTEREST and 1300-B-WRITE-TX. */
class InterestCalculationServiceTest {

    private final InterestCalculationService service = new InterestCalculationService();

    @Test
    void appliesTheAnnualRateDividedByTwelveHundred() {
        assertThat(service.monthlyInterest(new BigDecimal("1000.00"), new BigDecimal("12.00")))
                .isEqualByComparingTo("10.00");
        assertThat(service.monthlyInterest(new BigDecimal("250.75"), new BigDecimal("18.50")))
                .isEqualByComparingTo("3.86");
        assertThat(service.monthlyInterest(new BigDecimal("1234.56"), new BigDecimal("24.99")))
                .isEqualByComparingTo("25.70");
    }

    @Test
    void truncatesInsteadOfRoundingBecauseTheComputeHasNoRoundedPhrase() {
        // 100.00 * 1.50 / 1200 = 0.125 exactly: WS-MONTHLY-INT PIC S9(09)V99 keeps 0.12.
        assertThat(service.monthlyInterest(new BigDecimal("100.00"), new BigDecimal("1.50")))
                .isEqualByComparingTo("0.12");
        assertThat(service.monthlyInterest(new BigDecimal("-100.00"), new BigDecimal("1.50")))
                .isEqualByComparingTo("-0.12");
        assertThat(service.monthlyInterest(new BigDecimal("0.00"), new BigDecimal("19.99")))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void treatsMissingAmountsAsZero() {
        assertThat(service.monthlyInterest(null, new BigDecimal("12.00"))).isEqualByComparingTo("0.00");
        assertThat(service.monthlyInterest(new BigDecimal("500.00"), null)).isEqualByComparingTo("0.00");
    }

    @Test
    void buildsTheTransactionIdFromTheParmDateAndTheSixDigitCounter() {
        assertThat(service.transactionId("2022071800", 1)).isEqualTo("2022071800000001");
        assertThat(service.transactionId("2022071800", 123456)).isEqualTo("2022071800123456");
        assertThat(service.transactionId("2022071800", 1)).hasSize(16);
    }

    @Test
    void buildsTheDescriptionWithTheElevenDigitAccountId() {
        assertThat(service.interestDescription(11L)).isEqualTo("Int. for a/c 00000000011");
    }

    @Test
    void formatsTheDb2Timestamp() {
        LocalDateTime timestamp = LocalDateTime.of(2022, 7, 18, 9, 5, 3, 456_000_000);
        assertThat(service.db2Timestamp(timestamp)).isEqualTo("2022-07-18-09.05.03.450000");
        assertThat(service.db2Timestamp(timestamp)).hasSize(26);
    }

    @Test
    void writesTheInterestTransactionFieldsOfParagraph1300B() {
        LocalDateTime timestamp = LocalDateTime.of(2022, 7, 18, 9, 5, 3, 0);

        Transaction transaction = service.buildInterestTransaction("2022071800", 7, 11L,
                "4111111111111111", new BigDecimal("10.00"), timestamp);

        assertThat(transaction.getTransactionId()).isEqualTo("2022071800000007");
        assertThat(transaction.getTypeCode()).isEqualTo("01");
        assertThat(transaction.getCategoryCode()).isEqualTo(5);
        assertThat(transaction.getSource()).isEqualTo("System");
        assertThat(transaction.getDescription()).isEqualTo("Int. for a/c 00000000011");
        assertThat(transaction.getAmount()).isEqualByComparingTo("10.00");
        assertThat(transaction.getMerchantId()).isZero();
        assertThat(transaction.getMerchantName()).isEmpty();
        assertThat(transaction.getMerchantCity()).isEmpty();
        assertThat(transaction.getMerchantZip()).isEmpty();
        assertThat(transaction.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(transaction.getOriginTimestamp()).isEqualTo("2022-07-18-09.05.03.000000");
        assertThat(transaction.getProcessTimestamp()).isEqualTo(transaction.getOriginTimestamp());
    }

    @Test
    void addsTheAccumulatedInterestAndClearsBothCycleBuckets() {
        Account account = Account.builder()
                .accountId(11L)
                .currentBalance(new BigDecimal("500.00"))
                .currentCycleCredit(new BigDecimal("120.00"))
                .currentCycleDebit(new BigDecimal("45.50"))
                .build();

        service.applyTotalInterest(account, new BigDecimal("13.86"));

        assertThat(account.getCurrentBalance()).isEqualByComparingTo("513.86");
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("0.00");
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("0.00");
    }
}
