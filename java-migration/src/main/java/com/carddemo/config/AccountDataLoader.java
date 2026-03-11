package com.carddemo.config;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

/**
 * Loads seed data from the original VSAM ACCTDAT flat file into the accounts table.
 * <p>
 * This loader parses fixed-width records (300 bytes each) using the field layout
 * reverse-engineered from the COBOL copybook {@code CVACT01Y} and the seed file
 * {@code app/data/ASCII/acctdata.txt}.
 * <p>
 * <b>Field offsets (0-based byte positions):</b>
 * <pre>
 *   Bytes  0-10  (11): ACCT-ID            – PIC 9(11)
 *   Byte   11    ( 1): ACTIVE-STATUS      – PIC X(1)
 *   Bytes 12-23  (12): CURR-BAL           – PIC S9(10)V99 (zoned decimal)
 *   Bytes 24-35  (12): CREDIT-LIMIT       – PIC S9(10)V99 (zoned decimal)
 *   Bytes 36-47  (12): CASH-CREDIT-LIMIT  – PIC S9(10)V99 (zoned decimal)
 *   Bytes 48-57  (10): OPEN-DATE          – PIC X(10) YYYY-MM-DD
 *   Bytes 58-67  (10): EXPIRATION-DATE    – PIC X(10) YYYY-MM-DD
 *   Bytes 68-77  (10): REISSUE-DATE       – PIC X(10) YYYY-MM-DD
 *   Bytes 78-89  (12): CURR-CYC-CREDIT    – PIC S9(10)V99 (zoned decimal)
 *   Bytes 90-101 (12): CURR-CYC-DEBIT     – PIC S9(10)V99 (zoned decimal)
 *   Bytes 102-111(10): ADDR-ZIP           – PIC X(10)
 *   Bytes 112-121(10): GROUP-ID           – PIC X(10)
 *   Bytes 122-299(178): FILLER             – PIC X(178)
 * </pre>
 * <p>
 * This bean is only active under the {@code seed} profile.  The primary seeding
 * mechanism is the Flyway migration {@code V2__seed_accounts.sql}.
 */
@Configuration
public class AccountDataLoader {

    private static final Logger log = LoggerFactory.getLogger(AccountDataLoader.class);

    // EBCDIC/ASCII zoned-decimal positive sign overpunch characters
    private static final Map<Character, Character> POSITIVE_SIGNS = Map.ofEntries(
            Map.entry('{', '0'), Map.entry('A', '1'), Map.entry('B', '2'),
            Map.entry('C', '3'), Map.entry('D', '4'), Map.entry('E', '5'),
            Map.entry('F', '6'), Map.entry('G', '7'), Map.entry('H', '8'),
            Map.entry('I', '9')
    );

    // EBCDIC/ASCII zoned-decimal negative sign overpunch characters
    private static final Map<Character, Character> NEGATIVE_SIGNS = Map.ofEntries(
            Map.entry('}', '0'), Map.entry('J', '1'), Map.entry('K', '2'),
            Map.entry('L', '3'), Map.entry('M', '4'), Map.entry('N', '5'),
            Map.entry('O', '6'), Map.entry('P', '7'), Map.entry('Q', '8'),
            Map.entry('R', '9')
    );

    /**
     * CommandLineRunner that reads the VSAM seed file and inserts records.
     * Activated only with the {@code seed} Spring profile.
     */
    @Bean
    @Profile("seed")
    public CommandLineRunner loadAccountData(AccountRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Accounts table already contains data – skipping seed load.");
                return;
            }
            log.info("Loading account seed data from acctdata.txt ...");
            int count = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new ClassPathResource("seed/acctdata.txt").getInputStream(),
                    StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    Account account = parseRecord(line);
                    repository.save(account);
                    count++;
                }
            } catch (IOException e) {
                log.error("Failed to load account seed data", e);
                throw e;
            }
            log.info("Loaded {} account records.", count);
        };
    }

    /**
     * Parses a single fixed-width account record line into an {@link Account} entity.
     */
    static Account parseRecord(String line) {
        Account account = new Account();

        int pos = 0;
        account.setAcctId(Long.parseLong(line.substring(pos, pos + 11).trim()));
        pos += 11;

        account.setActiveStatus(line.substring(pos, pos + 1));
        pos += 1;

        account.setCurrBal(parseZonedDecimal(line.substring(pos, pos + 12)));
        pos += 12;

        account.setCreditLimit(parseZonedDecimal(line.substring(pos, pos + 12)));
        pos += 12;

        account.setCashCreditLimit(parseZonedDecimal(line.substring(pos, pos + 12)));
        pos += 12;

        account.setOpenDate(LocalDate.parse(line.substring(pos, pos + 10)));
        pos += 10;

        account.setExpirationDate(LocalDate.parse(line.substring(pos, pos + 10)));
        pos += 10;

        account.setReissueDate(LocalDate.parse(line.substring(pos, pos + 10)));
        pos += 10;

        account.setCurrCycCredit(parseZonedDecimal(line.substring(pos, pos + 12)));
        pos += 12;

        account.setCurrCycDebit(parseZonedDecimal(line.substring(pos, pos + 12)));
        pos += 12;

        account.setAddrZip(line.substring(pos, pos + 10).trim());
        pos += 10;

        account.setGroupId(line.substring(pos, pos + 10).trim());

        return account;
    }

    /**
     * Converts an ASCII zoned-decimal string (with trailing sign overpunch) to
     * a {@link BigDecimal} with 2 implied decimal places (V99).
     * <p>
     * In EBCDIC/ASCII zoned decimal the last byte encodes both a digit and the
     * sign.  For example, {@code '{'} means {@code +0}, {@code '}'} means
     * {@code -0}, {@code 'A'} means {@code +1}, {@code 'J'} means {@code -1},
     * and so on.
     *
     * @param raw the raw zoned-decimal string (e.g. {@code "00000001940{"})
     * @return the parsed decimal value (e.g. {@code 194.00})
     */
    static BigDecimal parseZonedDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        }
        raw = raw.trim();
        boolean negative = false;
        char lastChar = raw.charAt(raw.length() - 1);

        if (POSITIVE_SIGNS.containsKey(lastChar)) {
            raw = raw.substring(0, raw.length() - 1) + POSITIVE_SIGNS.get(lastChar);
        } else if (NEGATIVE_SIGNS.containsKey(lastChar)) {
            negative = true;
            raw = raw.substring(0, raw.length() - 1) + NEGATIVE_SIGNS.get(lastChar);
        }

        long longVal = Long.parseLong(raw);
        BigDecimal result = BigDecimal.valueOf(longVal, 2); // scale=2 → divide by 100
        if (negative) {
            result = result.negate();
        }
        return result;
    }
}
