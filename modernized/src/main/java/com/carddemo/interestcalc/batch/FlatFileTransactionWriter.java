package com.carddemo.interestcalc.batch;

import com.carddemo.interestcalc.domain.TransactionRecord;
import com.carddemo.interestcalc.repository.TransactionWriter;
import com.carddemo.interestcalc.util.ZonedDecimal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes interest transactions as fixed-width 350-byte records matching the CVTRA05Y
 * ({@code TRAN-RECORD}) layout, mirroring the sequential OUTPUT {@code TRANSACT-FILE}.
 */
public class FlatFileTransactionWriter implements TransactionWriter, AutoCloseable {

    private final BufferedWriter writer;

    public FlatFileTransactionWriter(Path outputFile) {
        try {
            this.writer = Files.newBufferedWriter(outputFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed opening transaction output file " + outputFile, e);
        }
    }

    @Override
    public void write(TransactionRecord t) {
        StringBuilder rec = new StringBuilder(350);
        rec.append(padRight(t.transactionId(), 16));              // TRAN-ID            X(16)
        rec.append(padRight(t.typeCode(), 2));                    // TRAN-TYPE-CD       X(02)
        rec.append(padDigits(t.categoryCode(), 4));               // TRAN-CAT-CD        9(04)
        rec.append(padRight(t.source(), 10));                     // TRAN-SOURCE        X(10)
        rec.append(padRight(t.description(), 100));               // TRAN-DESC          X(100)
        rec.append(ZonedDecimal.format(t.amount(), 11, 2));       // TRAN-AMT           S9(09)V99
        rec.append(padDigits(Long.toString(t.merchantId()), 9));  // TRAN-MERCHANT-ID   9(09)
        rec.append(padRight(t.merchantName(), 50));               // TRAN-MERCHANT-NAME X(50)
        rec.append(padRight(t.merchantCity(), 50));               // TRAN-MERCHANT-CITY X(50)
        rec.append(padRight(t.merchantZip(), 10));                // TRAN-MERCHANT-ZIP  X(10)
        rec.append(padRight(t.cardNumber(), 16));                 // TRAN-CARD-NUM      X(16)
        rec.append(padRight(t.originTimestamp(), 26));            // TRAN-ORIG-TS       X(26)
        rec.append(padRight(t.processTimestamp(), 26));           // TRAN-PROC-TS       X(26)
        rec.append(" ".repeat(20));                               // FILLER             X(20)
        try {
            writer.write(rec.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed writing transaction record", e);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed closing transaction output file", e);
        }
    }

    private static String padRight(String value, int length) {
        if (value.length() > length) {
            return value.substring(0, length);
        }
        return value + " ".repeat(length - value.length());
    }

    private static String padDigits(String value, int length) {
        return "0".repeat(Math.max(0, length - value.length())) + value;
    }
}
