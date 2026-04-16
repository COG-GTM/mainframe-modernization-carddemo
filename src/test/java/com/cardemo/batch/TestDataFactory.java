package com.cardemo.batch;

import com.cardemo.batch.entity.Account;
import com.cardemo.batch.entity.CardXref;
import com.cardemo.batch.entity.Customer;
import com.cardemo.batch.entity.TransactionRecord;
import com.cardemo.batch.service.StatementData;

import java.math.BigDecimal;
import java.util.List;

/**
 * Factory for creating test data objects matching COBOL copybook structures.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Customer createCustomer(String custId) {
        Customer c = new Customer();
        c.setCustId(custId);
        c.setFirstName("John");
        c.setMiddleName("M");
        c.setLastName("Doe");
        c.setAddrLine1("123 Main St");
        c.setAddrLine2("Apt 4B");
        c.setAddrLine3("Downtown");
        c.setAddrStateCd("WA");
        c.setAddrCountryCd("US");
        c.setAddrZip("98101");
        c.setFicoCreditScore(750);
        return c;
    }

    public static Account createAccount(String acctId) {
        Account a = new Account();
        a.setAcctId(acctId);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal("5000.50"));
        a.setCreditLimit(new BigDecimal("10000.00"));
        a.setCashCreditLimit(new BigDecimal("2000.00"));
        a.setOpenDate("2020-01-15");
        a.setExpirationDate("2025-01-15");
        return a;
    }

    public static CardXref createCardXref(String cardNum, String custId, String acctId) {
        CardXref x = new CardXref();
        x.setCardNum(cardNum);
        x.setCustId(custId);
        x.setAcctId(acctId);
        return x;
    }

    public static TransactionRecord createTransaction(String cardNum,
                                                       String tranId,
                                                       String description,
                                                       BigDecimal amount) {
        TransactionRecord t = new TransactionRecord();
        t.setCardNum(cardNum);
        t.setTranId(tranId);
        t.setTypeCd("PR");
        t.setCatCd(5411);
        t.setSource("ONLINE");
        t.setDescription(description);
        t.setAmount(amount);
        t.setMerchantId(123456789);
        t.setMerchantName("Test Merchant");
        t.setMerchantCity("Seattle");
        t.setMerchantZip("98101");
        t.setOrigTimestamp("2024-01-15T10:30:00.000000");
        t.setProcTimestamp("2024-01-15T10:30:01.000000");
        return t;
    }

    public static StatementData createStatementData() {
        Customer customer = createCustomer("000000001");
        Account account = createAccount("00000000001");
        String cardNum = "4111111111111111";

        List<TransactionRecord> transactions = List.of(
                createTransaction(cardNum, "0000000000000001",
                        "Grocery Store Purchase", new BigDecimal("45.67")),
                createTransaction(cardNum, "0000000000000002",
                        "Gas Station", new BigDecimal("32.10")),
                createTransaction(cardNum, "0000000000000003",
                        "Restaurant Dinner", new BigDecimal("78.50"))
        );

        BigDecimal totalAmount = new BigDecimal("156.27");

        return new StatementData(cardNum, customer, account, transactions, totalAmount);
    }

    public static StatementData createStatementDataWithNegativeAmount() {
        Customer customer = createCustomer("000000002");
        Account account = createAccount("00000000002");
        account.setCurrBal(new BigDecimal("-250.75"));
        String cardNum = "4222222222222222";

        List<TransactionRecord> transactions = List.of(
                createTransaction(cardNum, "0000000000000010",
                        "Refund - Online Return", new BigDecimal("-50.00")),
                createTransaction(cardNum, "0000000000000011",
                        "Coffee Shop", new BigDecimal("4.50"))
        );

        BigDecimal totalAmount = new BigDecimal("-45.50");

        return new StatementData(cardNum, customer, account, transactions, totalAmount);
    }
}
