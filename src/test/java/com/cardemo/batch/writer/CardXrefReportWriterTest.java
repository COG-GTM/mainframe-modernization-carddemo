package com.cardemo.batch.writer;

import com.cardemo.batch.model.CardXrefRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardXrefReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteXrefRecordsToCsvWithHeaders() throws Exception {
        Path outputFile = tempDir.resolve("xref-report.csv");
        CardXrefReportWriter writer = new CardXrefReportWriter(
                new FileSystemResource(outputFile));
        writer.open(new ExecutionContext());

        CardXrefRecord record = new CardXrefRecord();
        record.setXrefCardNum("4111111111111111");
        record.setXrefCustId("000000001");
        record.setXrefAcctId("00000000001");

        writer.write(new Chunk<>(List.of(record)));
        writer.close();

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("XREF-CARD-NUM"));
        assertTrue(lines.get(1).contains("4111111111111111"));
    }
}
