package com.carddemo.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.carddemo.event.StatementGeneratedEvent;
import com.carddemo.model.Account;
import com.carddemo.model.Customer;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;

/**
 * Statement generation service.
 *
 * Replaces: CBSTM03A main logic (924 lines) + CBSTM03B file I/O subroutine.
 * Also eliminates the need for CREASTMT.JCL steps:
 *   - DELDEF01 (delete/redefine TRXFL VSAM)
 *   - STEP010 (SORT TRANSACT by card+tran-id)
 *   - STEP020 (REPRO sequential → TRXFL VSAM)
 *   - STEP030 (delete previous output files)
 *
 * Generates per-account statements in both plain text and HTML formats,
 * faithfully reproducing the output structure from CBSTM03A.
 */
@Service
public class StatementGenerationService {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationService.class);
    static final String TOPIC_STATEMENT = "statement.generated";

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public StatementGenerationService(CustomerRepository customerRepository,
                                       AccountRepository accountRepository,
                                       TransactionRepository transactionRepository,
                                       CardXrefRepository cardXrefRepository,
                                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Generate a statement for a specific card.
     *
     * Replaces: the per-card iteration in CBSTM03A 1000-MAINLINE (lines 316-329):
     *   PERFORM 1000-XREFFILE-GET-NEXT   — iterate cards
     *   PERFORM 2000-CUSTFILE-GET         — lookup customer
     *   PERFORM 3000-ACCTFILE-GET         — lookup account
     *   PERFORM 5000-CREATE-STATEMENT     — write header
     *   PERFORM 4000-TRNXFILE-GET         — match transactions
     *
     * @param cardNumber the card number to generate a statement for
     * @param customerId the customer ID from XREF
     * @param accountId  the account ID from XREF
     */
    public void generateStatement(String cardNumber, long customerId, long accountId) {
        log.info("Generating statement for card={}, account={}", cardNumber, accountId);

        // Replaces: 2000-CUSTFILE-GET (CALL 'CBSTM03B' with CUSTFILE/READ-K)
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            log.error("Customer {} not found for statement generation", customerId);
            return;
        }

        // Replaces: 3000-ACCTFILE-GET (CALL 'CBSTM03B' with ACCTFILE/READ-K)
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            log.error("Account {} not found for statement generation", accountId);
            return;
        }

        // Replaces: 4000-TRNXFILE-GET — in-memory WS-TRNX-TABLE lookup
        // Also replaces CREASTMT STEP010 SORT + STEP020 REPRO (no temp TRXFL needed)
        List<Transaction> transactions = transactionRepository.findByCardNumOrderByTranId(cardNumber);

        // Replaces: 5000-CREATE-STATEMENT (lines 458-504) + 6000-WRITE-TRANS
        String textStatement = buildTextStatement(customer, account, cardNumber, transactions);

        // Replaces: 5100-WRITE-HTML-HEADER + 5200-WRITE-HTML-NMADBS + HTML transaction lines
        String htmlStatement = buildHtmlStatement(customer, account, cardNumber, transactions);

        BigDecimal totalExpense = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StatementGeneratedEvent event = new StatementGeneratedEvent(
                accountId, customerId, cardNumber,
                textStatement, htmlStatement,
                transactions.size(), totalExpense.toPlainString()
        );
        kafkaTemplate.send(TOPIC_STATEMENT, cardNumber, event);

        log.info("Statement generated for card={}: {} transactions, total={}",
                cardNumber, transactions.size(), totalExpense);
    }

    /**
     * Build plain-text statement content.
     *
     * Replaces: 5000-CREATE-STATEMENT (lines 458-504) which writes
     * ST-LINE0 through ST-LINE13 to STMTFILE, plus 6000-WRITE-TRANS
     * for individual transaction lines.
     */
    String buildTextStatement(Customer customer, Account account,
                              String cardNumber, List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();

        // Statement header (replaces ST-LINE0 through ST-LINE13)
        sb.append("========================================\n");
        sb.append("          CREDIT CARD STATEMENT\n");
        sb.append("========================================\n");
        sb.append(String.format("Card Number   : %s%n", cardNumber));
        sb.append(String.format("Account       : %d%n", account.getAcctId()));
        sb.append(String.format("Customer      : %s %s %s%n",
                nullSafe(customer.getFirstName()),
                nullSafe(customer.getMiddleName()),
                nullSafe(customer.getLastName())));
        sb.append(String.format("Address       : %s%n", nullSafe(customer.getAddrLine1())));
        if (customer.getAddrLine2() != null && !customer.getAddrLine2().isBlank()) {
            sb.append(String.format("                %s%n", customer.getAddrLine2()));
        }
        sb.append(String.format("                %s, %s %s%n",
                nullSafe(customer.getAddrStateCd()),
                nullSafe(customer.getAddrCountryCd()),
                nullSafe(customer.getAddrZip())));
        sb.append(String.format("Credit Limit  : %s%n",
                account.getCreditLimit() != null ? account.getCreditLimit().toPlainString() : "N/A"));
        sb.append(String.format("Current Bal   : %s%n",
                account.getCurrentBalance() != null ? account.getCurrentBalance().toPlainString() : "0.00"));
        sb.append(String.format("FICO Score    : %s%n",
                customer.getFicoCreditScore() != null ? customer.getFicoCreditScore() : "N/A"));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-16s %-10s %-30s %12s%n",
                "TRAN-ID", "TYPE", "DESCRIPTION", "AMOUNT"));
        sb.append("----------------------------------------\n");

        // Transaction lines (replaces 6000-WRITE-TRANS, lines 675-723)
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction txn : transactions) {
            sb.append(String.format("%-16s %-10s %-30s %12s%n",
                    nullSafe(txn.getTranId()),
                    nullSafe(txn.getTypeCd()),
                    truncate(nullSafe(txn.getDescription()), 30),
                    txn.getAmount() != null ? txn.getAmount().toPlainString() : "0.00"));
            if (txn.getAmount() != null) {
                total = total.add(txn.getAmount());
            }
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-58s %12s%n", "TOTAL", total.toPlainString()));
        sb.append("========================================\n");

        return sb.toString();
    }

    /**
     * Build HTML statement content.
     *
     * Replaces: 5100-WRITE-HTML-HEADER (lines 506-530),
     * 5200-WRITE-HTML-NMADBS (lines 532-672), and HTML transaction lines.
     *
     * Reproduces the table-based HTML layout with inline styles matching
     * the CBSTM03A output structure.
     */
    String buildHtmlStatement(Customer customer, Account account,
                              String cardNumber, List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();

        // HTML header (replaces 5100-WRITE-HTML-HEADER)
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n");
        sb.append("<title>Credit Card Statement</title>\n");
        sb.append("<style>\n");
        sb.append("  body { font-family: Arial, sans-serif; margin: 20px; }\n");
        sb.append("  table { border-collapse: collapse; width: 100%; }\n");
        sb.append("  th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        sb.append("  th { background-color: #4472C4; color: white; }\n");
        sb.append("  .header { background-color: #f5f5f5; padding: 15px; margin-bottom: 20px; }\n");
        sb.append("  .total { font-weight: bold; background-color: #e8e8e8; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        // Customer/account header (replaces 5200-WRITE-HTML-NMADBS)
        sb.append("<div class=\"header\">\n");
        sb.append("<h2>Credit Card Statement</h2>\n");
        sb.append(String.format("<p><strong>Card Number:</strong> %s</p>%n", cardNumber));
        sb.append(String.format("<p><strong>Account:</strong> %d</p>%n", account.getAcctId()));
        sb.append(String.format("<p><strong>Customer:</strong> %s %s %s</p>%n",
                nullSafe(customer.getFirstName()),
                nullSafe(customer.getMiddleName()),
                nullSafe(customer.getLastName())));
        sb.append(String.format("<p><strong>Address:</strong> %s</p>%n",
                nullSafe(customer.getAddrLine1())));
        sb.append(String.format("<p><strong>Credit Limit:</strong> %s</p>%n",
                account.getCreditLimit() != null ? account.getCreditLimit().toPlainString() : "N/A"));
        sb.append(String.format("<p><strong>Current Balance:</strong> %s</p>%n",
                account.getCurrentBalance() != null ? account.getCurrentBalance().toPlainString() : "0.00"));
        sb.append(String.format("<p><strong>FICO Score:</strong> %s</p>%n",
                customer.getFicoCreditScore() != null ? customer.getFicoCreditScore() : "N/A"));
        sb.append("</div>\n");

        // Transaction table
        sb.append("<table>\n<thead>\n<tr>\n");
        sb.append("<th>Transaction ID</th>\n");
        sb.append("<th>Type</th>\n");
        sb.append("<th>Category</th>\n");
        sb.append("<th>Description</th>\n");
        sb.append("<th>Merchant</th>\n");
        sb.append("<th>City</th>\n");
        sb.append("<th>Date</th>\n");
        sb.append("<th style=\"text-align:right\">Amount</th>\n");
        sb.append("</tr>\n</thead>\n<tbody>\n");

        BigDecimal total = BigDecimal.ZERO;
        for (Transaction txn : transactions) {
            sb.append("<tr>\n");
            sb.append(String.format("<td>%s</td>%n", nullSafe(txn.getTranId())));
            sb.append(String.format("<td>%s</td>%n", nullSafe(txn.getTypeCd())));
            sb.append(String.format("<td>%s</td>%n",
                    txn.getCatCd() != null ? txn.getCatCd() : ""));
            sb.append(String.format("<td>%s</td>%n", nullSafe(txn.getDescription())));
            sb.append(String.format("<td>%s</td>%n", nullSafe(txn.getMerchantName())));
            sb.append(String.format("<td>%s</td>%n", nullSafe(txn.getMerchantCity())));
            sb.append(String.format("<td>%s</td>%n", nullSafe(txn.getOriginTimestamp())));
            sb.append(String.format("<td style=\"text-align:right\">%s</td>%n",
                    txn.getAmount() != null ? txn.getAmount().toPlainString() : "0.00"));
            sb.append("</tr>\n");
            if (txn.getAmount() != null) {
                total = total.add(txn.getAmount());
            }
        }

        // Total row
        sb.append("<tr class=\"total\">\n");
        sb.append(String.format("<td colspan=\"7\">Total (%d transactions)</td>%n",
                transactions.size()));
        sb.append(String.format("<td style=\"text-align:right\">%s</td>%n",
                total.toPlainString()));
        sb.append("</tr>\n");
        sb.append("</tbody>\n</table>\n</body>\n</html>\n");

        return sb.toString();
    }

    private static String nullSafe(String value) {
        return value != null ? value.trim() : "";
    }

    private static String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
