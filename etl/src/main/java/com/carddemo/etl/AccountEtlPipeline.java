package com.carddemo.etl;

import com.carddemo.etl.db.AccountRepository;
import com.carddemo.etl.model.Account;
import com.carddemo.etl.reader.AccountRecordParser;
import com.carddemo.etl.reader.AccountRecordReader;
import com.carddemo.etl.reader.RecordParseException;
import com.carddemo.etl.reader.SourceFormat;
import com.carddemo.etl.validation.AccountValidator;
import com.carddemo.etl.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Orchestrates extract → transform → validate → load for Account records.
 *
 * <p>Records are read lazily, parsed and validated individually so a single malformed or
 * business-invalid record is skipped (and counted) rather than aborting the whole run, and valid
 * records are flushed to the database in fixed-size batches.
 */
public final class AccountEtlPipeline {

    private static final Logger log = LoggerFactory.getLogger(AccountEtlPipeline.class);

    private final SourceFormat format;
    private final AccountValidator validator;
    private final int batchSize;
    private final boolean dryRun;

    public AccountEtlPipeline(SourceFormat format, AccountValidator validator, int batchSize, boolean dryRun) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        this.format = format;
        this.validator = validator;
        this.batchSize = batchSize;
        this.dryRun = dryRun;
    }

    /**
     * Runs the pipeline over {@code input}, loading valid accounts via {@code repository}. When the
     * pipeline is in dry-run mode the repository is not used and may be {@code null}.
     */
    public EtlResult run(InputStream input, AccountRepository repository) throws SQLException {
        AccountRecordReader reader = new AccountRecordReader(format);
        long read = 0;
        long loaded = 0;
        long invalid = 0;
        long failed = 0;
        List<Account> batch = new ArrayList<>(batchSize);

        try (var rawStream = reader.rawRecords(input)) {
            Iterator<String> records = rawStream.iterator();
            while (records.hasNext()) {
                String raw = records.next();
                Account account;
                try {
                    account = AccountRecordParser.parse(raw);
                } catch (RecordParseException e) {
                    failed++;
                    log.warn("Skipping unparseable record #{}: {}", read + failed, e.getMessage());
                    continue;
                }
                read++;

                ValidationResult validation = validator.validate(account);
                if (!validation.isValid()) {
                    invalid++;
                    log.warn("Skipping invalid account {}: {}", account.acctId(), validation.summary());
                    continue;
                }

                batch.add(account);
                if (batch.size() >= batchSize) {
                    loaded += flush(batch, repository);
                }
            }
            loaded += flush(batch, repository);
        }

        EtlResult result = new EtlResult(read, loaded, invalid, failed);
        log.info("ETL complete: read={}, loaded={}, invalid={}, failed={}",
                result.read(), result.loaded(), result.invalid(), result.failed());
        return result;
    }

    private long flush(List<Account> batch, AccountRepository repository) throws SQLException {
        if (batch.isEmpty()) {
            return 0;
        }
        long count;
        if (dryRun) {
            count = batch.size();
        } else {
            count = repository.upsertBatch(batch);
        }
        batch.clear();
        return count;
    }
}
