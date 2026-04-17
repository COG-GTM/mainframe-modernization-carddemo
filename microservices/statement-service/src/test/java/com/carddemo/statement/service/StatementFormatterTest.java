package com.carddemo.statement.service;

import com.carddemo.statement.dto.AccountData;
import com.carddemo.statement.dto.TransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatementFormatterTest {

    private StatementFormatter formatter;
    private AccountData account;
    private List<TransactionData> transactions;

    @BeforeEach
    void setUp() {
        formatter = new StatementFormatter();

        account = new AccountData();
        account.setAccountId("00000000001");
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("1500.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCashCreditLimit(new BigDecimal("1000.00"));
        account.setOpenDate("2020-01-15");
        account.setExpirationDate("2025-01-15");
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("350.00"));
        account.setCustomerName("John Q Public");
        account.setAddressLine1("123 Main Street");
        account.setAddressLine2("Apt 4B");
        account.setAddressLine3("Springfield");
        account.setStateCode("IL");
        account.setCountryCode("US");
        account.setZipCode("62704");
        account.setFicoScore(750);

        transactions = new ArrayList<>();

        TransactionData txn1 = new TransactionData();
        txn1.setCardNumber("4111111111111111");
        txn1.setTransactionId("TXN00000000001");
        txn1.setTypeCode("SA");
        txn1.setCategoryCode(5411);
        txn1.setSource("POS");
        txn1.setDescription("Grocery Store Purchase");
        txn1.setAmount(new BigDecimal("125.50"));
        txn1.setMerchantId("987654321");
        txn1.setMerchantName("FreshMart");
        txn1.setMerchantCity("Springfield");
        txn1.setMerchantZip("62704");
        txn1.setOriginTimestamp("2024-01-15T10:30:00.000000");
        txn1.setProcessTimestamp("2024-01-15T10:30:05.000000");
        transactions.add(txn1);

        TransactionData txn2 = new TransactionData();
        txn2.setCardNumber("4111111111111111");
        txn2.setTransactionId("TXN00000000002");
        txn2.setTypeCode("SA");
        txn2.setCategoryCode(5812);
        txn2.setSource("POS");
        txn2.setDescription("Restaurant Dinner");
        txn2.setAmount(new BigDecimal("75.25"));
        txn2.setMerchantId("123456789");
        txn2.setMerchantName("Olive Garden");
        txn2.setMerchantCity("Springfield");
        txn2.setMerchantZip("62704");
        txn2.setOriginTimestamp("2024-01-16T19:00:00.000000");
        txn2.setProcessTimestamp("2024-01-16T19:00:03.000000");
        transactions.add(txn2);

        TransactionData txn3 = new TransactionData();
        txn3.setCardNumber("4111111111111111");
        txn3.setTransactionId("TXN00000000003");
        txn3.setTypeCode("CR");
        txn3.setCategoryCode(9999);
        txn3.setSource("ONLINE");
        txn3.setDescription("Payment - Thank You");
        txn3.setAmount(new BigDecimal("-50.00"));
        txn3.setMerchantId("000000000");
        txn3.setMerchantName("Online Payment");
        txn3.setMerchantCity("N/A");
        txn3.setMerchantZip("00000");
        txn3.setOriginTimestamp("2024-01-17T08:00:00.000000");
        txn3.setProcessTimestamp("2024-01-17T08:00:01.000000");
        transactions.add(txn3);
    }

    @Test
    void formatTextStatement_shouldContainStatementMarkers() {
        String result = formatter.formatTextStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertNotNull(result);
        assertTrue(result.contains("START OF STATEMENT"));
        assertTrue(result.contains("END OF STATEMENT"));
    }

    @Test
    void formatTextStatement_shouldContainCustomerInfo() {
        String result = formatter.formatTextStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("John Q Public"));
        assertTrue(result.contains("123 Main Street"));
        assertTrue(result.contains("Apt 4B"));
        assertTrue(result.contains("Springfield"));
    }

    @Test
    void formatTextStatement_shouldContainAccountDetails() {
        String result = formatter.formatTextStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("Account ID"));
        assertTrue(result.contains("00000000001"));
        assertTrue(result.contains("Current Balance"));
        assertTrue(result.contains("1,500.00"));
        assertTrue(result.contains("FICO Score"));
        assertTrue(result.contains("750"));
        assertTrue(result.contains("Credit Limit"));
        assertTrue(result.contains("5,000.00"));
    }

    @Test
    void formatTextStatement_shouldContainTransactions() {
        String result = formatter.formatTextStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("TRANSACTION SUMMARY"));
        assertTrue(result.contains("TXN00000000001"));
        assertTrue(result.contains("Grocery Store Purchase"));
        assertTrue(result.contains("125.50"));
        assertTrue(result.contains("TXN00000000002"));
        assertTrue(result.contains("Restaurant Dinner"));
        assertTrue(result.contains("75.25"));
        assertTrue(result.contains("TXN00000000003"));
        assertTrue(result.contains("Payment - Thank You"));
    }

    @Test
    void formatTextStatement_shouldContainTotals() {
        String result = formatter.formatTextStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("Total Debits"));
        assertTrue(result.contains("200.75"));
        assertTrue(result.contains("Total Credits"));
        assertTrue(result.contains("50.00"));
        assertTrue(result.contains("Total EXP:"));
    }

    @Test
    void formatTextStatement_shouldContainStatementPeriod() {
        String result = formatter.formatTextStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("Statement Period"));
        assertTrue(result.contains("2024-01-01"));
        assertTrue(result.contains("2024-01-31"));
    }

    @Test
    void formatTextStatement_withEmptyTransactions() {
        String result = formatter.formatTextStatement(account, new ArrayList<>(), "2024-01-01", "2024-01-31");

        assertNotNull(result);
        assertTrue(result.contains("START OF STATEMENT"));
        assertTrue(result.contains("END OF STATEMENT"));
        assertTrue(result.contains("TRANSACTION SUMMARY"));
        assertTrue(result.contains("00000000001"));
    }

    @Test
    void formatHtmlStatement_shouldBeValidHtml() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html>"));
        assertTrue(result.contains("<html lang=\"en\">"));
        assertTrue(result.contains("</html>"));
        assertTrue(result.contains("<head>"));
        assertTrue(result.contains("</head>"));
        assertTrue(result.contains("<body"));
        assertTrue(result.contains("</body>"));
    }

    @Test
    void formatHtmlStatement_shouldContainBankInfo() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("Bank of XYZ"));
        assertTrue(result.contains("410 Terry Ave N"));
        assertTrue(result.contains("Seattle WA 99999"));
    }

    @Test
    void formatHtmlStatement_shouldContainAccountNumber() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("Statement for Account Number: 00000000001"));
    }

    @Test
    void formatHtmlStatement_shouldContainCustomerInfo() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("John Q Public"));
        assertTrue(result.contains("123 Main Street"));
        assertTrue(result.contains("Apt 4B"));
    }

    @Test
    void formatHtmlStatement_shouldContainBasicDetails() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("Basic Details"));
        assertTrue(result.contains("Account ID"));
        assertTrue(result.contains("Current Balance"));
        assertTrue(result.contains("FICO Score"));
        assertTrue(result.contains("Credit Limit"));
    }

    @Test
    void formatHtmlStatement_shouldContainTransactionSummary() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("Transaction Summary"));
        assertTrue(result.contains("Tran ID"));
        assertTrue(result.contains("Tran Details"));
        assertTrue(result.contains("Amount"));
    }

    @Test
    void formatHtmlStatement_shouldContainTransactionData() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("TXN00000000001"));
        assertTrue(result.contains("Grocery Store Purchase"));
        assertTrue(result.contains("$125.50"));
        assertTrue(result.contains("TXN00000000002"));
        assertTrue(result.contains("Restaurant Dinner"));
        assertTrue(result.contains("$75.25"));
    }

    @Test
    void formatHtmlStatement_shouldContainEndOfStatement() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("End of Statement"));
    }

    @Test
    void formatHtmlStatement_shouldUseInlineStyles() {
        String result = formatter.formatHtmlStatement(account, transactions, "2024-01-01", "2024-01-31");

        assertTrue(result.contains("background-color:#1d1d96b3"));
        assertTrue(result.contains("background-color:#FFAF33"));
        assertTrue(result.contains("background-color:#f2f2f2"));
        assertTrue(result.contains("background-color:#33FFD1"));
        assertTrue(result.contains("background-color:#33FF5E"));
    }

    @Test
    void formatHtmlStatement_withEmptyTransactions() {
        String result = formatter.formatHtmlStatement(account, new ArrayList<>(), "2024-01-01", "2024-01-31");

        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html>"));
        assertTrue(result.contains("</html>"));
        assertTrue(result.contains("Transaction Summary"));
        assertFalse(result.contains("TXN00000000001"));
    }

    @Test
    void formatTextStatement_withNullFields() {
        AccountData sparseAccount = new AccountData();
        sparseAccount.setAccountId("99999999999");
        sparseAccount.setCurrentBalance(BigDecimal.ZERO);
        sparseAccount.setCreditLimit(BigDecimal.ZERO);
        sparseAccount.setFicoScore(0);

        String result = formatter.formatTextStatement(sparseAccount, new ArrayList<>(), "2024-01-01", "2024-01-31");

        assertNotNull(result);
        assertTrue(result.contains("99999999999"));
        assertTrue(result.contains("START OF STATEMENT"));
        assertTrue(result.contains("END OF STATEMENT"));
    }

    @Test
    void formatHtmlStatement_withNullFields() {
        AccountData sparseAccount = new AccountData();
        sparseAccount.setAccountId("99999999999");
        sparseAccount.setCurrentBalance(BigDecimal.ZERO);
        sparseAccount.setCreditLimit(BigDecimal.ZERO);
        sparseAccount.setFicoScore(0);

        String result = formatter.formatHtmlStatement(sparseAccount, new ArrayList<>(), "2024-01-01", "2024-01-31");

        assertNotNull(result);
        assertTrue(result.contains("99999999999"));
        assertTrue(result.contains("<!DOCTYPE html>"));
        assertTrue(result.contains("</html>"));
    }
}
