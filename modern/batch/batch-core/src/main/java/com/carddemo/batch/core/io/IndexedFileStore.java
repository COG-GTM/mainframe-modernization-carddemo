package com.carddemo.batch.core.io;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * In-memory emulation of a VSAM KSDS keyed by {@code K}, backed by a {@link TreeMap}.
 *
 * <p>Supports random READ/WRITE/REWRITE/DELETE, START plus sequential READ NEXT in key order, and
 * reads by an alternate key (AIX). Operations return COBOL file status codes: '00' success,
 * '10' end of file, '22' duplicate key, '23' record not found. Keys built from the record's key
 * bytes (e.g. {@code TranCatBalRecord.key()}) sort the way the KSDS does for the ASCII data set.
 */
public final class IndexedFileStore<K extends Comparable<? super K>, V> {

    private final TreeMap<K, V> records = new TreeMap<>();
    private final Function<V, K> keyFunction;
    private K position;
    private boolean positionInclusive = true;

    public IndexedFileStore(Function<V, K> keyFunction) {
        this.keyFunction = Objects.requireNonNull(keyFunction);
    }

    /** Builds a store from records; duplicate keys are rejected like a VSAM REPRO load. */
    public static <K extends Comparable<? super K>, V> IndexedFileStore<K, V> of(
            Collection<V> initial, Function<V, K> keyFunction) {
        IndexedFileStore<K, V> store = new IndexedFileStore<>(keyFunction);
        for (V record : initial) {
            if (!store.write(record).isOk()) {
                throw new IllegalStateException("Duplicate key " + keyFunction.apply(record));
            }
        }
        return store;
    }

    /** Loads a store from a line-sequential flat file of fixed-length records. */
    public static <K extends Comparable<? super K>, V> IndexedFileStore<K, V> load(
            Path path, int recordLength, Function<byte[], V> decoder, Function<V, K> keyFunction) {
        return of(SequentialFileReader.readAll(path, recordLength, decoder), keyFunction);
    }

    /** Random READ by primary key: '00' or '23'. A successful read positions READ NEXT after it. */
    public IoResult<V> read(K key) {
        V record = records.get(key);
        if (record == null) {
            return IoResult.notFound();
        }
        position = key;
        positionInclusive = false;
        return IoResult.ok(record);
    }

    /**
     * Random READ by an alternate key: the first record in primary-key order whose alternate key
     * equals {@code alternateKey}; '00' or '23'.
     */
    public <A> IoResult<V> readByAlternateKey(Function<V, A> alternateKeyFunction, A alternateKey) {
        for (V record : records.values()) {
            if (Objects.equals(alternateKeyFunction.apply(record), alternateKey)) {
                return IoResult.ok(record);
            }
        }
        return IoResult.notFound();
    }

    /** WRITE: '00', or '22' if the key already exists. */
    public IoResult<V> write(V record) {
        K key = keyFunction.apply(record);
        if (records.containsKey(key)) {
            return IoResult.duplicateKey();
        }
        records.put(key, record);
        return IoResult.ok(record);
    }

    /** REWRITE: '00', or '23' if no record has this key. */
    public IoResult<V> rewrite(V record) {
        K key = keyFunction.apply(record);
        if (!records.containsKey(key)) {
            return IoResult.notFound();
        }
        records.put(key, record);
        return IoResult.ok(record);
    }

    /** DELETE: '00', or '23' if no record has this key. */
    public IoResult<V> delete(K key) {
        V removed = records.remove(key);
        return removed == null ? IoResult.notFound() : IoResult.ok(removed);
    }

    /** START KEY NOT LESS THAN {@code key}: '00', or '23' if no such record exists. */
    public String start(K key) {
        if (records.ceilingKey(key) == null) {
            return FileStatus.NOT_FOUND;
        }
        position = key;
        positionInclusive = true;
        return FileStatus.OK;
    }

    /** Positions READ NEXT at the first record (the state after OPEN). */
    public void rewind() {
        position = null;
        positionInclusive = true;
    }

    /** Sequential READ NEXT in key order: '00', or '10' past the last record. */
    public IoResult<V> readNext() {
        Map.Entry<K, V> entry;
        if (position == null) {
            entry = records.firstEntry();
        } else {
            entry = positionInclusive ? records.ceilingEntry(position) : records.higherEntry(position);
        }
        if (entry == null) {
            return IoResult.endOfFile();
        }
        position = entry.getKey();
        positionInclusive = false;
        return IoResult.ok(entry.getValue());
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(records.get(key));
    }

    public boolean containsKey(K key) {
        return records.containsKey(key);
    }

    public int size() {
        return records.size();
    }

    /** Snapshot of all records in key order. */
    public List<V> values() {
        return new ArrayList<>(records.values());
    }

    /** Unloads the store, in key order, to a line-sequential flat file. */
    public void save(Path path, int recordLength, Function<V, byte[]> encoder) {
        SequentialFileWriter.writeAll(path, recordLength, encoder, records.values());
    }
}
