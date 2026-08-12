package com.carddemo.batch.statement;

import com.carddemo.model.entity.Transaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COBOL program: CBSTM03A — WS-TRNX-TABLE / WS-TRN-TBL-CNTR, the two dimensional table the
 * program fills in 8500-READTRNX-READ while reading TRNXFILE once, and scans in
 * 4000-TRNXFILE-GET when printing each statement.
 *
 * <p>TRNXFILE arrives sorted on TRAN-CARD-NUM + TRAN-ID (see CREASTMT.JCL STEP010), so
 * 8500-READTRNX-READ starts a new card entry whenever the card number changes and the scan in
 * 4000-TRNXFILE-GET can stop as soon as a stored card number sorts after XREF-CARD-NUM.
 */
final class StatementTransactionTable {

    /** WS-CARD-TBL OCCURS 51 TIMES. */
    static final int MAX_CARDS = 51;

    /** WS-TRAN-TBL OCCURS 10 TIMES. */
    static final int MAX_TRANSACTIONS_PER_CARD = 10;

    private static final Logger LOG = LoggerFactory.getLogger(StatementTransactionTable.class);

    private final List<CardEntry> cards = new ArrayList<>();

    /** One WS-CARD-TBL occurrence: the card number and its WS-TRCT transactions. */
    record CardEntry(String cardNumber, List<Transaction> transactions) {
    }

    /**
     * Fills the table from TRNXFILE, which must already be in card number / transaction id
     * order. Records that would run past the OCCURS limits of the COBOL table are dropped.
     */
    static StatementTransactionTable load(List<Transaction> transactionFile) {
        StatementTransactionTable table = new StatementTransactionTable();
        String savedCard = null;
        for (Transaction transaction : transactionFile) {
            String cardNumber = StatementFormatter.alpha(transaction.getCardNumber(), 16);
            if (!cardNumber.equals(savedCard)) {
                savedCard = cardNumber;
                if (table.cards.size() == MAX_CARDS) {
                    LOG.warn("TRNXFILE holds more than {} cards; card {} is not statemented",
                            MAX_CARDS, cardNumber.trim());
                    break;
                }
                table.cards.add(new CardEntry(cardNumber, new ArrayList<>()));
            }
            List<Transaction> transactions = table.cards.get(table.cards.size() - 1).transactions();
            if (transactions.size() == MAX_TRANSACTIONS_PER_CARD) {
                LOG.warn("Card {} holds more than {} transactions; {} is not statemented",
                        cardNumber.trim(), MAX_TRANSACTIONS_PER_CARD, transaction.getTransactionId());
                continue;
            }
            transactions.add(transaction);
        }
        return table;
    }

    /**
     * 4000-TRNXFILE-GET: walks the card entries from the first one, stopping at the first card
     * number greater than XREF-CARD-NUM, and returns the transactions of the matching entry.
     */
    List<Transaction> transactionsFor(String xrefCardNumber) {
        String cardNumber = StatementFormatter.alpha(xrefCardNumber, 16);
        for (CardEntry card : cards) {
            if (card.cardNumber().compareTo(cardNumber) > 0) {
                break;
            }
            if (card.cardNumber().equals(cardNumber)) {
                return card.transactions();
            }
        }
        return Collections.emptyList();
    }
}
