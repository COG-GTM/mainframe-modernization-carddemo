package com.cardemo.batch.reader;

import com.cardemo.batch.model.CardRecord;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class CardFileReaderTest {

    @Test
    void shouldReadCardRecordsFromCsv() throws Exception {
        FlatFileItemReader<CardRecord> reader = CardFileReader.create(
                new ClassPathResource("data/input/cardfile.csv"));
        reader.open(new ExecutionContext());

        CardRecord record = reader.read();
        assertNotNull(record);
        assertEquals("4111111111111111", record.getCardNum());
        assertEquals("00000000001", record.getCardAcctId());
        assertEquals("123", record.getCardCvvCd());
        assertEquals("JOHN DOE", record.getCardEmbossedName());
        assertEquals("Y", record.getCardActiveStatus());

        reader.close();
    }

    @Test
    void shouldReturnNullAtEndOfFile() throws Exception {
        FlatFileItemReader<CardRecord> reader = CardFileReader.create(
                new ClassPathResource("data/input/cardfile.csv"));
        reader.open(new ExecutionContext());

        reader.read();
        reader.read();
        reader.read();
        CardRecord eof = reader.read();
        assertNull(eof);

        reader.close();
    }
}
