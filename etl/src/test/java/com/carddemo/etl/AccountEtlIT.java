package com.carddemo.etl;

import com.carddemo.etl.db.AccountRepository;
import com.carddemo.etl.db.SchemaInitializer;
import com.carddemo.etl.reader.SourceFormat;
import com.carddemo.etl.validation.AccountValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * End-to-end integration test: runs the full ETL pipeline against a real PostgreSQL instance
 * (via Testcontainers) using the repository's actual seed data, for both the ASCII seed file and
 * the EBCDIC VSAM unload.
 */
@Testcontainers
class AccountEtlIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("carddemo")
                    .withUsername("carddemo")
                    .withPassword("carddemo");

    // Resolved relative to the etl/ module directory (Maven's working dir for tests).
    private static final Path ASCII_DATA = Path.of("..", "app", "data", "ASCII", "acctdata.txt");
    private static final Path EBCDIC_DATA =
            Path.of("..", "app", "data", "EBCDIC", "AWS.M2.CARDDEMO.ACCTDATA.PS");

    private static final int EXPECTED_RECORDS = 50;

    @BeforeAll
    static void seedDataPresent() {
        assumeTrue(Files.isReadable(ASCII_DATA), "ASCII seed data not found at " + ASCII_DATA);
    }

    @BeforeEach
    void resetSchema() throws Exception {
        try (Connection connection = connect()) {
            new SchemaInitializer().initialize(connection);
            try (Statement statement = connection.createStatement()) {
                statement.execute("TRUNCATE TABLE accounts");
            }
        }
    }

    @Test
    void loadsAsciiSeedDataIntoPostgres() throws Exception {
        EtlResult result = runEtl(SourceFormat.ASCII, ASCII_DATA);

        assertThat(result.read()).isEqualTo(EXPECTED_RECORDS);
        assertThat(result.loaded()).isEqualTo(EXPECTED_RECORDS);
        assertThat(result.invalid()).isZero();
        assertThat(result.failed()).isZero();

        assertThat(rowCount()).isEqualTo(EXPECTED_RECORDS);
        assertFirstAccountLoaded();
    }

    @Test
    void loadsEbcdicUnloadIntoPostgres() throws Exception {
        assumeTrue(Files.isReadable(EBCDIC_DATA), "EBCDIC unload not found at " + EBCDIC_DATA);

        EtlResult result = runEtl(SourceFormat.EBCDIC, EBCDIC_DATA);

        assertThat(result.read()).isEqualTo(EXPECTED_RECORDS);
        assertThat(result.loaded()).isEqualTo(EXPECTED_RECORDS);
        assertThat(rowCount()).isEqualTo(EXPECTED_RECORDS);
        assertFirstAccountLoaded();
    }

    @Test
    void reRunIsIdempotent() throws Exception {
        runEtl(SourceFormat.ASCII, ASCII_DATA);
        runEtl(SourceFormat.ASCII, ASCII_DATA);
        assertThat(rowCount()).isEqualTo(EXPECTED_RECORDS);
    }

    private EtlResult runEtl(SourceFormat format, Path data) throws Exception {
        AccountEtlPipeline pipeline = new AccountEtlPipeline(format, new AccountValidator(), 25, false);
        try (Connection connection = connect();
             InputStream in = Files.newInputStream(data)) {
            AccountRepository repository = new AccountRepository(connection);
            return pipeline.run(in, repository);
        }
    }

    private void assertFirstAccountLoaded() throws Exception {
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT active_status, curr_bal, credit_limit, cash_credit_limit, open_date "
                             + "FROM accounts WHERE acct_id = 1")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("active_status")).isEqualTo("Y");
            assertThat(rs.getBigDecimal("curr_bal")).isEqualByComparingTo(new BigDecimal("194.00"));
            assertThat(rs.getBigDecimal("credit_limit")).isEqualByComparingTo(new BigDecimal("2020.00"));
            assertThat(rs.getBigDecimal("cash_credit_limit")).isEqualByComparingTo(new BigDecimal("1020.00"));
            assertThat(rs.getDate("open_date").toLocalDate().toString()).isEqualTo("2014-11-20");
        }
    }

    private long rowCount() throws Exception {
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM accounts")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private Connection connect() throws Exception {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}
