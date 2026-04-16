package com.cardemo.batch.service;

import com.cardemo.batch.entity.Account;
import com.cardemo.batch.entity.CardXref;
import com.cardemo.batch.entity.Customer;
import com.cardemo.batch.entity.TransactionRecord;
import com.cardemo.batch.formatter.HtmlStatementFormatter;
import com.cardemo.batch.formatter.PlainTextStatementFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main statement generation logic ported from CBSTM03A.CBL.
 * <p>
 * Replaces the COBOL PROCEDURE DIVISION flow:
 * <ol>
 *   <li>Open all files (now handled by Spring Data repositories)</li>
 *   <li>Read all transactions into 2D array (now SQL query per card)</li>
 *   <li>Iterate XREFFILE sequentially (card cross-references)</li>
 *   <li>For each card: lookup customer + account, load transactions</li>
 *   <li>Generate both plain text and HTML output simultaneously</li>
 *   <li>Close all files (managed by connection pool)</li>
 * </ol>
 * <p>
 * Mainframe control block addressing (PSA/TCB/TIOT, lines 262-291)
 * is replaced with Spring Batch job metadata logging.
 */
@Service
public class StatementGenerationService {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationService.class);

    private final StatementFileIOService fileIOService;
    private final PlainTextStatementFormatter textFormatter;
    private final HtmlStatementFormatter htmlFormatter;

    public StatementGenerationService(StatementFileIOService fileIOService,
                                      PlainTextStatementFormatter textFormatter,
                                      HtmlStatementFormatter htmlFormatter) {
        this.fileIOService = fileIOService;
        this.textFormatter = textFormatter;
        this.htmlFormatter = htmlFormatter;
    }

    /**
     * Generates statements for all cards in the cross-reference file.
     * This is the main entry point replacing CBSTM03A 1000-MAINLINE.
     *
     * @return list of generated statement results (one per card)
     */
    public List<StatementResult> generateAllStatements() {
        log.info("Starting statement generation (replaces CBSTM03A main flow)");

        List<CardXref> xrefs = fileIOService.readAllXrefs();
        log.info("Loaded {} card cross-references from XREFFILE", xrefs.size());

        List<StatementResult> results = new ArrayList<>();

        for (CardXref xref : xrefs) {
            try {
                StatementResult result = generateStatementForCard(xref);
                results.add(result);
            } catch (Exception e) {
                log.error("Error generating statement for card {}: {}",
                        xref.getCardNum(), e.getMessage(), e);
            }
        }

        log.info("Statement generation complete. Generated {} statements.", results.size());
        return results;
    }

    /**
     * Generates a single statement for a card cross-reference entry.
     * Corresponds to one iteration of the PERFORM UNTIL loop in 1000-MAINLINE.
     */
    public StatementResult generateStatementForCard(CardXref xref) {
        // 2000-CUSTFILE-GET: lookup customer by XREF-CUST-ID
        Optional<Customer> custOpt = fileIOService.readCustomerByKey(xref.getCustId());
        if (custOpt.isEmpty()) {
            throw new IllegalStateException(
                    "Customer not found for ID: " + xref.getCustId());
        }
        Customer customer = custOpt.get();

        // 3000-ACCTFILE-GET: lookup account by XREF-ACCT-ID
        Optional<Account> acctOpt = fileIOService.readAccountByKey(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            throw new IllegalStateException(
                    "Account not found for ID: " + xref.getAcctId());
        }
        Account account = acctOpt.get();

        // 4000-TRNXFILE-GET: load transactions for this card
        List<TransactionRecord> transactions =
                fileIOService.readTransactionsByCard(xref.getCardNum());

        // Compute WS-TOTAL-AMT (running total per card, COMP-3 PIC S9(9)V99)
        BigDecimal totalAmount = computeTotalAmount(transactions);

        // Build statement data
        StatementData statementData = new StatementData(
                xref.getCardNum(), customer, account, transactions, totalAmount);

        // 5000-CREATE-STATEMENT + 6000-WRITE-TRANS: generate both formats
        List<String> plainTextLines = textFormatter.format(statementData);
        List<String> htmlLines = htmlFormatter.format(statementData);

        log.debug("Generated statement for card {} with {} transactions, total: {}",
                xref.getCardNum(), transactions.size(), totalAmount);

        return new StatementResult(xref.getCardNum(), plainTextLines, htmlLines);
    }

    /**
     * Computes the running total of transaction amounts.
     * Matches COBOL: ADD TRNX-AMT TO WS-TOTAL-AMT (COMP-3 PIC S9(9)V99).
     */
    BigDecimal computeTotalAmount(List<TransactionRecord> transactions) {
        BigDecimal total = BigDecimal.ZERO;
        for (TransactionRecord txn : transactions) {
            if (txn.getAmount() != null) {
                total = total.add(txn.getAmount());
            }
        }
        return total;
    }

    /**
     * Holds the generated output for a single card's statement.
     */
    public static class StatementResult {
        private final String cardNum;
        private final List<String> plainTextLines;
        private final List<String> htmlLines;

        public StatementResult(String cardNum,
                               List<String> plainTextLines,
                               List<String> htmlLines) {
            this.cardNum = cardNum;
            this.plainTextLines = plainTextLines;
            this.htmlLines = htmlLines;
        }

        public String getCardNum() {
            return cardNum;
        }

        public List<String> getPlainTextLines() {
            return plainTextLines;
        }

        public List<String> getHtmlLines() {
            return htmlLines;
        }
    }
}
