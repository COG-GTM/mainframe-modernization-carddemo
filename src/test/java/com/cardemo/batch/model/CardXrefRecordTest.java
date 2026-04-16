package com.cardemo.batch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardXrefRecordTest {

    @Test
    void shouldCreateCardXrefRecordWithAllFields() {
        CardXrefRecord record = new CardXrefRecord();
        record.setXrefCardNum("4111111111111111");
        record.setXrefCustId("000000001");
        record.setXrefAcctId("00000000001");

        assertEquals("4111111111111111", record.getXrefCardNum());
        assertEquals("000000001", record.getXrefCustId());
        assertEquals("00000000001", record.getXrefAcctId());
    }

    @Test
    void shouldDisplayAllFieldsInToString() {
        CardXrefRecord record = new CardXrefRecord();
        record.setXrefCardNum("4111111111111111");
        record.setXrefCustId("000000001");
        record.setXrefAcctId("00000000001");

        String str = record.toString();
        assertTrue(str.contains("xrefCardNum"));
        assertTrue(str.contains("xrefCustId"));
        assertTrue(str.contains("xrefAcctId"));
    }

    @Test
    void shouldDefaultToNullFields() {
        CardXrefRecord record = new CardXrefRecord();
        assertNull(record.getXrefCardNum());
        assertNull(record.getXrefCustId());
        assertNull(record.getXrefAcctId());
    }
}
