package com.carddemo.posttran.io;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A VSAM KSDS stand-in: records addressed by a fixed-length key, held in key order.
 *
 * <p>Kept as a file-backed keyed store rather than a database table on purpose. This step is being
 * proved against the legacy program byte for byte, so the migrated side has to be able to emit the
 * same unloaded image; swapping in a relational store is the next refactor, once parity is signed
 * off, and only {@code KeyedStore} has to change.
 */
public final class KeyedStore {

    private final int recordLength;
    private final int keyLength;
    private final TreeMap<String, String> records = new TreeMap<>();

    private KeyedStore(int recordLength, int keyLength) {
        this.recordLength = recordLength;
        this.keyLength = keyLength;
    }

    /** {@code OPEN INPUT} / {@code OPEN I-O} over an existing dataset. */
    public static KeyedStore load(Path path, int recordLength, int keyLength) {
        KeyedStore store = new KeyedStore(recordLength, keyLength);
        for (String record : FixedWidthFiles.read(path, recordLength)) {
            store.records.put(record.substring(0, keyLength), record);
        }
        return store;
    }

    /** {@code OPEN OUTPUT} — CBTRN02C.cbl:256 opens TRANFILE this way, so it starts empty. */
    public static KeyedStore empty(int recordLength, int keyLength) {
        return new KeyedStore(recordLength, keyLength);
    }

    /** {@code READ ... KEY IS}; empty means {@code INVALID KEY} (file status 23). */
    public Optional<String> read(String key) {
        return Optional.ofNullable(records.get(key));
    }

    /** {@code WRITE} / {@code REWRITE}. */
    public void put(String record) {
        if (record.length() != recordLength) {
            throw new IllegalArgumentException(
                    "record length " + record.length() + " != " + recordLength);
        }
        records.put(record.substring(0, keyLength), record);
    }

    public int size() {
        return records.size();
    }

    /** Unloads in key order, which is how the legacy side is captured for the parity diff. */
    public void save(Path path) {
        FixedWidthFiles.write(path, new ArrayList<>(records.values()));
    }

    public List<String> inKeyOrder() {
        return new ArrayList<>(records.values());
    }
}
