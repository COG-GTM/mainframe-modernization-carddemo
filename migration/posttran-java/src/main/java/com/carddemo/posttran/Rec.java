package com.carddemo.posttran;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A fixed-length record area, the Java stand-in for a COBOL 01 level. Working-storage areas are
 * long-lived objects so that fields left untouched by a MOVE keep their previous bytes, matching
 * COBOL semantics (this matters for the FILLER of TRAN-CAT-BAL-RECORD, which
 * app/cbl/CBTRN02C.cbl:504 INITIALIZE does not clear).
 */
public final class Rec {

    private final byte[] bytes;

    public Rec(int len) {
        this.bytes = new byte[len];
        Arrays.fill(this.bytes, (byte) ' ');
    }

    public Rec(byte[] initial) {
        this.bytes = initial;
    }

    public byte[] bytes() {
        return bytes;
    }

    public int length() {
        return bytes.length;
    }

    public Rec copy() {
        return new Rec(Arrays.copyOf(bytes, bytes.length));
    }

    public void replace(byte[] src) {
        System.arraycopy(src, 0, bytes, 0, Math.min(src.length, bytes.length));
    }

    public String str(int off, int len) {
        return new String(bytes, off, len, StandardCharsets.ISO_8859_1);
    }

    public void setStr(int off, int len, String value) {
        byte[] v = value.getBytes(StandardCharsets.ISO_8859_1);
        for (int i = 0; i < len; i++) {
            bytes[off + i] = i < v.length ? v[i] : (byte) ' ';
        }
    }

    /** MOVE of an alphanumeric field to another of the same length. */
    public void moveAlpha(int off, int len, Rec src, int srcOff) {
        System.arraycopy(src.bytes, srcOff, bytes, off, len);
    }

    public void fill(int off, int len, char c) {
        Arrays.fill(bytes, off, off + len, (byte) c);
    }

    public BigDecimal signed(int off, int len, int scale) {
        return Zoned.readSigned(bytes, off, len, scale);
    }

    public BigDecimal unsigned(int off, int len, int scale) {
        return Zoned.readUnsigned(bytes, off, len, scale);
    }

    public void setSigned(int off, int len, int scale, BigDecimal v) {
        Zoned.writeSigned(bytes, off, len, scale, v);
    }

    public void setUnsigned(int off, int len, int scale, BigDecimal v) {
        Zoned.writeUnsigned(bytes, off, len, scale, v);
    }
}
