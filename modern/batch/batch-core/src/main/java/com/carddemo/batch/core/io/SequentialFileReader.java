package com.carddemo.batch.core.io;

import com.carddemo.batch.core.FixedWidth;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Sequential reader of fixed-length records ({@code OPEN INPUT} / {@code READ ... AT END}). */
public final class SequentialFileReader<T> implements AutoCloseable {

    private final InputStream in;
    private final int recordLength;
    private final Function<byte[], T> decoder;
    private final RecordFormat format;
    private boolean endOfFile;

    public SequentialFileReader(Path path, int recordLength, Function<byte[], T> decoder) {
        this(path, recordLength, decoder, RecordFormat.LINE_SEQUENTIAL);
    }

    public SequentialFileReader(Path path, int recordLength, Function<byte[], T> decoder, RecordFormat format) {
        try {
            this.in = new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open " + path, e);
        }
        this.recordLength = recordLength;
        this.decoder = decoder;
        this.format = format;
    }

    /** Reads the next record: status '00' with the record, or '10' at end of file. */
    public IoResult<T> read() {
        if (endOfFile) {
            return IoResult.endOfFile();
        }
        byte[] raw;
        try {
            raw = format == RecordFormat.LINE_SEQUENTIAL ? readLine() : readFixed();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (raw == null) {
            endOfFile = true;
            return IoResult.endOfFile();
        }
        return IoResult.ok(decoder.apply(FixedWidth.padRecord(raw, recordLength)));
    }

    public static <T> List<T> readAll(Path path, int recordLength, Function<byte[], T> decoder) {
        List<T> records = new ArrayList<>();
        try (SequentialFileReader<T> reader = new SequentialFileReader<>(path, recordLength, decoder)) {
            for (IoResult<T> r = reader.read(); r.isOk(); r = reader.read()) {
                records.add(r.record());
            }
        }
        return records;
    }

    private byte[] readLine() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream(recordLength + 2);
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                return stripCarriageReturn(line.toByteArray());
            }
            line.write(b);
        }
        return line.size() == 0 ? null : stripCarriageReturn(line.toByteArray());
    }

    private static byte[] stripCarriageReturn(byte[] line) {
        if (line.length > 0 && line[line.length - 1] == '\r') {
            byte[] trimmed = new byte[line.length - 1];
            System.arraycopy(line, 0, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return line;
    }

    private byte[] readFixed() throws IOException {
        byte[] record = in.readNBytes(recordLength);
        return record.length == 0 ? null : record;
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
