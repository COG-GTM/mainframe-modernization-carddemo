package com.carddemo.shared.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createAndVerifyAllFields() {
        AccountRecord record = new AccountRecord();
        record.setAcctId(12345678901L);
        record.setAcctActiveStatus("Y");
        record.setAcctCurrBal(new BigDecimal("5000.50"));
        record.setAcctCreditLimit(new BigDecimal("10000.00"));
        record.setAcctCashCreditLimit(new BigDecimal("3000.00"));
        record.setAcctOpenDate("2020-01-15");
        record.setAcctExpirationDate("2025-01-15");
        record.setAcctReissueDate("2023-01-15");
        record.setAcctCurrCycCredit(new BigDecimal("1500.25"));
        record.setAcctCurrCycDebit(new BigDecimal("750.10"));
        record.setAcctAddrZip("10001");
        record.setAcctGroupId("GRP001");

        assertEquals(12345678901L, record.getAcctId());
        assertEquals("Y", record.getAcctActiveStatus());
        assertEquals(new BigDecimal("5000.50"), record.getAcctCurrBal());
        assertEquals(new BigDecimal("10000.00"), record.getAcctCreditLimit());
        assertEquals(new BigDecimal("3000.00"), record.getAcctCashCreditLimit());
        assertEquals("2020-01-15", record.getAcctOpenDate());
        assertEquals("2025-01-15", record.getAcctExpirationDate());
        assertEquals("2023-01-15", record.getAcctReissueDate());
        assertEquals(new BigDecimal("1500.25"), record.getAcctCurrCycCredit());
        assertEquals(new BigDecimal("750.10"), record.getAcctCurrCycDebit());
        assertEquals("10001", record.getAcctAddrZip());
        assertEquals("GRP001", record.getAcctGroupId());
    }

    @Test
    void equalsAndHashCode() {
        AccountRecord r1 = new AccountRecord();
        r1.setAcctId(1L);
        r1.setAcctActiveStatus("Y");
        r1.setAcctCurrBal(new BigDecimal("100.00"));

        AccountRecord r2 = new AccountRecord();
        r2.setAcctId(1L);
        r2.setAcctActiveStatus("Y");
        r2.setAcctCurrBal(new BigDecimal("100.00"));

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        r2.setAcctId(2L);
        assertNotEquals(r1, r2);
    }

    @Test
    void toStringContainsFields() {
        AccountRecord record = new AccountRecord();
        record.setAcctId(999L);
        String str = record.toString();
        assertTrue(str.contains("acctId=999"));
    }

    @Test
    void jsonSerialization() throws Exception {
        AccountRecord record = new AccountRecord();
        record.setAcctId(11111111111L);
        record.setAcctActiveStatus("Y");
        record.setAcctCurrBal(new BigDecimal("2500.75"));

        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("\"acctId\":11111111111"));
        assertTrue(json.contains("\"acctActiveStatus\":\"Y\""));
        assertTrue(json.contains("\"acctCurrBal\":2500.75"));

        AccountRecord deserialized = mapper.readValue(json, AccountRecord.class);
        assertEquals(record.getAcctId(), deserialized.getAcctId());
        assertEquals(record.getAcctActiveStatus(), deserialized.getAcctActiveStatus());
    }

    @Test
    void monetaryFieldsAreBigDecimal() {
        AccountRecord record = new AccountRecord();
        record.setAcctCurrBal(new BigDecimal("9999999999.99"));
        record.setAcctCreditLimit(new BigDecimal("-1234567890.12"));
        record.setAcctCashCreditLimit(BigDecimal.ZERO);
        record.setAcctCurrCycCredit(new BigDecimal("0.01"));
        record.setAcctCurrCycDebit(new BigDecimal("0.99"));

        assertInstanceOf(BigDecimal.class, record.getAcctCurrBal());
        assertInstanceOf(BigDecimal.class, record.getAcctCreditLimit());
        assertInstanceOf(BigDecimal.class, record.getAcctCashCreditLimit());
        assertInstanceOf(BigDecimal.class, record.getAcctCurrCycCredit());
        assertInstanceOf(BigDecimal.class, record.getAcctCurrCycDebit());
    }
}
