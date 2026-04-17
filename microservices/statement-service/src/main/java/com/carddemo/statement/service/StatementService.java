package com.carddemo.statement.service;

import com.carddemo.statement.dto.AccountData;
import com.carddemo.statement.dto.CardXrefData;
import com.carddemo.statement.dto.StatementRequest;
import com.carddemo.statement.dto.StatementResponse;
import com.carddemo.statement.dto.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Core statement generation service.
 * Translates the main processing logic from CBSTM03A.CBL (1000-MAINLINE).
 *
 * <p>CBSTM03A's flow:
 * 1. Open all files (TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE) via CBSTM03B
 * 2. Read all transactions into WS-TRNX-TABLE (2D array: 51 cards x 10 txns)
 * 3. For each XREF record (1000-XREFFILE-GET-NEXT):
 *    a. Read customer record by XREF-CUST-ID (2000-CUSTFILE-GET)
 *    b. Read account record by XREF-ACCT-ID (3000-ACCTFILE-GET)
 *    c. Create statement header (5000-CREATE-STATEMENT)
 *    d. Match and write transactions (4000-TRNXFILE-GET, 6000-WRITE-TRANS)
 * 4. Close all files
 *
 * In this modernized version, file I/O is replaced by HTTP service calls.</p>
 */
@Service
public class StatementService {

    private static final Logger log = LoggerFactory.getLogger(StatementService.class);

    private final AccountClient accountClient;
    private final CardClient cardClient;
    private final TransactionClient transactionClient;
    private final StatementFormatter formatter;

    public StatementService(AccountClient accountClient, CardClient cardClient,
                            TransactionClient transactionClient, StatementFormatter formatter) {
        this.accountClient = accountClient;
        this.cardClient = cardClient;
        this.transactionClient = transactionClient;
        this.formatter = formatter;
    }

    /**
     * Generate a statement for the given account and date range.
     * Orchestrates cross-service calls and formats the output.
     */
    public StatementResponse generateStatement(StatementRequest request) {
        log.info("Generating statement for accountId={}, period={} to {}",
                request.getAccountId(), request.getStartDate(), request.getEndDate());

        // Step 1: Fetch account details (replaces 3000-ACCTFILE-GET + 2000-CUSTFILE-GET)
        AccountData account = accountClient.getAccount(request.getAccountId());

        // Step 2: Fetch card cross-references (replaces 1000-XREFFILE-GET-NEXT)
        List<CardXrefData> cards = cardClient.getCardsForAccount(request.getAccountId());

        // Step 3: For each card, fetch transactions (replaces 4000-TRNXFILE-GET)
        // In COBOL, transactions were pre-loaded into WS-TRNX-TABLE (51 cards x 10 txns)
        // and matched by card number. Here we fetch per-card via the Transaction Service.
        List<TransactionData> allTransactions = new ArrayList<>();
        for (CardXrefData card : cards) {
            List<TransactionData> cardTransactions = transactionClient.getTransactions(
                    card.getCardNumber(), request.getStartDate(), request.getEndDate());
            allTransactions.addAll(cardTransactions);
        }

        log.info("Retrieved {} cards and {} transactions for accountId={}",
                cards.size(), allTransactions.size(), request.getAccountId());

        // Step 4: Generate statement output in both formats
        // (replaces 5000-CREATE-STATEMENT + 6000-WRITE-TRANS)
        String textOutput = formatter.formatTextStatement(
                account, allTransactions, request.getStartDate(), request.getEndDate());
        String htmlOutput = formatter.formatHtmlStatement(
                account, allTransactions, request.getStartDate(), request.getEndDate());

        String statementPeriod = request.getStartDate() + " to " + request.getEndDate();

        StatementResponse response = new StatementResponse(
                request.getAccountId(), statementPeriod, textOutput, htmlOutput);

        log.info("Statement generated successfully for accountId={}", request.getAccountId());
        return response;
    }
}
