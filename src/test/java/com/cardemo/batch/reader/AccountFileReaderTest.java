package com.cardemo.batch.reader;

import com.cardemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class AccountFileReaderTest {

    @Test
    void shouldReadAccountRecordsFromCsv() throws Exception {
        FlatFileItemReader<AccountRecord> reader = AccountFileReader.create(
                new ClassPathResource("data/input/acctfile.csv"));
        reader.open(new ExecutionContext());

        AccountRecord record = reader.read();
        assertNotNull(record);
        assertEquals("00000000001", record.getAcctId());
        assertEquals("Y", record.getAcctActiveStatus());

        reader.close();
    }

    @Test
    void shouldReadAllElevenFieldsForAccountRecord() throws Exception {
        FlatFileItemReader<AccountRecord> reader = AccountFileReader.create(
                new ClassPathResource("data/input/acctfile.csv"));
        reader.open(new ExecutionContext());

        AccountRecord record = reader.read();
        assertNotNull(record);
        assertNotNull(record.getAcctId());
        assertNotNull(record.getAcctActiveStatus());
        assertNotNull(record.getAcctCurrBal());
        assertNotNull(record.getAcctCreditLimit());
        assertNotNull(record.getAcctCashCreditLimit());
        assertNotNull(record.getAcctOpenDate());
        assertNotNull(record.getAcctExpiraionDate());
        assertNotNull(record.getAcctReissueDate());
        assertNotNull(record.getAcctCurrCycCredit());
        assertNotNull(record.getAcctCurrCycDebit());
        assertNotNull(record.getAcctGroupId());

        reader.close();
    }

    @Test
    void shouldReturnNullAtEndOfFile() throws Exception {
        FlatFileItemReader<AccountRecord> reader = AccountFileReader.create(
                new ClassPathResource("data/input/acctfile.csv"));
        reader.open(new ExecutionContext());

        reader.read(); // record 1
        reader.read(); // record 2
        reader.read(); // record 3
        AccountRecord eof = reader.read();
        assertNull(eof);

        reader.close();
    }
}
