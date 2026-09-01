package com.carddemo.posttran;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Minimal COBOL file-handling equivalents for the six SELECTs of
 * app/cbl/CBTRN02C.cbl:29-61. Keyed files are loaded into a key-ordered map, which reproduces
 * VSAM KSDS RANDOM access (READ/WRITE/REWRITE by primary key) and lets the after-image be written
 * back in key sequence, as an IDCAMS REPRO unload would produce.
 */
public final class Files {

    private Files() {
    }

    /** ORGANIZATION SEQUENTIAL, ACCESS SEQUENTIAL, OPEN INPUT — fixed-length records. */
    public static final class SeqInput implements AutoCloseable {
        private final InputStream in;
        private final int recLen;

        public SeqInput(Path path, int recLen) throws IOException {
            this.in = java.nio.file.Files.newInputStream(path);
            this.recLen = recLen;
        }

        /** Returns the next record, or null at end of file (COBOL status '10'). */
        public byte[] read() throws IOException {
            byte[] buf = in.readNBytes(recLen);
            if (buf.length == 0) {
                return null;
            }
            if (buf.length < recLen) {
                buf = Arrays.copyOf(buf, recLen);
                Arrays.fill(buf, buf.length, recLen, (byte) ' ');
            }
            return buf;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    /** ORGANIZATION SEQUENTIAL, OPEN OUTPUT. */
    public static final class SeqOutput implements AutoCloseable {
        private final OutputStream out;

        public SeqOutput(Path path) throws IOException {
            this.out = java.nio.file.Files.newOutputStream(path);
        }

        public void write(byte[] rec) throws IOException {
            out.write(rec);
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    /** ORGANIZATION INDEXED, ACCESS RANDOM. */
    public static final class KeyedFile {
        private final Map<String, byte[]> records;
        private final int recLen;
        private final int keyLen;

        private KeyedFile(Map<String, byte[]> records, int recLen, int keyLen) {
            this.records = records;
            this.recLen = recLen;
            this.keyLen = keyLen;
        }

        /** OPEN I-O against an existing unloaded KSDS image. */
        public static KeyedFile open(Path path, int recLen, int keyLen) throws IOException {
            Map<String, byte[]> map = new TreeMap<>();
            byte[] all = java.nio.file.Files.readAllBytes(path);
            if (all.length % recLen != 0) {
                throw new IOException(path + ": length " + all.length + " is not a multiple of " + recLen);
            }
            for (int off = 0; off < all.length; off += recLen) {
                byte[] rec = Arrays.copyOfRange(all, off, off + recLen);
                map.put(new String(rec, 0, keyLen, StandardCharsets.ISO_8859_1), rec);
            }
            return new KeyedFile(map, recLen, keyLen);
        }

        /** OPEN OUTPUT — an empty cluster; insertion order is irrelevant, output is key-ordered. */
        public static KeyedFile empty(int recLen, int keyLen) {
            return new KeyedFile(new TreeMap<>(), recLen, keyLen);
        }

        /** READ ... INVALID KEY: returns null when the key is absent (status '23'). */
        public byte[] read(String key) {
            byte[] rec = records.get(key);
            return rec == null ? null : Arrays.copyOf(rec, rec.length);
        }

        public boolean write(byte[] rec) {
            String key = new String(rec, 0, keyLen, StandardCharsets.ISO_8859_1);
            if (records.containsKey(key)) {
                return false; // duplicate key, status '22'
            }
            records.put(key, Arrays.copyOf(rec, recLen));
            return true;
        }

        /** REWRITE ... INVALID KEY: false when the key is absent. */
        public boolean rewrite(byte[] rec) {
            String key = new String(rec, 0, keyLen, StandardCharsets.ISO_8859_1);
            if (!records.containsKey(key)) {
                return false;
            }
            records.put(key, Arrays.copyOf(rec, recLen));
            return true;
        }

        public int size() {
            return records.size();
        }

        public Map<String, byte[]> snapshot() {
            return new LinkedHashMap<>(records);
        }

        /** Writes the cluster out in key sequence (the after-image used for parity diffs). */
        public void unload(Path path) throws IOException {
            try (OutputStream out = java.nio.file.Files.newOutputStream(path)) {
                for (byte[] rec : records.values()) {
                    out.write(rec);
                }
            }
        }
    }
}
