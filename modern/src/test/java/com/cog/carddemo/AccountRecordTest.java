package com.cog.carddemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AccountRecordTest {

    @Test
    void parsesTheFirstSampleRecordFieldByField() throws IOException {
        String raw = AccountFileReader.readRawRecords(SampleData.accountFile()).get(0);

        AccountRecord account = AccountRecord.parse(raw);

        assertEquals("00000000001", account.acctId());
        assertEquals("Y", account.activeStatus());
        assertEquals(new BigDecimal("194.00"), account.currBal());
        assertEquals(new BigDecimal("2020.00"), account.creditLimit());
        assertEquals(new BigDecimal("1020.00"), account.cashCreditLimit());
        assertEquals("2014-11-20", account.openDate());
        assertEquals("2025-05-20", account.expirationDate());
        assertEquals("2025-05-20", account.reissueDate());
        assertEquals(new BigDecimal("0.00"), account.currCycCredit());
        assertEquals(new BigDecimal("0.00"), account.currCycDebit());
        assertEquals("A000000000", account.addrZip());
        assertEquals("          ", account.groupId());
    }

    @Test
    void serializationOnlyNormalizesThePositiveSignOverpunch() throws IOException {
        for (String raw : AccountFileReader.readRawRecords(SampleData.accountFile())) {
            String serialized = AccountRecord.parse(raw).toRawRecord();

            assertEquals(AccountRecord.LENGTH, serialized.length());
            assertEquals(raw.replace('{', '0'), serialized);
        }
    }

    @Test
    void readsEverySampleAccountInKeyOrder() throws IOException {
        List<AccountRecord> accounts = AccountFileReader.readAll(SampleData.accountFile());

        assertEquals(50, accounts.size());
        for (int i = 0; i < accounts.size(); i++) {
            assertEquals(String.format("%011d", i + 1), accounts.get(i).acctId());
        }
    }

    @Test
    void rejectsRecordsOfTheWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> AccountRecord.parse("00000000001Y"));
    }
}
