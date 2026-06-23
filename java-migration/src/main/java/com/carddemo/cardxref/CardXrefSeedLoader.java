package com.carddemo.cardxref;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Optional runtime loader that parses the original fixed-width VSAM extract
 * ({@code seed/cardxref.txt}) into {@link CardXref} rows. Activated only under the
 * {@code seed} profile; the Flyway migration {@code V10__seed_card_xref_data.sql}
 * is the primary seeding mechanism.
 *
 * <p>The ASCII extract omits the trailing 14-byte {@code FILLER}, so each line is
 * 36 characters: {@code XREF-CARD-NUM[0:16]}, {@code XREF-CUST-ID[16:25]},
 * {@code XREF-ACCT-ID[25:36]}. Short lines are handled defensively by padding so
 * substring offsets never overflow.</p>
 */
@Slf4j
@Configuration
@Profile("seed")
public class CardXrefSeedLoader {

    private static final String RESOURCE = "seed/cardxref.txt";
    private static final int RECORD_LEN = 36;

    CommandLineRunner seedCardXrefs(CardXrefRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("card_xref table already populated; skipping seed load.");
                return;
            }
            List<CardXref> parsed = parse();
            repository.saveAll(parsed);
            log.info("Seeded {} card xrefs from {}", parsed.size(), RESOURCE);
        };
    }

    static List<CardXref> parse() throws Exception {
        List<CardXref> rows = new ArrayList<>();
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

    private static CardXref parseRecord(String rec) {
        // Pad short lines (trailing FILLER omitted from the ASCII extract).
        String padded = rec.length() < RECORD_LEN
                ? String.format("%-" + RECORD_LEN + "s", rec)
                : rec;
        CardXref xref = new CardXref();
        xref.setXrefCardNum(padded.substring(0, 16).trim());
        xref.setXrefCustId(Long.parseLong(padded.substring(16, 25).trim()));
        xref.setXrefAcctId(Long.parseLong(padded.substring(25, 36).trim()));
        return xref;
    }
}
