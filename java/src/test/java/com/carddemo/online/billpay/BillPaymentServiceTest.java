package com.carddemo.online.billpay;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.TransactionBrowseRepository;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** COBOL program: COBIL00C — the balance effects of a confirmed bill payment. */
@DataJpaTest
class BillPaymentServiceTest {

    private static final String CARD = "4111111111111111";
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2023-03-04T05:06:07Z"), ZoneOffset.UTC);

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private CardXrefRepository cardXrefs;

    @Autowired
    private TransactionRepository transactions;

    @Autowired
    private TransactionBrowseRepository browse;

    private BillPaymentService service;

    @BeforeEach
    void setUp() {
        service = new BillPaymentService(accounts, cardXrefs, transactions, browse, CLOCK);
        cardXrefs.save(CardXref.builder().cardNumber(CARD).customerId(1L).accountId(7L).build());
        accounts.save(Account.builder()
                .accountId(7L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("194.00"))
                .creditLimit(new BigDecimal("2020.00"))
                .currentCycleCredit(new BigDecimal("30.00"))
                .currentCycleDebit(new BigDecimal("40.00"))
                .build());
    }

    @Test
    void aConfirmedPaymentClearsTheBalanceAndWritesOneTransaction() {
        BillPaymentResponse response =
                service.pay(BillPaymentRequest.builder().accountId("7").confirm("Y").build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTransactionId()).isEqualTo("0000000000000001");
        assertThat(response.getMessage())
                .isEqualTo("Payment successful.  Your Transaction ID is 0000000000000001.");
        assertThat(response.getPaidAmount()).isEqualByComparingTo("194.00");
        assertThat(response.getCurrentBalance()).isEqualByComparingTo("0.00");
        assertThat(response.getCurrentBalanceDisplay()).isEqualTo("+0000000000.00");

        Account account = accounts.findById(7L).orElseThrow();
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("0.00");
        // COBIL00C rewrites only ACCT-CURR-BAL; the cycle totals are untouched.
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("30.00");
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("40.00");

        Transaction payment = transactions.findById("0000000000000001").orElseThrow();
        assertThat(payment.getTypeCode()).isEqualTo("02");
        assertThat(payment.getCategoryCode()).isEqualTo(2);
        assertThat(payment.getSource()).isEqualTo("POS TERM");
        assertThat(payment.getDescription()).isEqualTo("BILL PAYMENT - ONLINE");
        assertThat(payment.getAmount()).isEqualByComparingTo("194.00");
        assertThat(payment.getCardNumber()).isEqualTo(CARD);
        assertThat(payment.getMerchantId()).isEqualTo(999999999L);
        assertThat(payment.getMerchantName()).isEqualTo("BILL PAYMENT");
        assertThat(payment.getMerchantCity()).isEqualTo("N/A");
        assertThat(payment.getMerchantZip()).isEqualTo("N/A");
        assertThat(payment.getOriginTimestamp()).isEqualTo("2023-03-04 05:06:07.000000");
        assertThat(payment.getProcessTimestamp()).isEqualTo("2023-03-04 05:06:07.000000");
    }

    @Test
    void theGeneratedIdContinuesAfterTheHighestTransactionOnFile() {
        transactions.save(Transaction.builder()
                .transactionId("0000000000000123")
                .typeCode("01")
                .categoryCode(1)
                .amount(new BigDecimal("1.00"))
                .cardNumber(CARD)
                .build());

        BillPaymentResponse response =
                service.pay(BillPaymentRequest.builder().accountId("7").confirm("Y").build());

        assertThat(response.getTransactionId()).isEqualTo("0000000000000124");
    }

    @Test
    void withoutConfirmationTheBalanceIsShownAndNothingChanges() {
        BillPaymentResponse response =
                service.pay(BillPaymentRequest.builder().accountId("7").build());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(BillPaymentService.MSG_CONFIRM);
        assertThat(response.getCurrentBalanceDisplay()).isEqualTo("+0000000194.00");
        assertThat(accounts.findById(7L).orElseThrow().getCurrentBalance())
                .isEqualByComparingTo("194.00");
        assertThat(transactions.count()).isZero();
    }

    @Test
    void aDeclinedConfirmationClearsTheScreen() {
        BillPaymentResponse response =
                service.pay(BillPaymentRequest.builder().accountId("7").confirm("N").build());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isNull();
        assertThat(transactions.count()).isZero();
    }

    @Test
    void nothingIsPayableWhenTheBalanceIsNotPositive() {
        Account account = accounts.findById(7L).orElseThrow();
        account.setCurrentBalance(BigDecimal.ZERO);
        accounts.save(account);

        BillPaymentResponse response =
                service.pay(BillPaymentRequest.builder().accountId("7").confirm("Y").build());

        assertThat(response.getMessage()).isEqualTo(BillPaymentService.MSG_NOTHING_TO_PAY);
        assertThat(transactions.count()).isZero();
    }

    @Test
    void screenValidationsMatchTheCobol() {
        assertThat(service.pay(BillPaymentRequest.builder().accountId(" ").build()).getMessage())
                .isEqualTo(BillPaymentService.MSG_ACCT_EMPTY);
        assertThat(service.pay(BillPaymentRequest.builder().accountId("7").confirm("X").build())
                        .getMessage())
                .isEqualTo(BillPaymentService.MSG_CONFIRM_INVALID);
        assertThat(service.pay(BillPaymentRequest.builder().accountId("99").confirm("Y").build())
                        .getMessage())
                .isEqualTo(BillPaymentService.MSG_ACCT_NOT_FOUND);
    }
}
