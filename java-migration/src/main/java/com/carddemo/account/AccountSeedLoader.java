package com.carddemo.account;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Optional runtime loader that parses the original fixed-width VSAM extract
 * ({@code seed/acctdata.txt}) into {@link Account} rows. Activated only under the
 * {@code seed} profile; the Flyway migration {@code V2__seed_account_data.sql} is
 * the primary seeding mechanism.
 *
 * <p>Demonstrates the data-type traps from the COBOL layout: zoned-decimal
 * overpunch sign decoding, implied 2-digit decimals, and fixed-width offsets.</p>
 */
@Slf4j
@Configuration
@Profile("seed")
public class AccountSeedLoader {

    private static final String RESOURCE = "seed/acctdata.txt";

    CommandLineRunner seedAccounts(AccountRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Account table already populated; skipping seed load.");
                return;
            }
            List<Account> parsed = parse();
            repository.saveAll(parsed);
            log.info("Seeded {} accounts from {}", parsed.size(), RESOURCE);
        };
    }

    static List<Account> parse() throws Exception {
        List<Account> rows = new ArrayList<>();
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

    private static Account parseRecord(String rec) {
        Account acct = new Account();
        acct.setAcctId(Long.parseLong(rec.substring(0, 11).trim()));
        acct.setAcctActiveStatus(rec.substring(11, 12));
        acct.setAcctCurrBal(decodeMoney(rec.substring(12, 24)));
        acct.setAcctCreditLimit(decodeMoney(rec.substring(24, 36)));
        acct.setAcctCashCreditLimit(decodeMoney(rec.substring(36, 48)));
        acct.setAcctOpenDate(parseDate(rec.substring(48, 58)));
        acct.setAcctExpirationDate(parseDate(rec.substring(58, 68)));
        acct.setAcctReissueDate(parseDate(rec.substring(68, 78)));
        acct.setAcctCurrCycCredit(decodeMoney(rec.substring(78, 90)));
        acct.setAcctCurrCycDebit(decodeMoney(rec.substring(90, 102)));
        acct.setAcctAddrZip(trimToNull(rec.substring(102, 112)));
        acct.setAcctGroupId(trimToNull(rec.substring(112, 122)));
        return acct;
    }

    /**
     * Decode a 12-character zoned-decimal {@code S9(10)V99} field. The final byte
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

    private static LocalDate parseDate(String raw) {
        String v = raw.trim();
        if (v.isEmpty() || v.chars().allMatch(c -> c == '0')) {
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String trimToNull(String raw) {
        String v = raw.trim();
        return v.isEmpty() ? null : v;
    }
}
