package com.carddemo.online.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.dto.TransactionAddRequest;
import com.carddemo.online.transaction.dto.TransactionAddResponse;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** COBOL program: COTRN02C — validation, confirmation and the write to TRANSACT. */
@DataJpaTest
class TransactionAddServiceTest {

    private static final String CARD = "4111111111111111";

    @Autowired
    private TransactionRepository transactions;

    @Autowired
    private TransactionBrowseRepository browse;

    @Autowired
    private CardXrefRepository cardXrefs;

    private TransactionAddService service;

    @BeforeEach
    void setUp() {
        service = new TransactionAddService(transactions, browse, cardXrefs);
        cardXrefs.save(CardXref.builder().cardNumber(CARD).customerId(1L).accountId(7L).build());
        transactions.save(Transaction.builder()
                .transactionId("0000000000000009")
                .typeCode("01")
                .categoryCode(1)
                .source("POS TERM")
                .description("EXISTING")
                .amount(new BigDecimal("10.00"))
                .cardNumber(CARD)
                .merchantId(1L)
                .merchantName("M")
                .merchantCity("C")
                .merchantZip("Z")
                .originTimestamp("2022-06-10 19:27:53.000000")
                .processTimestamp("2022-06-10 19:27:53.000000")
                .build());
    }

    private TransactionAddRequest.TransactionAddRequestBuilder valid() {
        return TransactionAddRequest.builder()
                .accountId("7")
                .typeCode("01")
                .categoryCode("0001")
                .source("POS TERM")
                .description("PURCHASE")
                .amount("-00000100.50")
                .originDate("2023-01-31")
                .processDate("2023-02-01")
                .merchantId("123456789")
                .merchantName("MERCHANT")
                .merchantCity("CITY")
                .merchantZip("12345");
    }

    @Test
    void withoutConfirmationNothingIsWritten() {
        TransactionAddResponse response = service.add(valid().build());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(TransactionAddService.MSG_CONFIRM);
        assertThat(response.getAccountId()).isEqualTo("00000000007");
        assertThat(response.getCardNumber()).isEqualTo(CARD);
        assertThat(transactions.count()).isEqualTo(1);
    }

    @Test
    void confirmationWritesTheTransactionWithTheNextIdAndTheXrefCard() {
        TransactionAddResponse response = service.add(valid().confirm("Y").build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTransactionId()).isEqualTo("0000000000000010");
        assertThat(response.getMessage())
                .isEqualTo("Transaction added successfully.  Your Tran ID is 0000000000000010.");

        Optional<Transaction> written = transactions.findById("0000000000000010");
        assertThat(written).isPresent();
        Transaction transaction = written.get();
        assertThat(transaction.getAmount()).isEqualByComparingTo("-100.50");
        assertThat(transaction.getCardNumber()).isEqualTo(CARD);
        assertThat(transaction.getTypeCode()).isEqualTo("01");
        assertThat(transaction.getCategoryCode()).isEqualTo(1);
        assertThat(transaction.getMerchantId()).isEqualTo(123456789L);
        assertThat(transaction.getOriginTimestamp()).isEqualTo("2023-01-31");
        assertThat(transaction.getProcessTimestamp()).isEqualTo("2023-02-01");
    }

    @Test
    void aCardNumberResolvesTheAccountThroughTheCrossReference() {
        TransactionAddResponse response =
                service.add(valid().accountId(null).cardNumber(CARD).confirm("Y").build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getAccountId()).isEqualTo("00000000007");
    }

    @Test
    void invalidConfirmationValueIsRejected() {
        TransactionAddResponse response = service.add(valid().confirm("X").build());

        assertThat(response.getMessage()).isEqualTo(TransactionAddService.MSG_CONFIRM_INVALID);
        assertThat(transactions.count()).isEqualTo(1);
    }

    @Test
    void keyFieldValidationsMatchTheCobol() {
        assertThat(message(TransactionAddRequest.builder().build()))
                .isEqualTo(TransactionAddService.MSG_KEY_REQUIRED);
        assertThat(message(valid().accountId("ABC").build()))
                .isEqualTo(TransactionAddService.MSG_ACCT_NOT_NUMERIC);
        assertThat(message(valid().accountId("99").build()))
                .isEqualTo(TransactionAddService.MSG_ACCT_NOT_FOUND);
        assertThat(message(valid().accountId(null).cardNumber("ABC").build()))
                .isEqualTo(TransactionAddService.MSG_CARD_NOT_NUMERIC);
        assertThat(message(valid().accountId(null).cardNumber("4111111111111112").build()))
                .isEqualTo(TransactionAddService.MSG_CARD_NOT_FOUND);
    }

    @Test
    void dataFieldValidationsRunInTheCobolOrder() {
        assertThat(message(valid().typeCode(" ").build()))
                .isEqualTo(TransactionAddService.MSG_TYPE_EMPTY);
        assertThat(message(valid().categoryCode("").build()))
                .isEqualTo(TransactionAddService.MSG_CATEGORY_EMPTY);
        assertThat(message(valid().source("").build()))
                .isEqualTo(TransactionAddService.MSG_SOURCE_EMPTY);
        assertThat(message(valid().merchantZip("").build()))
                .isEqualTo(TransactionAddService.MSG_MERCHANT_ZIP_EMPTY);
        assertThat(message(valid().typeCode("AB").build()))
                .isEqualTo(TransactionAddService.MSG_TYPE_NOT_NUMERIC);
        assertThat(message(valid().categoryCode("ABCD").build()))
                .isEqualTo(TransactionAddService.MSG_CATEGORY_NOT_NUMERIC);
        assertThat(message(valid().amount("100.50").build()))
                .isEqualTo(TransactionAddService.MSG_AMOUNT_FORMAT);
        assertThat(message(valid().originDate("31-01-2023").build()))
                .isEqualTo(TransactionAddService.MSG_ORIG_DATE_FORMAT);
        assertThat(message(valid().processDate("2023/02/01").build()))
                .isEqualTo(TransactionAddService.MSG_PROC_DATE_FORMAT);
        assertThat(message(valid().originDate("2023-02-30").build()))
                .isEqualTo(TransactionAddService.MSG_ORIG_DATE_INVALID);
        assertThat(message(valid().processDate("2023-13-01").build()))
                .isEqualTo(TransactionAddService.MSG_PROC_DATE_INVALID);
        assertThat(message(valid().merchantId("12345678A").build()))
                .isEqualTo(TransactionAddService.MSG_MERCHANT_ID_NOT_NUMERIC);
    }

    @Test
    void amountsAreParsedLikeNumvalC() {
        assertThat(TransactionAddService.numvalC("-00000100.50")).isEqualByComparingTo("-100.50");
        assertThat(TransactionAddService.numvalC("+00000000.99")).isEqualByComparingTo("0.99");
    }

    private String message(TransactionAddRequest request) {
        return service.add(request).getMessage();
    }
}
