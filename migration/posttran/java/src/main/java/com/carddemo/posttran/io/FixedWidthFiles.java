package com.carddemo.posttran.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes RECFM=F datasets.
 *
 * <p>Output is always written without record delimiters, matching what the legacy step produces
 * for {@code ORGANIZATION IS SEQUENTIAL}. Input accepts either form, because the sample data
 * checked into app/data/ASCII is newline-terminated for readability while a real unload is not.
 *
 * <p>ISO-8859-1 is used throughout so that a byte is a character: these are already-translated
 * fixed-width datasets, and any lossy re-encoding would destroy the zoned-decimal sign bytes.
 */
public final class FixedWidthFiles {

    private FixedWidthFiles() {
    }

    public static List<String> read(Path path, int recordLength) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path, e);
        }
        String content = new String(bytes, StandardCharsets.ISO_8859_1);
        List<String> records = new ArrayList<>();

        boolean delimited = !content.isEmpty()
                && content.length() % (recordLength + 1) == 0
                && content.charAt(recordLength) == '\n';
        int stride = delimited ? recordLength + 1 : recordLength;
        if (content.length() % stride != 0) {
            throw new IllegalStateException(path + " is not a multiple of " + recordLength
                    + " bytes (length " + content.length() + ")");
        }
        for (int offset = 0; offset < content.length(); offset += stride) {
            records.add(content.substring(offset, offset + recordLength));
        }
        return records;
    }

    public static void write(Path path, List<String> records) {
        StringBuilder out = new StringBuilder();
        records.forEach(out::append);
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Files.write(path, out.toString().getBytes(StandardCharsets.ISO_8859_1));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + path, e);
        }
    }
}
