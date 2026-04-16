package com.carddemo.batch.batch;

import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.RejectedTransaction;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Writes rejected transactions to a sequential flat file.
 * Each line = 350-byte original transaction data + 4-digit reason code + 76-char reason description
 * = 430 bytes total per the COBOL DALYREJS-FILE layout.
 *
 * Supports being opened, closed, and re-opened for successive job runs.
 */
public class RejectWriter implements Closeable {

    private static final DateTimeFormatter DB2_TS_FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd-HH.mm.ss.")
            .appendValue(ChronoField.MICRO_OF_SECOND, 6)
            .toFormatter();

    private final String outputPath;
    private BufferedWriter writer;
    private final AtomicInteger rejectCount = new AtomicInteger(0);

    public RejectWriter(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Opens (or re-opens) the output file for writing.
     * Resets the reject count.
     */
    public void open() throws IOException {
        if (writer != null) {
            writer.close();
        }
        writer = new BufferedWriter(new FileWriter(outputPath));
        rejectCount.set(0);
    }

    public void write(RejectedTransaction rejection) throws IOException {
        if (writer == null) {
            open();
        }
        String line = formatTransactionData(rejection.getOriginalTransaction())
                + formatReasonCode(rejection.getReasonCode())
                + padRight(rejection.getReasonDescription(), 76);
        writer.write(line);
        writer.newLine();
        rejectCount.incrementAndGet();
    }

    public int getRejectCount() {
        return rejectCount.get();
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    /**
     * Formats the DailyTransaction back into a 350-byte fixed-width string
     * matching the CVTRA06Y layout.
     */
    private String formatTransactionData(DailyTransaction txn) {
        StringBuilder sb = new StringBuilder(350);
        sb.append(padRight(txn.getTransactionId(), 16));
        sb.append(padRight(txn.getTypeCode(), 2));
        sb.append(padLeft(String.valueOf(txn.getCategoryCode()), 4, '0'));
        sb.append(padRight(txn.getSource(), 10));
        sb.append(padRight(txn.getDescription(), 100));
        sb.append(formatCobolAmount(txn.getAmount(), 11));
        sb.append(padLeft(String.valueOf(txn.getMerchantId()), 9, '0'));
        sb.append(padRight(txn.getMerchantName(), 50));
        sb.append(padRight(txn.getMerchantCity(), 50));
        sb.append(padRight(txn.getMerchantZip(), 10));
        sb.append(padRight(txn.getCardNumber(), 16));
        sb.append(formatTimestamp(txn.getOriginTimestamp(), 26));
        sb.append(formatTimestamp(txn.getProcessedTimestamp(), 26));
        sb.append(padRight("", 20)); // FILLER
        return sb.toString();
    }

    private String formatReasonCode(int code) {
        return padLeft(String.valueOf(code), 4, '0');
    }

    private String formatCobolAmount(BigDecimal amount, int width) {
        if (amount == null) {
            return padLeft("0", width, '0');
        }
        boolean negative = amount.signum() < 0;
        BigDecimal abs = amount.abs();
        String unscaled = abs.movePointRight(2).toBigInteger().toString();
        String padded = padLeft(unscaled, width - (negative ? 1 : 0), '0');
        return negative ? "-" + padded : padLeft(padded, width, '0');
    }

    private String formatTimestamp(java.time.LocalDateTime ts, int width) {
        if (ts == null) {
            return padRight("", width);
        }
        String formatted = ts.format(DB2_TS_FMT);
        return padRight(formatted, width);
    }

    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    private static String padLeft(String s, int width, char padChar) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        return String.valueOf(padChar).repeat(width - s.length()) + s;
    }
}
