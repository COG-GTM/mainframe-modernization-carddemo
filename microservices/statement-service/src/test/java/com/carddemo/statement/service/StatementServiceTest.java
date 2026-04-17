package com.carddemo.statement.service;

import com.carddemo.statement.dto.AccountData;
import com.carddemo.statement.dto.CardXrefData;
import com.carddemo.statement.dto.StatementRequest;
import com.carddemo.statement.dto.StatementResponse;
import com.carddemo.statement.dto.TransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private CardClient cardClient;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private StatementFormatter formatter;

    @InjectMocks
    private StatementService statementService;

    private AccountData testAccount;
    private List<CardXrefData> testCards;
    private List<TransactionData> testTransactions;

    @BeforeEach
    void setUp() {
        testAccount = new AccountData();
        testAccount.setAccountId("00000000001");
        testAccount.setCurrentBalance(new BigDecimal("1500.00"));
        testAccount.setCreditLimit(new BigDecimal("5000.00"));
        testAccount.setCustomerName("John Q Public");
        testAccount.setFicoScore(750);

        CardXrefData card1 = new CardXrefData("4111111111111111", "000000001", "00000000001");
        CardXrefData card2 = new CardXrefData("4222222222222222", "000000001", "00000000001");
        testCards = Arrays.asList(card1, card2);

        TransactionData txn1 = new TransactionData();
        txn1.setTransactionId("TXN001");
        txn1.setCardNumber("4111111111111111");
        txn1.setDescription("Purchase 1");
        txn1.setAmount(new BigDecimal("100.00"));

        TransactionData txn2 = new TransactionData();
        txn2.setTransactionId("TXN002");
        txn2.setCardNumber("4222222222222222");
        txn2.setDescription("Purchase 2");
        txn2.setAmount(new BigDecimal("200.00"));

        testTransactions = Arrays.asList(txn1, txn2);
    }

    @Test
    void generateStatement_shouldFetchAccountCardAndTransactionData() {
        StatementRequest request = new StatementRequest("00000000001", "2024-01-01", "2024-01-31");

        when(accountClient.getAccount("00000000001")).thenReturn(testAccount);
        when(cardClient.getCardsForAccount("00000000001")).thenReturn(testCards);
        when(transactionClient.getTransactions("4111111111111111", "2024-01-01", "2024-01-31"))
                .thenReturn(Collections.singletonList(testTransactions.get(0)));
        when(transactionClient.getTransactions("4222222222222222", "2024-01-01", "2024-01-31"))
                .thenReturn(Collections.singletonList(testTransactions.get(1)));
        when(formatter.formatTextStatement(eq(testAccount), anyList(), eq("2024-01-01"), eq("2024-01-31")))
                .thenReturn("TEXT OUTPUT");
        when(formatter.formatHtmlStatement(eq(testAccount), anyList(), eq("2024-01-01"), eq("2024-01-31")))
                .thenReturn("HTML OUTPUT");

        StatementResponse response = statementService.generateStatement(request);

        assertNotNull(response);
        assertEquals("00000000001", response.getAccountId());
        assertEquals("2024-01-01 to 2024-01-31", response.getStatementPeriod());
        assertEquals("TEXT OUTPUT", response.getTextStatement());
        assertEquals("HTML OUTPUT", response.getHtmlStatement());

        verify(accountClient).getAccount("00000000001");
        verify(cardClient).getCardsForAccount("00000000001");
        verify(transactionClient).getTransactions("4111111111111111", "2024-01-01", "2024-01-31");
        verify(transactionClient).getTransactions("4222222222222222", "2024-01-01", "2024-01-31");
    }

    @Test
    void generateStatement_withMultipleCards_shouldFetchTransactionsForEachCard() {
        StatementRequest request = new StatementRequest("00000000001", "2024-01-01", "2024-01-31");

        when(accountClient.getAccount("00000000001")).thenReturn(testAccount);
        when(cardClient.getCardsForAccount("00000000001")).thenReturn(testCards);
        when(transactionClient.getTransactions(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(formatter.formatTextStatement(eq(testAccount), anyList(), anyString(), anyString()))
                .thenReturn("TEXT");
        when(formatter.formatHtmlStatement(eq(testAccount), anyList(), anyString(), anyString()))
                .thenReturn("HTML");

        statementService.generateStatement(request);

        verify(transactionClient, times(2)).getTransactions(anyString(), anyString(), anyString());
    }

    @Test
    void generateStatement_withNoCards_shouldStillGenerateStatement() {
        StatementRequest request = new StatementRequest("00000000001", "2024-01-01", "2024-01-31");

        when(accountClient.getAccount("00000000001")).thenReturn(testAccount);
        when(cardClient.getCardsForAccount("00000000001")).thenReturn(Collections.emptyList());
        when(formatter.formatTextStatement(eq(testAccount), anyList(), anyString(), anyString()))
                .thenReturn("TEXT");
        when(formatter.formatHtmlStatement(eq(testAccount), anyList(), anyString(), anyString()))
                .thenReturn("HTML");

        StatementResponse response = statementService.generateStatement(request);

        assertNotNull(response);
        verify(transactionClient, never()).getTransactions(anyString(), anyString(), anyString());
    }

    @Test
    void generateStatement_whenAccountServiceFails_shouldThrowException() {
        StatementRequest request = new StatementRequest("00000000001", "2024-01-01", "2024-01-31");

        when(accountClient.getAccount("00000000001"))
                .thenThrow(new RuntimeException("Account service unavailable"));

        assertThrows(RuntimeException.class, () -> statementService.generateStatement(request));
    }

    @Test
    void generateStatement_whenCardServiceFails_shouldThrowException() {
        StatementRequest request = new StatementRequest("00000000001", "2024-01-01", "2024-01-31");

        when(accountClient.getAccount("00000000001")).thenReturn(testAccount);
        when(cardClient.getCardsForAccount("00000000001"))
                .thenThrow(new RuntimeException("Card service unavailable"));

        assertThrows(RuntimeException.class, () -> statementService.generateStatement(request));
    }

    @Test
    void generateStatement_whenTransactionServiceFails_shouldThrowException() {
        StatementRequest request = new StatementRequest("00000000001", "2024-01-01", "2024-01-31");

        when(accountClient.getAccount("00000000001")).thenReturn(testAccount);
        when(cardClient.getCardsForAccount("00000000001")).thenReturn(testCards);
        when(transactionClient.getTransactions("4111111111111111", "2024-01-01", "2024-01-31"))
                .thenThrow(new RuntimeException("Transaction service unavailable"));

        assertThrows(RuntimeException.class, () -> statementService.generateStatement(request));
    }

    @Test
    void generateStatement_shouldReturnBothFormats() {
        StatementRequest request = new StatementRequest("00000000001", "2024-01-01", "2024-01-31");

        when(accountClient.getAccount("00000000001")).thenReturn(testAccount);
        when(cardClient.getCardsForAccount("00000000001")).thenReturn(Collections.emptyList());
        when(formatter.formatTextStatement(eq(testAccount), anyList(), anyString(), anyString()))
                .thenReturn("PLAIN TEXT CONTENT");
        when(formatter.formatHtmlStatement(eq(testAccount), anyList(), anyString(), anyString()))
                .thenReturn("<html>HTML CONTENT</html>");

        StatementResponse response = statementService.generateStatement(request);

        assertTrue(response.getTextStatement().contains("PLAIN TEXT"));
        assertTrue(response.getHtmlStatement().contains("<html>"));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> anyList() {
        return org.mockito.ArgumentMatchers.anyList();
    }
}
