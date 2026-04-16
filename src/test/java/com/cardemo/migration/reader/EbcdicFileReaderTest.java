package com.cardemo.migration.reader;

import com.cardemo.migration.model.CopybookLayout;
import com.cardemo.migration.model.VsamFileDescriptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EbcdicFileReader - reading records from fixed-length EBCDIC files.
 */
class EbcdicFileReaderTest {

    private static final Charset EBCDIC = Charset.forName("IBM037");

    @TempDir
    Path tempDir;

    private Path createTestFile(String fileName, int recordLength, int recordCount) throws IOException {
        byte[] data = new byte[recordLength * recordCount];
        for (int i = 0; i < recordCount; i++) {
            // Fill each record with a pattern based on record index
            String idStr = String.format("%011d", i + 1);
            byte[] idBytes = idStr.getBytes(EBCDIC);
            System.arraycopy(idBytes, 0, data, i * recordLength, Math.min(idBytes.length, recordLength));
            // Fill rest with EBCDIC spaces (0x40)
            for (int j = idBytes.length; j < recordLength; j++) {
                data[i * recordLength + j] = 0x40;
            }
        }
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, data);
        return filePath;
    }

    @Test
    @DisplayName("Count records correctly based on file size / record length")
    void countRecords() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.ACCTDATA.PS", 300, 50);
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        int count = reader.countRecords(VsamFileDescriptor.ACCTDATA);
        assertEquals(50, count);
    }

    @Test
    @DisplayName("Count records throws IOException when file size not divisible by record length")
    void countRecordsInvalidFileSize() throws IOException {
        // Create a file with 301 bytes (not divisible by 300)
        byte[] data = new byte[301];
        Files.write(tempDir.resolve("AWS.M2.CARDDEMO.ACCTDATA.PS"), data);
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        assertThrows(IOException.class, () -> reader.countRecords(VsamFileDescriptor.ACCTDATA));
    }

    @Test
    @DisplayName("Read individual record by index")
    void readRecord() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.ACCTDATA.PS", 300, 5);
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        byte[] record = reader.readRecord(VsamFileDescriptor.ACCTDATA, 0);
        assertEquals(300, record.length);
        // First 11 bytes should be "00000000001" in EBCDIC
        String id = new String(record, 0, 11, EBCDIC);
        assertEquals("00000000001", id);
    }

    @Test
    @DisplayName("Read second record returns correct data")
    void readSecondRecord() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.ACCTDATA.PS", 300, 5);
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        byte[] record = reader.readRecord(VsamFileDescriptor.ACCTDATA, 1);
        String id = new String(record, 0, 11, EBCDIC);
        assertEquals("00000000002", id);
    }

    @Test
    @DisplayName("Read last record returns correct data")
    void readLastRecord() throws IOException {
        createTestFile("AWS.M2.CARDDEMO.ACCTDATA.PS", 300, 5);
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        byte[] record = reader.readRecord(VsamFileDescriptor.ACCTDATA, 4);
        String id = new String(record, 0, 11, EBCDIC);
        assertEquals("00000000005", id);
    }

    @Test
    @DisplayName("Spot check indices include first, last, and random records")
    void getSpotCheckIndices() {
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        List<Integer> indices = reader.getSpotCheckIndices(100, 3);
        // Should include index 0 (first), 99 (last), and 3 random indices
        assertEquals(5, indices.size());
        assertTrue(indices.contains(0), "Should contain first record index");
        assertTrue(indices.contains(99), "Should contain last record index");
    }

    @Test
    @DisplayName("Spot check indices for single record file")
    void getSpotCheckIndicesSingleRecord() {
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        List<Integer> indices = reader.getSpotCheckIndices(1, 3);
        assertEquals(1, indices.size());
        assertEquals(0, indices.get(0));
    }

    @Test
    @DisplayName("Spot check indices for empty file")
    void getSpotCheckIndicesEmpty() {
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        List<Integer> indices = reader.getSpotCheckIndices(0, 3);
        assertTrue(indices.isEmpty());
    }

    @Test
    @DisplayName("Spot check indices for two-record file")
    void getSpotCheckIndicesTwoRecords() {
        EbcdicFileReader reader = new EbcdicFileReader(tempDir);
        List<Integer> indices = reader.getSpotCheckIndices(2, 3);
        assertEquals(2, indices.size());
        assertTrue(indices.contains(0));
        assertTrue(indices.contains(1));
    }
}
