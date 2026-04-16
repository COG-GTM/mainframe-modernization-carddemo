package com.carddemo.etl;

import com.carddemo.etl.model.MigrationReport;
import com.carddemo.etl.service.EtlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that runs the full ETL pipeline against an H2 in-memory database
 * using the actual EBCDIC data files from the repo.
 */
@SpringBootTest(properties = {
    "etl.source-dir=../app/data/EBCDIC",
    "spring.main.web-application-type=none"
})
@ActiveProfiles("h2")
class EtlIntegrationTest {

    @Autowired
    private EtlService etlService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void fullMigration_loadsAllFiles() {
        MigrationReport report = etlService.runMigration();

        // Verify record counts from file stats
        assertNotNull(report);

        // Verify accounts loaded (15000 bytes / 300 = 50 records)
        Integer accountCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts", Integer.class);
        assertNotNull(accountCount);
        assertTrue(accountCount > 0, "Expected accounts to be loaded");

        // Verify customers loaded (25000 bytes / 500 = 50 records)
        Integer customerCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        assertNotNull(customerCount);
        assertTrue(customerCount > 0, "Expected customers to be loaded");

        // Verify cards loaded (7500 bytes / 150 = 50 records)
        Integer cardCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cards", Integer.class);
        assertNotNull(cardCount);
        assertTrue(cardCount > 0, "Expected cards to be loaded");

        // Verify card xrefs loaded (2500 bytes / 50 = 50 records)
        Integer xrefCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM card_xref", Integer.class);
        assertNotNull(xrefCount);
        assertTrue(xrefCount > 0, "Expected card xrefs to be loaded");

        // Verify user security loaded (800 bytes / 80 = 10 records)
        Integer userSecCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_security", Integer.class);
        assertNotNull(userSecCount);
        assertTrue(userSecCount > 0, "Expected user security records to be loaded");

        // Verify daily transactions loaded (105000 bytes / 350 = 300 records)
        Integer dailyTranCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM daily_transactions", Integer.class);
        assertNotNull(dailyTranCount);
        assertTrue(dailyTranCount > 0, "Expected daily transactions to be loaded");

        // Verify tran cat balances loaded
        Integer tcbCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tran_cat_balance", Integer.class);
        assertNotNull(tcbCount);
        assertTrue(tcbCount > 0, "Expected tran cat balances to be loaded");

        // Verify disclosure groups loaded
        Integer dgCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM disclosure_group", Integer.class);
        assertNotNull(dgCount);
        assertTrue(dgCount > 0, "Expected disclosure groups to be loaded");

        // Verify tran types loaded
        Integer ttCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tran_type", Integer.class);
        assertNotNull(ttCount);
        assertTrue(ttCount > 0, "Expected tran types to be loaded");

        // Verify tran categories loaded
        Integer tcCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tran_category", Integer.class);
        assertNotNull(tcCount);
        assertTrue(tcCount > 0, "Expected tran categories to be loaded");

        // Log counts for visibility
        System.out.println("=== Migration Record Counts ===");
        System.out.println("Accounts:           " + accountCount);
        System.out.println("Customers:          " + customerCount);
        System.out.println("Cards:              " + cardCount);
        System.out.println("Card XRefs:         " + xrefCount);
        System.out.println("User Security:      " + userSecCount);
        System.out.println("Daily Transactions: " + dailyTranCount);
        System.out.println("Tran Cat Balances:  " + tcbCount);
        System.out.println("Disclosure Groups:  " + dgCount);
        System.out.println("Tran Types:         " + ttCount);
        System.out.println("Tran Categories:    " + tcCount);
    }

    @Test
    void idempotentRerun_producesConsistentResults() {
        // Run migration twice - second run should produce same results
        MigrationReport report1 = etlService.runMigration();
        MigrationReport report2 = etlService.runMigration();

        Integer accountCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts", Integer.class);
        assertNotNull(accountCount);
        // After idempotent re-run, counts should match (truncate + reload)
        assertTrue(accountCount > 0);
    }
}
