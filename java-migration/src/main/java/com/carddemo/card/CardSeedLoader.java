package com.carddemo.card;

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
 * ({@code seed/carddata.txt}) into {@link Card} rows. Activated only under the
 * {@code seed} profile; the Flyway migration {@code V4__seed_card_data.sql} is
 * the primary seeding mechanism.
 *
 * <p>The ASCII extract contains only display fields (no COMP-3/COMP), so the
 * loader uses fixed-width offsets straight from copybook {@code CVACT02Y.cpy}
 * (record length 150, 59-byte trailing FILLER ignored).</p>
 */
@Slf4j
@Configuration
@Profile("seed")
public class CardSeedLoader {

    private static final String RESOURCE = "seed/carddata.txt";

    CommandLineRunner seedCards(CardRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Card table already populated; skipping seed load.");
                return;
            }
            List<Card> parsed = parse();
            repository.saveAll(parsed);
            log.info("Seeded {} cards from {}", parsed.size(), RESOURCE);
        };
    }

    static List<Card> parse() throws Exception {
        List<Card> rows = new ArrayList<>();
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

    private static Card parseRecord(String rec) {
        Card card = new Card();
        card.setCardNum(rec.substring(0, 16).trim());
        card.setCardAcctId(Long.parseLong(rec.substring(16, 27).trim()));
        card.setCardCvvCd(Integer.parseInt(rec.substring(27, 30).trim()));
        card.setCardEmbossedName(trimToNull(rec.substring(30, 80)));
        card.setCardExpirationDate(parseDate(rec.substring(80, 90)));
        card.setCardActiveStatus(rec.substring(90, 91));
        return card;
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
