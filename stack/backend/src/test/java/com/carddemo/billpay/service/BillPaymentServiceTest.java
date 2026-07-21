package com.carddemo.billpay.service;

import com.carddemo.billpay.domain.AccountEntity;
import com.carddemo.billpay.domain.CardXrefEntity;
import com.carddemo.billpay.domain.TransactionEntity;
import com.carddemo.billpay.repository.AccountRepository;
import com.carddemo.billpay.repository.CardXrefRepository;
import com.carddemo.billpay.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavioral parity tests for {@link BillPaymentService} vs COBIL00C.
 * Covers FR-2..FR-9 and acceptance criteria AC-1..AC-4.
 */
@SpringBootTest
@Transactional
class BillPaymentServiceTest {

    @Autowired BillPaymentService service;
    @Autowired AccountRepository accountRepository;
    @Autowired CardXrefRepository cardXrefRepository;
    @Autowired TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private void seedAccount(long id, String balance) {
        AccountEntity a = new AccountEntity();
        a.setAcctId(id);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal(balance));
        accountRepository.save(a);
    }

    private void seedXref(String card, long acct) {
        CardXrefEntity x = new CardXrefEntity();
        x.setCardNum(card);
        x.setCustId(1L);
        x.setAcctId(acct);
        cardXrefRepository.save(x);
    }

    @Test
    void pay_emptyAcct_error() { // FR-4
        BillPaymentResult r = service.process("", "Y");
        assertThat(r.getMessageType()).isEqualTo(BillPaymentResult.MessageType.ERROR);
        assertThat(r.getMessage()).isEqualTo("Acct ID can NOT be empty...");
        assertThat(r.getFieldInError()).isEqualTo(BillPaymentResult.Field.ACCT_ID);
    }

    @Test
    void pay_unknownAcct_error() { // FR-6
        BillPaymentResult r = service.process("99999999999", "Y");
        assertThat(r.getMessageType()).isEqualTo(BillPaymentResult.MessageType.ERROR);
        assertThat(r.getMessage()).isEqualTo("Account ID NOT found...");
    }

    @Test
    void pay_invalidConfirm_error() { // FR-8
        seedAccount(11L, "123.45");
        BillPaymentResult r = service.process("11", "X");
        assertThat(r.getMessageType()).isEqualTo(BillPaymentResult.MessageType.ERROR);
        assertThat(r.getMessage()).isEqualTo("Invalid value. Valid values are (Y/N)...");
        assertThat(r.getFieldInError()).isEqualTo(BillPaymentResult.Field.CONFIRM);
    }

    @Test
    void inquiry_returnsBalanceAndConfirmPrompt() { // FR-2 / FR-7
        seedAccount(11L, "123.45");
        seedXref("4111111111111111", 11L);
        BillPaymentResult r = service.inquiry("11");
        assertThat(r.getMessageType()).isEqualTo(BillPaymentResult.MessageType.INFO);
        assertThat(r.getMessage()).isEqualTo("Confirm to make a bill payment...");
        assertThat(r.getBalance()).isEqualByComparingTo("123.45");
        assertThat(transactionRepository.count()).isZero(); // no side effect
    }

    @Test
    void pay_nonPositiveBalance_error() { // FR-5
        seedAccount(12L, "0.00");
        BillPaymentResult r = service.process("12", "Y");
        assertThat(r.getMessageType()).isEqualTo(BillPaymentResult.MessageType.ERROR);
        assertThat(r.getMessage()).isEqualTo("You have nothing to pay...");
        assertThat(transactionRepository.count()).isZero();
    }

    @Test
    void pay_declined_noChange() { // FR-9
        seedAccount(11L, "123.45");
        seedXref("4111111111111111", 11L);
        BillPaymentResult r = service.process("11", "N");
        assertThat(r.isCleared()).isTrue();
        assertThat(transactionRepository.count()).isZero();
        assertThat(accountRepository.findById(11L).orElseThrow().getCurrBal()).isEqualByComparingTo("123.45");
    }

    @Test
    void pay_confirmed_insertsTxnAndZeroesBalance() { // FR-3 / AC-1 / AC-3
        seedAccount(11L, "123.45");
        seedXref("4111111111111111", 11L);

        BillPaymentResult r = service.process("11", "Y");

        assertThat(r.getMessageType()).isEqualTo(BillPaymentResult.MessageType.SUCCESS);
        assertThat(r.getMessage()).isEqualTo("Payment successful.  Your Transaction ID is 0000000000000001.");
        assertThat(r.getBalance()).isEqualByComparingTo("0.00");

        assertThat(accountRepository.findById(11L).orElseThrow().getCurrBal()).isEqualByComparingTo("0.00");

        assertThat(transactionRepository.count()).isEqualTo(1);
        TransactionEntity txn = transactionRepository.findById("0000000000000001").orElseThrow();
        assertThat(txn.getAmount()).isEqualByComparingTo("123.45");
        assertThat(txn.getTypeCode()).isEqualTo("02");
        assertThat(txn.getCategoryCode()).isEqualTo(2);
        assertThat(txn.getSource()).isEqualTo("POS TERM");
        assertThat(txn.getDescription()).isEqualTo("BILL PAYMENT - ONLINE");
        assertThat(txn.getMerchantId()).isEqualTo(999999999L);
        assertThat(txn.getMerchantName()).isEqualTo("BILL PAYMENT");
        assertThat(txn.getMerchantCity()).isEqualTo("N/A");
        assertThat(txn.getMerchantZip()).isEqualTo("N/A");
        assertThat(txn.getCardNum()).isEqualTo("4111111111111111");
    }

    @Test
    void pay_idSequencing_isMaxPlusOne() { // AC-2
        seedAccount(13L, "5000.00");
        seedXref("4222222222222222", 13L);

        // Pre-existing transaction with id 100 → next id must be 101.
        TransactionEntity existing = new TransactionEntity();
        existing.setId("0000000000000100");
        existing.setTypeCode("01");
        existing.setCategoryCode(1);
        existing.setAmount(new BigDecimal("50.00"));
        transactionRepository.save(existing);

        BillPaymentResult r = service.process("13", "Y");
        assertThat(r.getTransactionId()).isEqualTo("0000000000000101");
        assertThat(transactionRepository.existsById("0000000000000101")).isTrue();
    }
}
