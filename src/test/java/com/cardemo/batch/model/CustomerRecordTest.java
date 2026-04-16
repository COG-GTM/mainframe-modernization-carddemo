package com.cardemo.batch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRecordTest {

    @Test
    void shouldCreateCustomerRecordWithAllFields() {
        CustomerRecord record = new CustomerRecord();
        record.setCustId("000000001");
        record.setCustFirstName("John");
        record.setCustMiddleName("A");
        record.setCustLastName("Doe");
        record.setCustAddrLine1("123 Main St");
        record.setCustAddrLine2("Apt 4B");
        record.setCustAddrLine3("");
        record.setCustAddrStateCd("NY");
        record.setCustAddrCountryCd("USA");
        record.setCustAddrZip("10001");
        record.setCustPhoneNum1("212-555-0101");
        record.setCustPhoneNum2("212-555-0102");
        record.setCustSsn("123456789");
        record.setCustGovtIssuedId("DL12345678");
        record.setCustDobYyyyMmDd("1980-05-15");
        record.setCustEftAccountId("EFT0000001");
        record.setCustPriCardHolderInd("Y");
        record.setCustFicoCreditScore("750");

        assertEquals("000000001", record.getCustId());
        assertEquals("John", record.getCustFirstName());
        assertEquals("A", record.getCustMiddleName());
        assertEquals("Doe", record.getCustLastName());
        assertEquals("123 Main St", record.getCustAddrLine1());
        assertEquals("Apt 4B", record.getCustAddrLine2());
        assertEquals("", record.getCustAddrLine3());
        assertEquals("NY", record.getCustAddrStateCd());
        assertEquals("USA", record.getCustAddrCountryCd());
        assertEquals("10001", record.getCustAddrZip());
        assertEquals("212-555-0101", record.getCustPhoneNum1());
        assertEquals("212-555-0102", record.getCustPhoneNum2());
        assertEquals("123456789", record.getCustSsn());
        assertEquals("DL12345678", record.getCustGovtIssuedId());
        assertEquals("1980-05-15", record.getCustDobYyyyMmDd());
        assertEquals("EFT0000001", record.getCustEftAccountId());
        assertEquals("Y", record.getCustPriCardHolderInd());
        assertEquals("750", record.getCustFicoCreditScore());
    }

    @Test
    void shouldDisplayAllFieldsInToString() {
        CustomerRecord record = new CustomerRecord();
        record.setCustId("000000001");
        record.setCustFirstName("John");
        record.setCustLastName("Doe");

        String str = record.toString();
        assertTrue(str.contains("custId"));
        assertTrue(str.contains("custFirstName"));
        assertTrue(str.contains("custLastName"));
    }

    @Test
    void shouldDefaultToNullFields() {
        CustomerRecord record = new CustomerRecord();
        assertNull(record.getCustId());
        assertNull(record.getCustFirstName());
        assertNull(record.getCustFicoCreditScore());
    }
}
