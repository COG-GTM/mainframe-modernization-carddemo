package com.carddemo.batch.core.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.carddemo.batch.core.FixedWidth;
import com.carddemo.batch.core.record.TranTypeRecord;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SequentialFileTest {

    @Test
    void writesLineSequentialRecordsAndReadsThemBack(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("nested/trantype.txt");
        try (SequentialFileWriter<TranTypeRecord> writer = SequentialFileWriter.of(file, TranTypeRecord.LENGTH)) {
            assertEquals(FileStatus.OK, writer.write(new TranTypeRecord("01", "Purchase", "")));
            writer.write(new TranTypeRecord("02", "Payment", ""));
            assertEquals(2, writer.recordCount());
        }
        assertEquals(2 * 61, Files.size(file));
        try (SequentialFileReader<TranTypeRecord> reader =
                new SequentialFileReader<>(file, TranTypeRecord.LENGTH, TranTypeRecord::fromBytes)) {
            assertEquals("01", reader.read().record().tranType());
            assertEquals("02", reader.read().record().tranType());
            assertEquals(FileStatus.END_OF_FILE, reader.read().status());
            assertEquals(FileStatus.END_OF_FILE, reader.read().status());
        }
    }

    @Test
    void padsShortLinesAndHandlesCrlfAndMissingFinalNewline(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("short.txt");
        Files.write(file, FixedWidth.encode("AB\r\nCDEFGH\nXY"));
        List<byte[]> records = SequentialFileReader.readAll(file, 4, Function.identity());
        assertEquals(3, records.size());
        assertArrayEquals(FixedWidth.encode("AB  "), records.get(0));
        assertArrayEquals(FixedWidth.encode("CDEF"), records.get(1));
        assertArrayEquals(FixedWidth.encode("XY  "), records.get(2));
    }

    @Test
    void fixedFormatHasNoDelimiters(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("fixed.dat");
        try (SequentialFileWriter<byte[]> writer =
                new SequentialFileWriter<>(file, 3, Function.identity(), RecordFormat.FIXED)) {
            writer.write(FixedWidth.encode("abc"));
            writer.write(FixedWidth.encode("def"));
        }
        assertEquals("abcdef", Files.readString(file));
        try (SequentialFileReader<byte[]> reader =
                new SequentialFileReader<>(file, 3, Function.identity(), RecordFormat.FIXED)) {
            assertArrayEquals(FixedWidth.encode("abc"), reader.read().record());
            assertArrayEquals(FixedWidth.encode("def"), reader.read().record());
            assertEquals(FileStatus.END_OF_FILE, reader.read().status());
        }
    }

    @Test
    void rejectsWrongRecordLength(@TempDir Path dir) {
        try (SequentialFileWriter<byte[]> writer = new SequentialFileWriter<>(dir.resolve("x"), 3, Function.identity())) {
            assertThrows(IllegalArgumentException.class, () -> writer.write(new byte[2]));
        }
    }
}
