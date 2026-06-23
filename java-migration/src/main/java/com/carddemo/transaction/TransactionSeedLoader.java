package com.carddemo.transaction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Optional runtime loader that parses the original fixed-width daily-transaction
 * extract ({@code seed/dailytran.txt}) into {@link Transaction} rows. Activated
 * only under the {@code seed} profile; the Flyway migration
 * {@code V8__seed_transaction_data.sql} is the primary seeding mechanism.
 *
 * <p>Mirrors {@code AccountSeedLoader}: zoned-decimal overpunch sign decoding,
 * implied 2-digit decimals, and fixed-width offsets. {@code TRAN-AMT} is an
 * 11-byte {@code S9(09)V99} field (one byte narrower than the Account money
 * fields). Timestamps use {@link #TS_FORMAT}; blank values decode to {@code null}.</p>
 */
@Slf4j
@Configuration
@Profile("seed")
public class TransactionSeedLoader {

    private static final String RESOURCE = "seed/dailytran.txt";

    /** Stored timestamp form for {@code TRAN-ORIG-TS} / {@code TRAN-PROC-TS}, e.g. {@code 2022-06-10 19:27:53.000000}. */
    static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    CommandLineRunner seedTransactions(TransactionRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Transaction table already populated; skipping seed load.");
                return;
            }
            List<Transaction> parsed = parse();
            repository.saveAll(parsed);
            log.info("Seeded {} transactions from {}", parsed.size(), RESOURCE);
        };
    }

    static List<Transaction> parse() throws Exception {
        List<Transaction> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(RESOURCE);
        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in, StandardCharsets.US_ASCII))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                rows.add(parseRecord(line));
            }
        }
        return rows;
    }

    private static Transaction parseRecord(String rec) {
        Transaction tran = new Transaction();
        tran.setTranId(rec.substring(0, 16).trim());
        tran.setTranTypeCd(trimToNull(rec.substring(16, 18)));
        tran.setTranCatCd(parseIntOrNull(rec.substring(18, 22)));
        tran.setTranSource(trimToNull(rec.substring(22, 32)));
        tran.setTranDesc(trimToNull(rec.substring(32, 132)));
        tran.setTranAmt(decodeMoney(rec.substring(132, 143)));
        tran.setTranMerchantId(parseLongOrNull(rec.substring(143, 152)));
        tran.setTranMerchantName(trimToNull(rec.substring(152, 202)));
        tran.setTranMerchantCity(trimToNull(rec.substring(202, 252)));
        tran.setTranMerchantZip(trimToNull(rec.substring(252, 262)));
        tran.setTranCardNum(trimToNull(rec.substring(262, 278)));
        tran.setTranOrigTs(parseTimestamp(rec.substring(278, 304)));
        tran.setTranProcTs(parseTimestamp(rec.substring(304, 330)));
        return tran;
    }

    /**
     * Decode an 11-character zoned-decimal {@code S9(09)V99} field. The final byte
     * is an overpunch encoding the last digit plus the sign.
     */
    static BigDecimal decodeMoney(String raw) {
        char last = raw.charAt(raw.length() - 1);
        String body = raw.substring(0, raw.length() - 1);
        boolean negative = false;
        String lastDigit;
        switch (last) {
            case '{' -> lastDigit = "0";
            case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' -> lastDigit = String.valueOf(last - 'A' + 1);
            case '}' -> { lastDigit = "0"; negative = true; }
            case 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R' -> { lastDigit = String.valueOf(last - 'J' + 1); negative = true; }
            default -> {
                if (Character.isDigit(last)) {
                    lastDigit = String.valueOf(last);
                } else {
                    throw new IllegalArgumentException("Invalid overpunch char: " + last);
                }
            }
        }
        BigDecimal value = new BigDecimal(body + lastDigit).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY);
        return negative ? value.negate() : value;
    }

    private static LocalDateTime parseTimestamp(String raw) {
        String v = raw.trim();
        if (v.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(v, TS_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static Integer parseIntOrNull(String raw) {
        String v = raw.trim();
        return v.isEmpty() ? null : Integer.valueOf(v);
    }

    private static Long parseLongOrNull(String raw) {
        String v = raw.trim();
        return v.isEmpty() ? null : Long.valueOf(v);
    }

    private static String trimToNull(String raw) {
        String v = raw.trim();
        return v.isEmpty() ? null : v;
    }
}
