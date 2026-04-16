package com.cardemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordTest {

    @Test
    void shouldCreateAccountRecordWithAllFields() {
        AccountRecord record = new AccountRecord();
        record.setAcctId("00000000001");
        record.setAcctActiveStatus("Y");
        record.setAcctCurrBal(new BigDecimal("1500.00"));
        record.setAcctCreditLimit(new BigDecimal("5000.00"));
        record.setAcctCashCreditLimit(new BigDecimal("1500.00"));
        record.setAcctOpenDate("2020-01-15");
        record.setAcctExpiraionDate("2025-01-15");
        record.setAcctReissueDate("2023-01-15");
        record.setAcctCurrCycCredit(new BigDecimal("200.00"));
        record.setAcctCurrCycDebit(new BigDecimal("150.00"));
        record.setAcctAddrZip("10001");
        record.setAcctGroupId("GRP001");

        assertEquals("00000000001", record.getAcctId());
        assertEquals("Y", record.getAcctActiveStatus());
        assertEquals(new BigDecimal("1500.00"), record.getAcctCurrBal());
        assertEquals(new BigDecimal("5000.00"), record.getAcctCreditLimit());
        assertEquals(new BigDecimal("1500.00"), record.getAcctCashCreditLimit());
        assertEquals("2020-01-15", record.getAcctOpenDate());
        assertEquals("2025-01-15", record.getAcctExpiraionDate());
        assertEquals("2023-01-15", record.getAcctReissueDate());
        assertEquals(new BigDecimal("200.00"), record.getAcctCurrCycCredit());
        assertEquals(new BigDecimal("150.00"), record.getAcctCurrCycDebit());
        assertEquals("10001", record.getAcctAddrZip());
        assertEquals("GRP001", record.getAcctGroupId());
    }

    @Test
    void shouldPreserveExpiraionDateTypo() {
        AccountRecord record = new AccountRecord();
        record.setAcctExpiraionDate("2025-12-31");
        assertEquals("2025-12-31", record.getAcctExpiraionDate());
    }

    @Test
    void shouldHandleAllElevenDisplayFields() {
        AccountRecord record = new AccountRecord();
        record.setAcctId("00000000001");
        record.setAcctActiveStatus("Y");
        record.setAcctCurrBal(BigDecimal.ZERO);
        record.setAcctCreditLimit(BigDecimal.ZERO);
        record.setAcctCashCreditLimit(BigDecimal.ZERO);
        record.setAcctOpenDate("2020-01-01");
        record.setAcctExpiraionDate("2025-01-01");
        record.setAcctReissueDate("2023-01-01");
        record.setAcctCurrCycCredit(BigDecimal.ZERO);
        record.setAcctCurrCycDebit(BigDecimal.ZERO);
        record.setAcctGroupId("GRP001");

        String str = record.toString();
        assertTrue(str.contains("acctId"));
        assertTrue(str.contains("acctActiveStatus"));
        assertTrue(str.contains("acctCurrBal"));
        assertTrue(str.contains("acctCreditLimit"));
        assertTrue(str.contains("acctCashCreditLimit"));
        assertTrue(str.contains("acctOpenDate"));
        assertTrue(str.contains("acctExpiraionDate"));
        assertTrue(str.contains("acctReissueDate"));
        assertTrue(str.contains("acctCurrCycCredit"));
        assertTrue(str.contains("acctCurrCycDebit"));
        assertTrue(str.contains("acctGroupId"));
    }

    @Test
    void shouldDefaultToNullFields() {
        AccountRecord record = new AccountRecord();
        assertNull(record.getAcctId());
        assertNull(record.getAcctActiveStatus());
        assertNull(record.getAcctCurrBal());
        assertNull(record.getAcctCreditLimit());
        assertNull(record.getAcctGroupId());
    }
}
