package com.carddemo.shared.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardRecordTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createAndVerifyAllFields() {
        CardRecord record = new CardRecord();
        record.setCardNum("4111111111111111");
        record.setCardAcctId(12345678901L);
        record.setCardCvvCd(123);
        record.setCardEmbossedName("JOHN DOE");
        record.setCardExpirationDate("2025-12-31");
        record.setCardActiveStatus("Y");

        assertEquals("4111111111111111", record.getCardNum());
        assertEquals(12345678901L, record.getCardAcctId());
        assertEquals(123, record.getCardCvvCd());
        assertEquals("JOHN DOE", record.getCardEmbossedName());
        assertEquals("2025-12-31", record.getCardExpirationDate());
        assertEquals("Y", record.getCardActiveStatus());
    }

    @Test
    void equalsAndHashCode() {
        CardRecord r1 = new CardRecord();
        r1.setCardNum("4111111111111111");
        r1.setCardAcctId(1L);

        CardRecord r2 = new CardRecord();
        r2.setCardNum("4111111111111111");
        r2.setCardAcctId(1L);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        r2.setCardNum("5222222222222222");
        assertNotEquals(r1, r2);
    }

    @Test
    void jsonSerialization() throws Exception {
        CardRecord record = new CardRecord();
        record.setCardNum("4111111111111111");
        record.setCardAcctId(99999999999L);
        record.setCardCvvCd(456);

        String json = mapper.writeValueAsString(record);
        assertTrue(json.contains("\"cardNum\":\"4111111111111111\""));

        CardRecord deserialized = mapper.readValue(json, CardRecord.class);
        assertEquals(record.getCardNum(), deserialized.getCardNum());
        assertEquals(record.getCardAcctId(), deserialized.getCardAcctId());
    }

    @Test
    void toStringContainsFields() {
        CardRecord record = new CardRecord();
        record.setCardNum("1234567890123456");
        assertTrue(record.toString().contains("1234567890123456"));
    }
}
