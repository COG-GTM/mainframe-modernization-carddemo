package com.carddemo.service.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.web.transaction.dto.TransactionAddRequest;
import com.carddemo.web.transaction.dto.TransactionDetailDto;

/**
 * Service-layer tests focused on the {@code COTRN02C} validation ordering and the
 * {@code COTRN01C} view rules that are hard to see through the REST layer alone. Uses the
 * seeded card cross-reference so the key-field resolution runs against real data.
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
class TransactionServiceTest {

    @Autowired
    private TransactionService service;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;

    private String card;
    private String acct;

    @BeforeEach
    void setUp() {
        CardXref xref = cardXrefRepository.findAll().get(0);
        card = xref.getXrefCardNum();
        acct = xref.getXrefAcctId();
        transactionRepository.deleteAll();
    }

    private TransactionAddRequest valid() {
        return new TransactionAddRequest(acct, "", "01", "0001", "POS", "New purchase",
                "+00000100.50", "2023-03-01", "2023-03-02", "000000123", "Store", "Seattle",
                "98101", "Y");
    }

    private TransactionAddRequest with(TransactionAddRequest r, java.util.function.Function<
            TransactionAddRequest, TransactionAddRequest> f) {
        return f.apply(r);
    }

    @Test
    void viewEmptyIdRaisesCobolMessage() {
        assertThatThrownBy(() -> service.view("  "))
            .isInstanceOf(TransactionValidationException.class)
            .hasMessage("Tran ID can NOT be empty...");
    }

    @Test
    void addResolvesCardFromAccountAndGeneratesId() {
        var response = service.add(valid());
        assertThat(response.tranId()).isEqualTo("0000000000000001");
        Transaction saved = transactionRepository.findById("0000000000000001").orElseThrow();
        assertThat(saved.getTranCardNum()).isEqualTo(card);
        assertThat(saved.getTranAmt()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(saved.getTranCatCd()).isEqualTo(1);

        TransactionDetailDto detail = service.view("0000000000000001");
        assertThat(detail.merchantId()).isEqualTo("000000123");
    }

    @Test
    void emptyChecksRunInCobolFieldOrder() {
        // Type CD is the first empty check, ahead of category/amount/etc.
        assertThatThrownBy(() -> service.add(new TransactionAddRequest(acct, "", "", "", "", "",
                "", "", "", "", "", "", "", "Y")))
            .hasMessage("Type CD can NOT be empty...");
        // With type present, category is next.
        assertThatThrownBy(() -> service.add(new TransactionAddRequest(acct, "", "01", "", "", "",
                "", "", "", "", "", "", "", "Y")))
            .hasMessage("Category CD can NOT be empty...");
    }

    @Test
    void invalidCalendarDateIsRejectedAfterFormatCheck() {
        assertThatThrownBy(() -> service.add(with(valid(), r -> new TransactionAddRequest(r.acctId(),
                r.cardNum(), r.typeCd(), r.categoryCd(), r.source(), r.description(), r.amount(),
                "2023-02-30", r.procDate(), r.merchantId(), r.merchantName(), r.merchantCity(),
                r.merchantZip(), r.confirm()))))
            .hasMessage("Orig Date - Not a valid date...");
    }

    @Test
    void nonNumericMerchantIdIsRejected() {
        assertThatThrownBy(() -> service.add(with(valid(), r -> new TransactionAddRequest(r.acctId(),
                r.cardNum(), r.typeCd(), r.categoryCd(), r.source(), r.description(), r.amount(),
                r.origDate(), r.procDate(), "ABC", r.merchantName(), r.merchantCity(),
                r.merchantZip(), r.confirm()))))
            .hasMessage("Merchant ID must be Numeric...");
    }

    @Test
    void unknownCardNumberIsRejected() {
        assertThatThrownBy(() -> service.add(with(valid(), r -> new TransactionAddRequest("", "9999999999999999",
                r.typeCd(), r.categoryCd(), r.source(), r.description(), r.amount(), r.origDate(),
                r.procDate(), r.merchantId(), r.merchantName(), r.merchantCity(), r.merchantZip(),
                r.confirm()))))
            .hasMessage("Card Number NOT found...");
    }
}
