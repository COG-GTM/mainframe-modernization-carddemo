package com.cardemo.batch.reader;

import com.cardemo.batch.model.CardXrefRecord;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class CardXrefFileReaderTest {

    @Test
    void shouldReadXrefRecordsFromCsv() throws Exception {
        FlatFileItemReader<CardXrefRecord> reader = CardXrefFileReader.create(
                new ClassPathResource("data/input/xreffile.csv"));
        reader.open(new ExecutionContext());

        CardXrefRecord record = reader.read();
        assertNotNull(record);
        assertEquals("4111111111111111", record.getXrefCardNum());
        assertEquals("000000001", record.getXrefCustId());
        assertEquals("00000000001", record.getXrefAcctId());

        reader.close();
    }

    @Test
    void shouldReturnNullAtEndOfFile() throws Exception {
        FlatFileItemReader<CardXrefRecord> reader = CardXrefFileReader.create(
                new ClassPathResource("data/input/xreffile.csv"));
        reader.open(new ExecutionContext());

        reader.read();
        reader.read();
        reader.read();
        CardXrefRecord eof = reader.read();
        assertNull(eof);

        reader.close();
    }
}
