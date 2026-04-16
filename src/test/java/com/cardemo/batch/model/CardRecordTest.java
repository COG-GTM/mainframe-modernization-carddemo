package com.cardemo.batch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardRecordTest {

    @Test
    void shouldCreateCardRecordWithAllFields() {
        CardRecord record = new CardRecord();
        record.setCardNum("4111111111111111");
        record.setCardAcctId("00000000001");
        record.setCardCvvCd("123");
        record.setCardEmbossedName("JOHN DOE");
        record.setCardExpiraionDate("2025-12-31");
        record.setCardActiveStatus("Y");

        assertEquals("4111111111111111", record.getCardNum());
        assertEquals("00000000001", record.getCardAcctId());
        assertEquals("123", record.getCardCvvCd());
        assertEquals("JOHN DOE", record.getCardEmbossedName());
        assertEquals("2025-12-31", record.getCardExpiraionDate());
        assertEquals("Y", record.getCardActiveStatus());
    }

    @Test
    void shouldDisplayAllFieldsInToString() {
        CardRecord record = new CardRecord();
        record.setCardNum("4111111111111111");
        record.setCardAcctId("00000000001");
        record.setCardCvvCd("123");
        record.setCardEmbossedName("JOHN DOE");
        record.setCardExpiraionDate("2025-12-31");
        record.setCardActiveStatus("Y");

        String str = record.toString();
        assertTrue(str.contains("cardNum"));
        assertTrue(str.contains("cardAcctId"));
        assertTrue(str.contains("cardCvvCd"));
        assertTrue(str.contains("cardEmbossedName"));
        assertTrue(str.contains("cardExpiraionDate"));
        assertTrue(str.contains("cardActiveStatus"));
    }

    @Test
    void shouldDefaultToNullFields() {
        CardRecord record = new CardRecord();
        assertNull(record.getCardNum());
        assertNull(record.getCardAcctId());
        assertNull(record.getCardActiveStatus());
    }
}
