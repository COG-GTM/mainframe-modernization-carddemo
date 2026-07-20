package com.carddemo.signon.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Loads the sample {@code USRSEC} users into the datastore at startup, the cloud
 * analogue of the {@code DUSRSECJ} job that loads the VSAM KSDS from the
 * in-stream PS data. Records are read from {@code usrsec-seed.txt} (fixed-width,
 * copybook {@code CSUSR01Y}) and mapped with {@link UsrsecRecordMapper}.
 *
 * <p>Idempotent: seeding is skipped when the datastore already contains users.
 */
@Component
public class UsrsecSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UsrsecSeeder.class);
    private static final String SEED_RESOURCE = "usrsec-seed.txt";

    private final UserSecurityRepository repository;

    public UsrsecSeeder(UserSecurityRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        List<UserSecurity> users = loadSeedRecords();
        repository.saveAll(users);
        log.info("Seeded {} USRSEC users from {}", users.size(), SEED_RESOURCE);
    }

    static List<UserSecurity> loadSeedRecords() {
        List<UserSecurity> users = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);
        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("*") || line.startsWith("#")) {
                    continue;
                }
                // Right-pad/truncate to the copybook record length so the seed
                // file is robust to trailing-whitespace trimming by tooling.
                users.add(UsrsecRecordMapper.parse(toRecordLength(line)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read USRSEC seed data", e);
        }
        return users;
    }

    private static String toRecordLength(String line) {
        if (line.length() >= UsrsecRecordMapper.RECORD_LENGTH) {
            return line.substring(0, UsrsecRecordMapper.RECORD_LENGTH);
        }
        StringBuilder sb = new StringBuilder(line);
        while (sb.length() < UsrsecRecordMapper.RECORD_LENGTH) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
