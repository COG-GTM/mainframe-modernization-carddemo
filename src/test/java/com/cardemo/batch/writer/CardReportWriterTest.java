package com.cardemo.batch.writer;

import com.cardemo.batch.model.CardRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteCardRecordsToCsvWithHeaders() throws Exception {
        Path outputFile = tempDir.resolve("card-report.csv");
        CardReportWriter writer = new CardReportWriter(
                new FileSystemResource(outputFile));
        writer.open(new ExecutionContext());

        CardRecord record = new CardRecord();
        record.setCardNum("4111111111111111");
        record.setCardAcctId("00000000001");
        record.setCardCvvCd("123");
        record.setCardEmbossedName("JOHN DOE");
        record.setCardExpiraionDate("2025-12-31");
        record.setCardActiveStatus("Y");

        writer.write(new Chunk<>(List.of(record)));
        writer.close();

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("CARD-NUM"));
        assertTrue(lines.get(1).contains("4111111111111111"));
    }
}
