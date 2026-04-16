package com.cardemo.migration.reader;

import com.cardemo.migration.model.CopybookField;
import com.cardemo.migration.model.CopybookLayout;
import com.cardemo.migration.model.VsamFileDescriptor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Reads fixed-length records from EBCDIC-encoded VSAM data files.
 * Supports reading individual records by index, counting records,
 * and performing spot-check reads (first, last, and random records).
 */
public class EbcdicFileReader {

    private final Path dataDirectory;

    public EbcdicFileReader(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Counts the number of records in a VSAM file based on file size / record length.
     *
     * @param descriptor the VSAM file descriptor
     * @return the number of records
     * @throws IOException if the file cannot be read
     */
    public int countRecords(VsamFileDescriptor descriptor) throws IOException {
        Path filePath = dataDirectory.resolve(descriptor.fileName());
        long fileSize = Files.size(filePath);
        if (fileSize % descriptor.recordLength() != 0) {
            throw new IOException(String.format(
                    "File %s size %d is not evenly divisible by record length %d",
                    descriptor.fileName(), fileSize, descriptor.recordLength()));
        }
        return (int) (fileSize / descriptor.recordLength());
    }

    /**
     * Reads a single record by its zero-based index.
     *
     * @param descriptor  the VSAM file descriptor
     * @param recordIndex zero-based record index
     * @return raw bytes of the record
     * @throws IOException if the file cannot be read
     */
    public byte[] readRecord(VsamFileDescriptor descriptor, int recordIndex) throws IOException {
        Path filePath = dataDirectory.resolve(descriptor.fileName());
        byte[] record = new byte[descriptor.recordLength()];
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            long offset = (long) recordIndex * descriptor.recordLength();
            raf.seek(offset);
            int bytesRead = raf.read(record);
            if (bytesRead < descriptor.recordLength()) {
                throw new IOException(String.format(
                        "Incomplete record at index %d in file %s: expected %d bytes, got %d",
                        recordIndex, descriptor.fileName(), descriptor.recordLength(), bytesRead));
            }
        }
        return record;
    }

    /**
     * Reads a record and parses all fields using the given copybook layout.
     *
     * @param descriptor  the VSAM file descriptor
     * @param layout      the copybook layout definition
     * @param recordIndex zero-based record index
     * @return map of field name to parsed string value
     * @throws IOException if the file cannot be read
     */
    public Map<String, String> readParsedRecord(VsamFileDescriptor descriptor,
                                                 CopybookLayout layout,
                                                 int recordIndex) throws IOException {
        byte[] record = readRecord(descriptor, recordIndex);
        Map<String, String> parsedFields = new HashMap<>();
        for (CopybookField field : layout.dataFields()) {
            String value = EbcdicFieldParser.parseField(record, field);
            parsedFields.put(field.name(), value);
        }
        return parsedFields;
    }

    /**
     * Returns indices for spot-check validation: first, last, and N random records.
     *
     * @param totalRecords   the total number of records in the file
     * @param randomCount    number of random records to include
     * @return list of record indices to spot check
     */
    public List<Integer> getSpotCheckIndices(int totalRecords, int randomCount) {
        List<Integer> indices = new ArrayList<>();
        if (totalRecords == 0) {
            return indices;
        }

        indices.add(0); // first record
        if (totalRecords > 1) {
            indices.add(totalRecords - 1); // last record
        }

        if (totalRecords > 2) {
            Random random = new Random(42); // deterministic seed for reproducibility
            int actualRandom = Math.min(randomCount, totalRecords - 2);
            while (indices.size() < 2 + actualRandom) {
                int idx = 1 + random.nextInt(totalRecords - 2);
                if (!indices.contains(idx)) {
                    indices.add(idx);
                }
            }
        }

        return indices;
    }
}
