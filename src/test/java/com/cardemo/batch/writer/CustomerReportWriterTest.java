package com.cardemo.batch.writer;

import com.cardemo.batch.model.CustomerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteCustomerRecordsToCsvWithHeaders() throws Exception {
        Path outputFile = tempDir.resolve("customer-report.csv");
        CustomerReportWriter writer = new CustomerReportWriter(
                new FileSystemResource(outputFile));
        writer.open(new ExecutionContext());

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

        writer.write(new Chunk<>(List.of(record)));
        writer.close();

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("CUST-ID"));
        assertTrue(lines.get(0).contains("CUST-FICO-CREDIT-SCORE"));
        assertTrue(lines.get(1).contains("000000001"));
        assertTrue(lines.get(1).contains("750"));
    }
}
