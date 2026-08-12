package com.carddemo.batch.report;

import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCategory;
import com.carddemo.model.entity.TransactionCategoryId;
import com.carddemo.model.entity.TransactionType;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * COBOL program: CBTRN03C — transaction detail report (replaces the CBTRN03C step of
 * {@code app/jcl/TRANREPT.jcl}).
 *
 * <p>Consumes the already sorted/filtered TRANSACT extract (CVTRA05Y) that the SORT step of
 * TRANREPT.jcl produces, resolves CARDXREF (CVACT03Y), TRANTYPE (CVTRA03Y) and TRANCATG
 * (CVTRA04Y), and emits the 133-byte report lines of CVTRA07Y. The page break, account
 * subtotal and grand total bookkeeping of 1100/1110/1120-* is reproduced line for line,
 * including:
 * <ul>
 *   <li>the page break test {@code FUNCTION MOD(WS-LINE-COUNTER, WS-PAGE-SIZE) = 0} evaluated
 *       <em>after</em> the four heading lines of the first page have been counted;</li>
 *   <li>1110-WRITE-PAGE-TOTALS rolling the page total into the grand total and printing a new
 *       dashed rule, without counting the page-total line itself;</li>
 *   <li>the end-of-file branch of the main PERFORM, which adds the amount of the <em>last</em>
 *       transaction a second time to the page and account totals (READ INTO leaves TRAN-RECORD
 *       untouched at EOF) before writing the page and grand totals, and which never writes a
 *       closing account total.</li>
 * </ul>
 */
@Service
public class TransactionDetailReportService {

    /** WS-PAGE-SIZE PIC 9(03) COMP-3 VALUE 20. */
    public static final int PAGE_SIZE = 20;

    private final CardXrefRepository cardXrefRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionReportFormatter formatter;

    public TransactionDetailReportService(CardXrefRepository cardXrefRepository,
                                          TransactionTypeRepository transactionTypeRepository,
                                          TransactionCategoryRepository transactionCategoryRepository,
                                          TransactionReportFormatter formatter) {
        this.cardXrefRepository = cardXrefRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
        this.formatter = formatter;
    }

    /**
     * Generates the report with the semantics of the COBOL source: a transaction whose
     * TRAN-PROC-TS falls outside the date range triggers {@code NEXT SENTENCE}, which
     * transfers control past the period that closes the whole {@code PERFORM UNTIL} and
     * therefore ends the report right there.
     */
    public List<String> generate(List<Transaction> transactions, ReportDateRange range) {
        return generate(transactions, range, true);
    }

    /**
     * @param stopOnOutOfRange {@code true} reproduces the COBOL {@code NEXT SENTENCE} exit
     *                         described in {@link #generate(List, ReportDateRange)};
     *                         {@code false} skips the out-of-range record and keeps reading,
     *                         which is what the surrounding JCL (which pre-filters the extract
     *                         with a SORT INCLUDE COND on the same dates) makes the loop do in
     *                         practice.
     */
    public List<String> generate(List<Transaction> transactions,
                                 ReportDateRange range,
                                 boolean stopOnOutOfRange) {
        ReportState state = new ReportState();
        Transaction lastRead = null;

        for (Transaction transaction : transactions) {
            lastRead = transaction;
            if (!range.contains(transaction.getProcessTimestamp())) {
                if (stopOnOutOfRange) {
                    return state.lines;
                }
                continue;
            }
            writeTransactionReport(state, transaction, range);
        }

        // End of file: the record area still holds the last transaction read.
        if (lastRead != null && range.contains(lastRead.getProcessTimestamp())) {
            BigDecimal amount = CobolUtils.nvl(lastRead.getAmount());
            state.pageTotal = state.pageTotal.add(amount);
            state.accountTotal = state.accountTotal.add(amount);
            writePageTotals(state);
            state.lines.add(formatter.grandTotals(state.grandTotal));
        }
        return state.lines;
    }

    /** 1100-WRITE-TRANSACTION-REPORT. */
    private void writeTransactionReport(ReportState state, Transaction transaction, ReportDateRange range) {
        if (!state.currentCardNumber.equals(CobolUtils.padRight(transaction.getCardNumber(), 16))) {
            if (!state.firstTime) {
                writeAccountTotals(state);
            }
            state.currentCardNumber = CobolUtils.padRight(transaction.getCardNumber(), 16);
            state.cardXref = lookupXref(transaction.getCardNumber());
        }
        TransactionType type = lookupTransactionType(transaction.getTypeCode());
        TransactionCategory category =
                lookupTransactionCategory(transaction.getTypeCode(), transaction.getCategoryCode());

        if (state.firstTime) {
            state.firstTime = false;
            writeHeaders(state, range);
        }
        if (state.lineCounter % PAGE_SIZE == 0) {
            writePageTotals(state);
            writeHeaders(state, range);
        }

        BigDecimal amount = CobolUtils.nvl(transaction.getAmount());
        state.pageTotal = state.pageTotal.add(amount);
        state.accountTotal = state.accountTotal.add(amount);

        state.lines.add(formatter.detail(transaction.getTransactionId(),
                state.cardXref.getAccountId(),
                transaction.getTypeCode(),
                type.getDescription(),
                transaction.getCategoryCode(),
                category.getDescription(),
                transaction.getSource(),
                amount));
        state.lineCounter++;
    }

    /** 1120-WRITE-HEADERS. */
    private void writeHeaders(ReportState state, ReportDateRange range) {
        state.lines.add(formatter.nameHeader(range));
        state.lineCounter++;
        state.lines.add(formatter.blankLine());
        state.lineCounter++;
        state.lines.add(formatter.columnHeader());
        state.lineCounter++;
        state.lines.add(formatter.separator());
        state.lineCounter++;
    }

    /** 1110-WRITE-PAGE-TOTALS. */
    private void writePageTotals(ReportState state) {
        state.lines.add(formatter.pageTotals(state.pageTotal));
        state.grandTotal = state.grandTotal.add(state.pageTotal);
        state.pageTotal = BigDecimal.ZERO;
        state.lineCounter++;
        state.lines.add(formatter.separator());
        state.lineCounter++;
    }

    /** 1120-WRITE-ACCOUNT-TOTALS. */
    private void writeAccountTotals(ReportState state) {
        state.lines.add(formatter.accountTotals(state.accountTotal));
        state.accountTotal = BigDecimal.ZERO;
        state.lineCounter++;
        state.lines.add(formatter.separator());
        state.lineCounter++;
    }

    /** 1500-A-LOOKUP-XREF. */
    private CardXref lookupXref(String cardNumber) {
        return cardXrefRepository.findById(CobolUtils.padRight(cardNumber, 16).trim())
                .orElseThrow(() -> new IllegalStateException("INVALID CARD NUMBER : " + cardNumber));
    }

    /** 1500-B-LOOKUP-TRANTYPE. */
    private TransactionType lookupTransactionType(String typeCode) {
        return transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new IllegalStateException("INVALID TRANSACTION TYPE : " + typeCode));
    }

    /** 1500-C-LOOKUP-TRANCATG. */
    private TransactionCategory lookupTransactionCategory(String typeCode, Integer categoryCode) {
        return transactionCategoryRepository.findById(TransactionCategoryId.builder()
                        .typeCode(typeCode)
                        .categoryCode(categoryCode)
                        .build())
                .orElseThrow(() -> new IllegalStateException("INVALID TRAN CATG KEY : "
                        + typeCode + CobolUtils.padLeftZeros(categoryCode, 4)));
    }

    /** WS-REPORT-VARS of CBTRN03C. */
    private static final class ReportState {
        private final List<String> lines = new ArrayList<>();
        private long lineCounter;
        /** WS-FIRST-TIME, shared by the heading and the account-total control break. */
        private boolean firstTime = true;
        private String currentCardNumber = " ".repeat(16);
        private CardXref cardXref;
        private BigDecimal pageTotal = BigDecimal.ZERO;
        private BigDecimal accountTotal = BigDecimal.ZERO;
        private BigDecimal grandTotal = BigDecimal.ZERO;
    }
}
