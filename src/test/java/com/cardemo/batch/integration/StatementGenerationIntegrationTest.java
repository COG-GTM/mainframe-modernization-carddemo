package com.cardemo.batch.integration;

import com.cardemo.batch.TestDataFactory;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.CustomerRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import com.cardemo.batch.service.StatementGenerationService;
import com.cardemo.batch.service.StatementGenerationService.StatementResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration test for statement generation.
 * Validates the full flow from XREF iteration through statement output,
 * replacing the CBSTM03A mainline logic.
 */
@SpringBootTest
@ActiveProfiles("test")
class StatementGenerationIntegrationTest {

    @Autowired
    private StatementGenerationService service;

    @Autowired
    private TransactionRecordRepository transactionRepo;

    @Autowired
    private CardXrefRepository cardXrefRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private AccountRepository accountRepo;

    @BeforeEach
    void setUp() {
        transactionRepo.deleteAll();
        cardXrefRepo.deleteAll();
        customerRepo.deleteAll();
        accountRepo.deleteAll();

        // Set up complete cross-reference chain: card -> account -> customer
        customerRepo.save(TestDataFactory.createCustomer("000000001"));
        accountRepo.save(TestDataFactory.createAccount("00000000001"));
        cardXrefRepo.save(TestDataFactory.createCardXref(
                "4111111111111111", "000000001", "00000000001"));

        transactionRepo.save(TestDataFactory.createTransaction(
                "4111111111111111", "0000000000000001",
                "Grocery Store Purchase", new BigDecimal("45.67")));
        transactionRepo.save(TestDataFactory.createTransaction(
                "4111111111111111", "0000000000000002",
                "Gas Station", new BigDecimal("32.10")));
        transactionRepo.save(TestDataFactory.createTransaction(
                "4111111111111111", "0000000000000003",
                "Restaurant Dinner", new BigDecimal("78.50")));
    }

    @Test
    void generateAllStatements_producesCorrectOutput() {
        List<StatementResult> results = service.generateAllStatements();

        assertEquals(1, results.size());
        StatementResult result = results.get(0);
        assertEquals("4111111111111111", result.getCardNum());

        // Verify plain text output
        List<String> textLines = result.getPlainTextLines();
        assertFalse(textLines.isEmpty());
        String plainText = String.join("\n", textLines);
        assertTrue(plainText.contains("START OF STATEMENT"));
        assertTrue(plainText.contains("John"));
        assertTrue(plainText.contains("Doe"));
        assertTrue(plainText.contains("00000000001"));
        assertTrue(plainText.contains("Grocery Store Purchase"));
        assertTrue(plainText.contains("156.27"));
        assertTrue(plainText.contains("END OF STATEMENT"));

        // Verify HTML output
        List<String> htmlLines = result.getHtmlLines();
        assertFalse(htmlLines.isEmpty());
        String html = String.join("\n", htmlLines);
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("Bank of XYZ"));
        assertTrue(html.contains("John"));
        assertTrue(html.contains("Grocery Store Purchase"));
        assertTrue(html.contains("</html>"));
    }

    @Test
    void generateAllStatements_handlesMultipleCards() {
        // Add a second card chain
        customerRepo.save(TestDataFactory.createCustomer("000000002"));
        accountRepo.save(TestDataFactory.createAccount("00000000002"));
        cardXrefRepo.save(TestDataFactory.createCardXref(
                "4222222222222222", "000000002", "00000000002"));
        transactionRepo.save(TestDataFactory.createTransaction(
                "4222222222222222", "0000000000000010",
                "Online Shopping", new BigDecimal("199.99")));

        List<StatementResult> results = service.generateAllStatements();

        assertEquals(2, results.size());
    }

    @Test
    void generateAllStatements_transactionTotalsComputedWithBigDecimal() {
        List<StatementResult> results = service.generateAllStatements();

        assertNotNull(results);
        assertEquals(1, results.size());
        // Verify the total appears in the plain text (45.67 + 32.10 + 78.50 = 156.27)
        String plainText = String.join("\n", results.get(0).getPlainTextLines());
        assertTrue(plainText.contains("Total EXP:"));
        assertTrue(plainText.contains("156.27"));
    }

    @Test
    void generateAllStatements_crossReferenceChainWorks() {
        List<StatementResult> results = service.generateAllStatements();

        assertEquals(1, results.size());
        StatementResult result = results.get(0);

        // The xref maps card 4111111111111111 -> cust 000000001 -> acct 00000000001
        // Verify customer data appears (from cust 000000001)
        String plainText = String.join("\n", result.getPlainTextLines());
        assertTrue(plainText.contains("John"));
        assertTrue(plainText.contains("750")); // FICO score

        // Verify account data appears (from acct 00000000001)
        assertTrue(plainText.contains("5000.50"));

        // Verify HTML also has the cross-referenced data
        String html = String.join("\n", result.getHtmlLines());
        assertTrue(html.contains("00000000001"));
        assertTrue(html.contains("John"));
    }

    @Test
    void generateAllStatements_compositeKeySortOrder() {
        List<StatementResult> results = service.generateAllStatements();

        assertEquals(1, results.size());
        // Verify transactions appear in order by tran_id (part of composite key)
        List<String> textLines = results.get(0).getPlainTextLines();
        String plainText = String.join("\n", textLines);

        int groceryIdx = plainText.indexOf("Grocery Store Purchase");
        int gasIdx = plainText.indexOf("Gas Station");
        int restaurantIdx = plainText.indexOf("Restaurant Dinner");

        assertTrue(groceryIdx < gasIdx, "Grocery should appear before Gas Station");
        assertTrue(gasIdx < restaurantIdx, "Gas Station should appear before Restaurant");
    }
}
