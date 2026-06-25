package com.carddemo.etl.reader;

import com.carddemo.etl.model.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads Account records from an input stream and decodes each into an {@link Account}.
 *
 * <p>Framing and character set are driven by the {@link SourceFormat}: line-delimited ISO-8859-1
 * for the ASCII seed file, and contiguous fixed-length {@code Cp037} (EBCDIC) blocks for the VSAM
 * unload. Records are produced lazily so large unloads stream without being fully buffered.
 */
public final class AccountRecordReader {

    private final SourceFormat format;
    private final int recordLength;

    public AccountRecordReader(SourceFormat format) {
        this(format, AccountRecordParser.RECORD_LENGTH);
    }

    public AccountRecordReader(SourceFormat format, int recordLength) {
        this.format = format;
        this.recordLength = recordLength;
    }

    /**
     * Streams decoded {@link Account} records from the supplied input stream. The stream MUST be
     * closed by the caller (use try-with-resources); closing it closes the underlying input.
     *
     * <p>A malformed record aborts the stream with a {@link RecordParseException}. Callers that
     * need to tolerate and count bad records should consume {@link #rawRecords(InputStream)} and
     * parse each record defensively.
     */
    public Stream<Account> read(InputStream in) {
        return rawRecords(in).map(AccountRecordParser::parse);
    }

    /**
     * Streams the undecoded, framed record strings from the supplied input stream. The stream MUST
     * be closed by the caller; closing it closes the underlying input.
     */
    public Stream<String> rawRecords(InputStream in) {
        Iterator<String> rawRecords = format.framing() == SourceFormat.Framing.LINE_DELIMITED
                ? lineIterator(in)
                : fixedLengthIterator(in);

        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(rawRecords, Spliterator.ORDERED | Spliterator.NONNULL),
                        false)
                .onClose(() -> close(in));
    }

    private Iterator<String> lineIterator(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, format.charset()));
        return new Iterator<>() {
            private String next = advance();

            private String advance() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isEmpty()) {
                            return line;
                        }
                    }
                    return null;
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed reading line-delimited records", e);
                }
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public String next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                String current = next;
                next = advance();
                return current;
            }
        };
    }

    private Iterator<String> fixedLengthIterator(InputStream in) {
        return new Iterator<>() {
            private final byte[] buffer = new byte[recordLength];
            private String next = advance();

            private String advance() {
                try {
                    int read = in.readNBytes(buffer, 0, recordLength);
                    if (read == 0) {
                        return null;
                    }
                    if (read < recordLength) {
                        throw new RecordParseException(
                                "Trailing partial record: read " + read + " of " + recordLength + " bytes");
                    }
                    return new String(buffer, 0, recordLength, format.charset());
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed reading fixed-length records", e);
                }
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public String next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                String current = next;
                next = advance();
                return current;
            }
        };
    }

    private static void close(InputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed closing input stream", e);
        }
    }
}
