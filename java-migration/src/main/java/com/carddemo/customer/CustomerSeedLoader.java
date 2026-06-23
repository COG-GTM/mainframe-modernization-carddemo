package com.carddemo.customer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * ({@code seed/custdata.txt}) into {@link Customer} rows. Activated only under the
 * {@code seed} profile; the Flyway migration {@code V6__seed_customer_data.sql} is
 * the primary seeding mechanism.
 *
 * <p>The CUSTDAT layout contains only display ({@code PIC X}) and unsigned
 * numeric ({@code PIC 9}) fields, so unlike the Account loader there is no
 * zoned-decimal overpunch decoding — fields are read directly from their
 * fixed-width offsets.</p>
 */
@Slf4j
@Configuration
@Profile("seed")
public class CustomerSeedLoader {

    private static final String RESOURCE = "seed/custdata.txt";

    CommandLineRunner seedCustomers(CustomerRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Customer table already populated; skipping seed load.");
                return;
            }
            List<Customer> parsed = parse();
            repository.saveAll(parsed);
            log.info("Seeded {} customers from {}", parsed.size(), RESOURCE);
        };
    }

    static List<Customer> parse() throws Exception {
        List<Customer> rows = new ArrayList<>();
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

    private static Customer parseRecord(String rec) {
        Customer cust = new Customer();
        cust.setCustId(Long.parseLong(rec.substring(0, 9).trim()));
        cust.setCustFirstName(trimToNull(rec.substring(9, 34)));
        cust.setCustMiddleName(trimToNull(rec.substring(34, 59)));
        cust.setCustLastName(trimToNull(rec.substring(59, 84)));
        cust.setCustAddrLine1(trimToNull(rec.substring(84, 134)));
        cust.setCustAddrLine2(trimToNull(rec.substring(134, 184)));
        cust.setCustAddrLine3(trimToNull(rec.substring(184, 234)));
        cust.setCustAddrStateCd(trimToNull(rec.substring(234, 236)));
        cust.setCustAddrCountryCd(trimToNull(rec.substring(236, 239)));
        cust.setCustAddrZip(trimToNull(rec.substring(239, 249)));
        cust.setCustPhoneNum1(trimToNull(rec.substring(249, 264)));
        cust.setCustPhoneNum2(trimToNull(rec.substring(264, 279)));
        cust.setCustSsn(trimToNull(rec.substring(279, 288)));
        cust.setCustGovtIssuedId(trimToNull(rec.substring(288, 308)));
        cust.setCustDobYyyyMmDd(parseDate(rec.substring(308, 318)));
        cust.setCustEftAccountId(trimToNull(rec.substring(318, 328)));
        cust.setCustPriCardHolderInd(trimToNull(rec.substring(328, 329)));
        cust.setCustFicoCreditScore(parseIntOrNull(rec.substring(329, 332)));
        // bytes 332-500 are FILLER PIC X(168) — not mapped
        return cust;
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

    private static Integer parseIntOrNull(String raw) {
        String v = raw.trim();
        return v.isEmpty() ? null : Integer.valueOf(v);
    }

    private static String trimToNull(String raw) {
        String v = raw.trim();
        return v.isEmpty() ? null : v;
    }
}
