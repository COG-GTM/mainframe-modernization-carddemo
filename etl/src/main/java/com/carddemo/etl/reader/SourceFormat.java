package com.carddemo.etl.reader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The physical format of an input Account dataset.
 *
 * <p>Both formats yield identical record characters once decoded, so downstream field parsing is
 * shared. They differ only in character set and record framing:
 *
 * <ul>
 *   <li>{@link #ASCII} — the {@code app/data/ASCII/acctdata.txt} seed file: newline-delimited
 *       lines, one 300-character record per line, ISO-8859-1 bytes.</li>
 *   <li>{@link #EBCDIC} — the {@code app/data/EBCDIC/*.ACCTDATA.PS} VSAM unload: contiguous
 *       fixed-length 300-byte records with no delimiters, IBM Cp037 (EBCDIC) bytes. The zoned
 *       sign-overpunch bytes decode to the same {@code '{' '}' 'A'..'R'} characters as the ASCII
 *       seed.</li>
 * </ul>
 */
public enum SourceFormat {

    ASCII(StandardCharsets.ISO_8859_1, Framing.LINE_DELIMITED),
    EBCDIC(Charset.forName("Cp037"), Framing.FIXED_LENGTH);

    /** How records are separated within the byte stream. */
    public enum Framing {
        /** Records separated by line terminators (one record per line). */
        LINE_DELIMITED,
        /** Records packed back-to-back, each exactly the record length in bytes. */
        FIXED_LENGTH
    }

    private final Charset charset;
    private final Framing framing;

    SourceFormat(Charset charset, Framing framing) {
        this.charset = charset;
        this.framing = framing;
    }

    public Charset charset() {
        return charset;
    }

    public Framing framing() {
        return framing;
    }
}
