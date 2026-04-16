package com.cardemo.batch.service;

import com.cardemo.batch.TestDataFactory;
import com.cardemo.batch.entity.Account;
import com.cardemo.batch.entity.CardXref;
import com.cardemo.batch.entity.Customer;
import com.cardemo.batch.entity.TransactionRecord;
import com.cardemo.batch.formatter.HtmlStatementFormatter;
import com.cardemo.batch.formatter.PlainTextStatementFormatter;
import com.cardemo.batch.service.StatementGenerationService.StatementResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StatementGenerationService.
 * Tests the main business logic ported from CBSTM03A.
 */
@ExtendWith(MockitoExtension.class)
class StatementGenerationServiceTest {

    @Mock
    private StatementFileIOService fileIOService;

    private StatementGenerationService service;

    @BeforeEach
    void setUp() {
        PlainTextStatementFormatter textFormatter = new PlainTextStatementFormatter();
        HtmlStatementFormatter htmlFormatter = new HtmlStatementFormatter();
        service = new StatementGenerationService(fileIOService, textFormatter, htmlFormatter);
    }

    @Test
    void generateStatementForCard_producesPlainTextAndHtml() {
        CardXref xref = TestDataFactory.createCardXref(
                "4111111111111111", "000000001", "00000000001");
        Customer customer = TestDataFactory.createCustomer("000000001");
        Account account = TestDataFactory.createAccount("00000000001");
        List<TransactionRecord> transactions = List.of(
                TestDataFactory.createTransaction("4111111111111111",
                        "0000000000000001", "Purchase", new BigDecimal("100.00"))
        );

        when(fileIOService.readCustomerByKey("000000001"))
                .thenReturn(Optional.of(customer));
        when(fileIOService.readAccountByKey("00000000001"))
                .thenReturn(Optional.of(account));
        when(fileIOService.readTransactionsByCard("4111111111111111"))
                .thenReturn(transactions);

        StatementResult result = service.generateStatementForCard(xref);

        assertNotNull(result);
        assertEquals("4111111111111111", result.getCardNum());
        assertFalse(result.getPlainTextLines().isEmpty());
        assertFalse(result.getHtmlLines().isEmpty());
    }

    @Test
    void generateStatementForCard_throwsWhenCustomerNotFound() {
        CardXref xref = TestDataFactory.createCardXref(
                "4111111111111111", "999999999", "00000000001");

        when(fileIOService.readCustomerByKey("999999999"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.generateStatementForCard(xref));
    }

    @Test
    void generateStatementForCard_throwsWhenAccountNotFound() {
        CardXref xref = TestDataFactory.createCardXref(
                "4111111111111111", "000000001", "99999999999");
        Customer customer = TestDataFactory.createCustomer("000000001");

        when(fileIOService.readCustomerByKey("000000001"))
                .thenReturn(Optional.of(customer));
        when(fileIOService.readAccountByKey("99999999999"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.generateStatementForCard(xref));
    }

    @Test
    void generateAllStatements_processesAllXrefs() {
        CardXref xref1 = TestDataFactory.createCardXref(
                "4111111111111111", "000000001", "00000000001");
        CardXref xref2 = TestDataFactory.createCardXref(
                "4222222222222222", "000000002", "00000000002");

        when(fileIOService.readAllXrefs()).thenReturn(List.of(xref1, xref2));
        when(fileIOService.readCustomerByKey(anyString()))
                .thenReturn(Optional.of(TestDataFactory.createCustomer("000000001")));
        when(fileIOService.readAccountByKey(anyString()))
                .thenReturn(Optional.of(TestDataFactory.createAccount("00000000001")));
        when(fileIOService.readTransactionsByCard(anyString()))
                .thenReturn(Collections.emptyList());

        List<StatementResult> results = service.generateAllStatements();

        assertEquals(2, results.size());
    }

    @Test
    void computeTotalAmount_sumsCorrectly() {
        List<TransactionRecord> transactions = List.of(
                TestDataFactory.createTransaction("card1", "t1", "desc1",
                        new BigDecimal("45.67")),
                TestDataFactory.createTransaction("card1", "t2", "desc2",
                        new BigDecimal("32.10")),
                TestDataFactory.createTransaction("card1", "t3", "desc3",
                        new BigDecimal("78.50"))
        );

        BigDecimal total = service.computeTotalAmount(transactions);

        assertEquals(new BigDecimal("156.27"), total);
    }

    @Test
    void computeTotalAmount_handlesNegativeAmounts() {
        List<TransactionRecord> transactions = List.of(
                TestDataFactory.createTransaction("card1", "t1", "Purchase",
                        new BigDecimal("100.00")),
                TestDataFactory.createTransaction("card1", "t2", "Refund",
                        new BigDecimal("-50.00"))
        );

        BigDecimal total = service.computeTotalAmount(transactions);

        assertEquals(new BigDecimal("50.00"), total);
    }

    @Test
    void computeTotalAmount_handlesEmptyList() {
        BigDecimal total = service.computeTotalAmount(Collections.emptyList());
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void computeTotalAmount_handlesNullAmounts() {
        TransactionRecord txn = new TransactionRecord();
        txn.setCardNum("card1");
        txn.setTranId("t1");
        txn.setAmount(null);

        BigDecimal total = service.computeTotalAmount(List.of(txn));

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void generateAllStatements_continuesOnError() {
        CardXref xref1 = TestDataFactory.createCardXref(
                "4111111111111111", "BAD_CUST", "00000000001");
        CardXref xref2 = TestDataFactory.createCardXref(
                "4222222222222222", "000000002", "00000000002");

        when(fileIOService.readAllXrefs()).thenReturn(List.of(xref1, xref2));
        // First xref: customer not found (will throw)
        when(fileIOService.readCustomerByKey("BAD_CUST"))
                .thenReturn(Optional.empty());
        // Second xref: succeeds
        when(fileIOService.readCustomerByKey("000000002"))
                .thenReturn(Optional.of(TestDataFactory.createCustomer("000000002")));
        when(fileIOService.readAccountByKey("00000000002"))
                .thenReturn(Optional.of(TestDataFactory.createAccount("00000000002")));
        when(fileIOService.readTransactionsByCard("4222222222222222"))
                .thenReturn(Collections.emptyList());

        List<StatementResult> results = service.generateAllStatements();

        // Only the second one succeeds
        assertEquals(1, results.size());
        assertEquals("4222222222222222", results.get(0).getCardNum());
    }

    @Test
    void generateStatementForCard_crossReferenceChain() {
        // Verify the card -> account -> customer chain
        String cardNum = "4111111111111111";
        String custId = "000000001";
        String acctId = "00000000001";
        CardXref xref = TestDataFactory.createCardXref(cardNum, custId, acctId);
        Customer customer = TestDataFactory.createCustomer(custId);
        Account account = TestDataFactory.createAccount(acctId);

        when(fileIOService.readCustomerByKey(custId))
                .thenReturn(Optional.of(customer));
        when(fileIOService.readAccountByKey(acctId))
                .thenReturn(Optional.of(account));
        when(fileIOService.readTransactionsByCard(cardNum))
                .thenReturn(Collections.emptyList());

        StatementResult result = service.generateStatementForCard(xref);

        assertNotNull(result);
        // Verify plain text contains the customer name from the resolved chain
        String text = String.join("\n", result.getPlainTextLines());
        assertTrue(text.contains("John"));
        assertTrue(text.contains("Doe"));
        // Verify account ID appears
        assertTrue(text.contains(acctId));
    }
}
