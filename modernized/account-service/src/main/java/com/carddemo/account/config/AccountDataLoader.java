package com.carddemo.account.config;

import com.carddemo.account.model.Account;
import com.carddemo.account.parser.AccountRecordParser;
import com.carddemo.account.repository.AccountRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Seeds the {@code account} table from the CardDemo ASCII seed file
 * {@code data/acctdata.txt} (a copy of {@code app/data/ASCII/acctdata.txt}) on
 * startup, simulating the mainframe {@code ACCTFILE} initial-load JCL job. Runs
 * before {@link com.carddemo.account.batch.AccountReportRunner} so the report has
 * data to read.
 */
@Component
@Order(10)
public class AccountDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AccountDataLoader.class);
    private static final String SEED_RESOURCE = "data/acctdata.txt";

    private final AccountRepository accountRepository;
    private final AccountRecordParser parser;

    public AccountDataLoader(AccountRepository accountRepository, AccountRecordParser parser) {
        this.accountRepository = accountRepository;
        this.parser = parser;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (accountRepository.count() > 0) {
            return;
        }
        List<Account> accounts = loadSeedData();
        accountRepository.saveAll(accounts);
        log.info("Loaded {} account record(s) from {}", accounts.size(), SEED_RESOURCE);
    }

    private List<Account> loadSeedData() throws IOException {
        List<Account> accounts = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);
        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                accounts.add(parser.parse(line));
            }
        }
        return accounts;
    }
}
