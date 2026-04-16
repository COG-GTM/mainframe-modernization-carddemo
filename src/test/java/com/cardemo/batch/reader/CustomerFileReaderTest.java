package com.cardemo.batch.reader;

import com.cardemo.batch.model.CustomerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class CustomerFileReaderTest {

    @Test
    void shouldReadCustomerRecordsFromCsv() throws Exception {
        FlatFileItemReader<CustomerRecord> reader = CustomerFileReader.create(
                new ClassPathResource("data/input/custfile.csv"));
        reader.open(new ExecutionContext());

        CustomerRecord record = reader.read();
        assertNotNull(record);
        assertEquals("000000001", record.getCustId());
        assertEquals("John", record.getCustFirstName());
        assertEquals("A", record.getCustMiddleName());
        assertEquals("Doe", record.getCustLastName());
        assertEquals("750", record.getCustFicoCreditScore());

        reader.close();
    }

    @Test
    void shouldReturnNullAtEndOfFile() throws Exception {
        FlatFileItemReader<CustomerRecord> reader = CustomerFileReader.create(
                new ClassPathResource("data/input/custfile.csv"));
        reader.open(new ExecutionContext());

        reader.read();
        reader.read();
        reader.read();
        CustomerRecord eof = reader.read();
        assertNull(eof);

        reader.close();
    }
}
