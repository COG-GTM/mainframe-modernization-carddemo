package com.carddemo;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DailyTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Helpers for building consistent test fixtures.
 */
public final class TestDataFactory {

    public static final String CARD_NUM = "4111111111111111";
    public static final long ACCT_ID = 10000000001L;
    public static final long CUST_ID = 900000001L;

    private TestDataFactory() {
    }

    public static CardXref xref() {
        return new CardXref(CARD_NUM, CUST_ID, ACCT_ID);
    }

    public static Account account() {
        Account account = new Account();
        account.setAcctId(ACCT_ID);
        account.setActiveStatus("Y");
        account.setCurrBal(new BigDecimal("100.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCashCreditLimit(new BigDecimal("1000.00"));
        account.setOpenDate(LocalDate.of(2020, 1, 1));
        account.setExpirationDate(LocalDate.of(2099, 12, 31));
        account.setReissueDate(LocalDate.of(2020, 1, 1));
        account.setCurrCycCredit(new BigDecimal("0.00"));
        account.setCurrCycDebit(new BigDecimal("0.00"));
        account.setAddrZip("12345");
        account.setGroupId("GRP1");
        return account;
    }

    public static DailyTransaction transaction(String id, BigDecimal amount) {
        DailyTransaction txn = new DailyTransaction();
        txn.setId(id);
        txn.setTypeCd("01");
        txn.setCatCd(5);
        txn.setSource("POS");
        txn.setDescription("TEST PURCHASE");
        txn.setAmount(amount);
        txn.setMerchantId(123456789L);
        txn.setMerchantName("TEST MERCHANT");
        txn.setMerchantCity("TEST CITY");
        txn.setMerchantZip("54321");
        txn.setCardNum(CARD_NUM);
        txn.setOrigTimestamp("2023-06-01-12.00.00.000000");
        txn.setProcTimestamp("");
        return txn;
    }
}
