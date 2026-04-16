package com.cardemo.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the full Transaction Detail Report batch job.
 * Uses H2 in-memory database with test data matching the COBOL program's
 * expected VSAM file contents.
 */
@SpringBatchTest
@SpringBootTest(properties = {
        "dateparm.start-date=2022-01-01",
        "dateparm.end-date=2022-12-31",
        "report.page-size=20",
        "report.output-path=${java.io.tmpdir}/cardemo-test-report.txt"
})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionDetailReportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Job transactionDetailReportJob;

    @Value("${report.output-path}")
    private String outputPath;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    @Test
    void jobShouldCompleteSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(transactionDetailReportJob);
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    void jobShouldProduceCorrectReport() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(transactionDetailReportJob);
        jobLauncherTestUtils.launchJob(params);

        Path reportPath = Path.of(outputPath);
        assertTrue(Files.exists(reportPath), "Report file should exist");

        List<String> lines = Files.readAllLines(reportPath);
        assertFalse(lines.isEmpty(), "Report should not be empty");

        // All lines should be exactly 133 characters
        for (String line : lines) {
            assertEquals(133, line.length(),
                    "All report lines must be 133 bytes: '" + line + "'");
        }

        // First line should be the report name header
        assertTrue(lines.get(0).contains("DALYREPT"));
        assertTrue(lines.get(0).contains("Daily Transaction Report"));
    }

    @Test
    void jobShouldFilterByDateRange() throws Exception {
        // Add an out-of-range transaction
        jdbcTemplate.update(
                "INSERT INTO transaction_record (tran_id, tran_type_cd, tran_cat_cd, tran_source, "
                        + "tran_desc, tran_amt, tran_card_num, tran_proc_ts) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "0000000000000099", "SA", 5001, "ONLINE",
                "Out of range", 999.99, "1234567890123456",
                "2021-06-15T10:30:00.000000"
        );

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(transactionDetailReportJob);
        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        Path reportPath = Path.of(outputPath);
        if (Files.exists(reportPath)) {
            String content = Files.readString(reportPath);
            // The out-of-range transaction should not appear
            assertFalse(content.contains("0000000000000099"));
        }
    }

    private void setupTestData() {
        // Create tables for H2
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transaction_record ("
                + "tran_id VARCHAR(16) NOT NULL, "
                + "tran_type_cd VARCHAR(2) NOT NULL, "
                + "tran_cat_cd INTEGER NOT NULL, "
                + "tran_source VARCHAR(10), "
                + "tran_desc VARCHAR(100), "
                + "tran_amt NUMERIC(11,2) NOT NULL, "
                + "tran_merchant_id NUMERIC(9), "
                + "tran_merchant_name VARCHAR(50), "
                + "tran_merchant_city VARCHAR(50), "
                + "tran_merchant_zip VARCHAR(10), "
                + "tran_card_num VARCHAR(16) NOT NULL, "
                + "tran_orig_ts VARCHAR(26), "
                + "tran_proc_ts VARCHAR(26) NOT NULL, "
                + "PRIMARY KEY (tran_id))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS card_xref ("
                + "xref_card_num VARCHAR(16) NOT NULL, "
                + "xref_cust_id NUMERIC(9), "
                + "xref_acct_id VARCHAR(11) NOT NULL, "
                + "PRIMARY KEY (xref_card_num))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tran_type ("
                + "tran_type VARCHAR(2) NOT NULL, "
                + "tran_type_desc VARCHAR(50) NOT NULL, "
                + "PRIMARY KEY (tran_type))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tran_category ("
                + "tran_type_cd VARCHAR(2) NOT NULL, "
                + "tran_cat_cd INTEGER NOT NULL, "
                + "tran_cat_type_desc VARCHAR(50) NOT NULL, "
                + "PRIMARY KEY (tran_type_cd, tran_cat_cd))");

        // Clear existing data
        jdbcTemplate.execute("DELETE FROM transaction_record");
        jdbcTemplate.execute("DELETE FROM card_xref");
        jdbcTemplate.execute("DELETE FROM tran_type");
        jdbcTemplate.execute("DELETE FROM tran_category");

        // Insert reference data
        jdbcTemplate.update("INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES (?, ?, ?)",
                "1234567890123456", 1, "00012345678");
        jdbcTemplate.update("INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES (?, ?, ?)",
                "9876543210987654", 2, "00098765432");

        jdbcTemplate.update("INSERT INTO tran_type (tran_type, tran_type_desc) VALUES (?, ?)",
                "SA", "Sale");
        jdbcTemplate.update("INSERT INTO tran_type (tran_type, tran_type_desc) VALUES (?, ?)",
                "CR", "Credit");

        jdbcTemplate.update("INSERT INTO tran_category (tran_type_cd, tran_cat_cd, tran_cat_type_desc) VALUES (?, ?, ?)",
                "SA", 5001, "Retail Purchase");
        jdbcTemplate.update("INSERT INTO tran_category (tran_type_cd, tran_cat_cd, tran_cat_type_desc) VALUES (?, ?, ?)",
                "CR", 1001, "Return");

        // Insert test transactions within date range
        jdbcTemplate.update(
                "INSERT INTO transaction_record (tran_id, tran_type_cd, tran_cat_cd, tran_source, "
                        + "tran_desc, tran_amt, tran_card_num, tran_proc_ts) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "0000000000000001", "SA", 5001, "ONLINE",
                "Test purchase 1", 100.00, "1234567890123456",
                "2022-06-15T10:30:00.000000");

        jdbcTemplate.update(
                "INSERT INTO transaction_record (tran_id, tran_type_cd, tran_cat_cd, tran_source, "
                        + "tran_desc, tran_amt, tran_card_num, tran_proc_ts) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "0000000000000002", "SA", 5001, "POS",
                "Test purchase 2", 250.50, "1234567890123456",
                "2022-07-20T14:00:00.000000");

        jdbcTemplate.update(
                "INSERT INTO transaction_record (tran_id, tran_type_cd, tran_cat_cd, tran_source, "
                        + "tran_desc, tran_amt, tran_card_num, tran_proc_ts) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "0000000000000003", "CR", 1001, "ONLINE",
                "Return", -50.00, "9876543210987654",
                "2022-08-10T09:15:00.000000");
    }
}
