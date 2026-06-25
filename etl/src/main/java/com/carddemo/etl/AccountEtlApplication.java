package com.carddemo.etl;

import com.carddemo.etl.db.AccountRepository;
import com.carddemo.etl.db.SchemaInitializer;
import com.carddemo.etl.reader.SourceFormat;
import com.carddemo.etl.validation.AccountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.Callable;

/**
 * Command-line entry point for the Account ETL.
 *
 * <p>Example:
 * <pre>
 *   java -jar account-etl.jar \
 *     --format ASCII \
 *     --input app/data/ASCII/acctdata.txt \
 *     --jdbc-url jdbc:postgresql://localhost:5432/carddemo \
 *     --db-user carddemo --db-password secret \
 *     --create-schema
 * </pre>
 */
@Command(
        name = "account-etl",
        mixinStandardHelpOptions = true,
        version = "account-etl 1.0.0",
        description = "Extract CardDemo Account records (CVACT01Y/ACCTDAT) and load them into PostgreSQL.")
public final class AccountEtlApplication implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(AccountEtlApplication.class);

    @Option(names = {"-f", "--format"}, required = true,
            description = "Source format: ${COMPLETION-CANDIDATES}.")
    private SourceFormat format;

    @Option(names = {"-i", "--input"}, required = true,
            description = "Path to the Account data file (ASCII seed or EBCDIC unload).")
    private Path input;

    @Option(names = {"--jdbc-url"},
            description = "PostgreSQL JDBC URL (required unless --dry-run).")
    private String jdbcUrl;

    @Option(names = {"--db-user"}, description = "Database user.")
    private String dbUser;

    @Option(names = {"--db-password"}, description = "Database password.", arity = "0..1", interactive = true)
    private String dbPassword;

    @Option(names = {"--batch-size"}, defaultValue = "500",
            description = "Number of records per insert batch (default: ${DEFAULT-VALUE}).")
    private int batchSize;

    @Option(names = {"--create-schema"}, defaultValue = "false",
            description = "Create the accounts table if it does not exist before loading.")
    private boolean createSchema;

    @Option(names = {"--dry-run"}, defaultValue = "false",
            description = "Parse and validate only; do not connect to or write to the database.")
    private boolean dryRun;

    @Override
    public Integer call() throws Exception {
        if (!Files.isReadable(input)) {
            log.error("Input file is not readable: {}", input);
            return 2;
        }

        AccountEtlPipeline pipeline = new AccountEtlPipeline(format, new AccountValidator(), batchSize, dryRun);

        EtlResult result;
        if (dryRun) {
            log.info("Running ETL in dry-run mode (no database writes).");
            try (InputStream in = Files.newInputStream(input)) {
                result = pipeline.run(in, null);
            }
        } else {
            if (jdbcUrl == null || jdbcUrl.isBlank()) {
                log.error("--jdbc-url is required unless --dry-run is set.");
                return 2;
            }
            try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
                if (createSchema) {
                    log.info("Ensuring accounts table exists.");
                    new SchemaInitializer().initialize(connection);
                }
                AccountRepository repository = new AccountRepository(connection);
                try (InputStream in = Files.newInputStream(input)) {
                    result = pipeline.run(in, repository);
                }
            }
        }

        log.info("Done. read={}, loaded={}, invalid={}, failed={}",
                result.read(), result.loaded(), result.invalid(), result.failed());
        return result.failed() > 0 ? 1 : 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AccountEtlApplication()).execute(args);
        System.exit(exitCode);
    }
}
