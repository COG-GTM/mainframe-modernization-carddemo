package com.carddemo.batch.core.io;

import com.carddemo.batch.core.FixedRecord;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/** Sequential writer of fixed-length records ({@code OPEN OUTPUT} / {@code WRITE}). */
public final class SequentialFileWriter<T> implements AutoCloseable {

    private final OutputStream out;
    private final int recordLength;
    private final Function<T, byte[]> encoder;
    private final RecordFormat format;
    private long recordCount;

    public SequentialFileWriter(Path path, int recordLength, Function<T, byte[]> encoder) {
        this(path, recordLength, encoder, RecordFormat.LINE_SEQUENTIAL);
    }

    public SequentialFileWriter(Path path, int recordLength, Function<T, byte[]> encoder, RecordFormat format) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            this.out = new BufferedOutputStream(Files.newOutputStream(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open " + path + " for output", e);
        }
        this.recordLength = recordLength;
        this.encoder = encoder;
        this.format = format;
    }

    /** Writer for {@link FixedRecord}s using their {@code toBytes()} image. */
    public static <T extends FixedRecord> SequentialFileWriter<T> of(Path path, int recordLength) {
        return new SequentialFileWriter<>(path, recordLength, FixedRecord::toBytes);
    }

    /** Writes one record and returns status '00'. Records must be exactly the record length. */
    public String write(T record) {
        byte[] bytes = encoder.apply(record);
        if (bytes.length != recordLength) {
            throw new IllegalArgumentException(
                    "Record is " + bytes.length + " bytes, expected " + recordLength);
        }
        try {
            out.write(bytes);
            if (format == RecordFormat.LINE_SEQUENTIAL) {
                out.write('\n');
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        recordCount++;
        return FileStatus.OK;
    }

    public long recordCount() {
        return recordCount;
    }

    public static <T> void writeAll(Path path, int recordLength, Function<T, byte[]> encoder, Iterable<T> records) {
        try (SequentialFileWriter<T> writer = new SequentialFileWriter<>(path, recordLength, encoder)) {
            records.forEach(writer::write);
        }
    }

    @Override
    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
