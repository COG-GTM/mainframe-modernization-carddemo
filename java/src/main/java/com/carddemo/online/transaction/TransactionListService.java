package com.carddemo.online.transaction;

import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.dto.TransactionListRequest;
import com.carddemo.online.transaction.dto.TransactionListResponse;
import com.carddemo.online.transaction.dto.TransactionListRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COTRN00C — "List Transactions from TRANSACT file" (transaction CT00).
 *
 * <p>Files: TRANSACT (copybook CVTRA05Y). Screen: BMS mapset COTRN00, map COTRN0A. Pseudo
 * conversational state: COCOM01Y COMMAREA plus CDEMO-CT00-INFO, here {@link TransactionListState}.
 *
 * <p>The CICS browse (STARTBR / READNEXT / READPREV / ENDBR) is reproduced with keyed range
 * queries: a page holds ten records, the eleventh read decides CDEMO-CT00-NEXT-PAGE-FLG, and PF7
 * and PF8 re-position the browse on CDEMO-CT00-TRNID-FIRST / -LAST exactly as the program does —
 * including skipping the record the browse is positioned on for the keys other than ENTER.
 */
@Service
public class TransactionListService {

    static final int PAGE_SIZE = 10;

    static final String MSG_INVALID_SELECTION = "Invalid selection. Valid value is S";
    static final String MSG_TRAN_ID_NOT_NUMERIC = "Tran ID must be Numeric ...";
    static final String MSG_TOP_OF_PAGE = "You are at the top of the page...";
    static final String MSG_BOTTOM_REACHED = "You have reached the bottom of the page...";
    static final String MSG_TOP_REACHED = "You have reached the top of the page...";
    static final String MSG_ALREADY_TOP = "You are already at the top of the page...";
    static final String MSG_ALREADY_BOTTOM = "You are already at the bottom of the page...";

    static final String TRANSACTION_VIEW_PROGRAM = "COTRN01C";

    private final TransactionBrowseRepository transactions;

    public TransactionListService(TransactionBrowseRepository transactions) {
        this.transactions = transactions;
    }

    /** MAIN-PARA dispatch on EIBAID. */
    public TransactionListResponse handle(TransactionListRequest request, TransactionListState state) {
        return switch (request.actionOrEnter()) {
            case ENTER -> processEnterKey(request, state);
            case PF7 -> processPf7Key(state);
            case PF8 -> processPf8Key(state);
        };
    }

    /** PROCESS-ENTER-KEY. */
    private TransactionListResponse processEnterKey(
            TransactionListRequest request, TransactionListState state) {
        String message = null;

        String selectionFlag = trimToNull(request.getSelectionFlag());
        String selected = trimToNull(request.getSelectedTransactionId());
        if (selectionFlag != null && selected != null) {
            if (selectionFlag.equalsIgnoreCase("S")) {
                return TransactionListResponse.builder()
                        .success(true)
                        .pageNumber(state.getPageNumber())
                        .nextPageAvailable(state.isNextPageAvailable())
                        .transactions(List.of())
                        .nextProgram(TRANSACTION_VIEW_PROGRAM)
                        .selectedTransactionId(selected)
                        .build();
            }
            // COTRN00C only records the message here: the browse still runs and the page is shown.
            message = MSG_INVALID_SELECTION;
        }

        String filter = trimToNull(request.getTransactionIdFilter());
        String startKey;
        if (filter == null) {
            startKey = TransactionFormats.LOW_VALUES_KEY;
        } else if (isNumeric(filter)) {
            startKey = TransactionFormats.transactionId(Long.parseLong(filter));
        } else {
            return failure(state, MSG_TRAN_ID_NOT_NUMERIC);
        }

        state.setPageNumber(0);
        return processPageForward(state, startKey, false, message);
    }

    /** PROCESS-PF7-KEY. */
    private TransactionListResponse processPf7Key(TransactionListState state) {
        String key = state.getFirstTransactionId() == null || state.getFirstTransactionId().isBlank()
                ? TransactionFormats.LOW_VALUES_KEY
                : state.getFirstTransactionId();

        state.setNextPageAvailable(true);

        if (state.getPageNumber() > 1) {
            return processPageBackward(state, key);
        }
        return success(state, MSG_ALREADY_TOP, List.of());
    }

    /** PROCESS-PF8-KEY. */
    private TransactionListResponse processPf8Key(TransactionListState state) {
        String key = state.getLastTransactionId() == null || state.getLastTransactionId().isBlank()
                ? TransactionFormats.HIGH_VALUES_KEY
                : state.getLastTransactionId();

        if (state.isNextPageAvailable()) {
            return processPageForward(state, key, true, null);
        }
        return success(state, MSG_ALREADY_BOTTOM, List.of());
    }

    /**
     * PROCESS-PAGE-FORWARD. {@code skipPositionedRecord} reproduces the extra READNEXT performed
     * for every AID other than ENTER, PF7 and PF3, which steps past the record the browse is
     * positioned on (the last row of the page just shown).
     */
    private TransactionListResponse processPageForward(
            TransactionListState state, String startKey, boolean skipPositionedRecord, String message) {

        int reads = (skipPositionedRecord ? 1 : 0) + PAGE_SIZE + 1;
        List<Transaction> browsed =
                transactions.findByTransactionIdGreaterThanEqualOrderByTransactionIdAsc(
                        startKey, Limit.of(reads));
        if (skipPositionedRecord && !browsed.isEmpty()) {
            browsed = browsed.subList(1, browsed.size());
        }

        if (browsed.isEmpty()) {
            // STARTBR NOTFND: nothing at or beyond the key, the page number is left untouched.
            state.setNextPageAvailable(false);
            return success(state, MSG_TOP_OF_PAGE, List.of());
        }

        List<Transaction> page = browsed.subList(0, Math.min(PAGE_SIZE, browsed.size()));
        boolean hasNextPage = browsed.size() > PAGE_SIZE;

        state.setFirstTransactionId(page.get(0).getTransactionId());
        if (page.size() == PAGE_SIZE) {
            // CDEMO-CT00-TRNID-LAST is only moved on the tenth detail line.
            state.setLastTransactionId(page.get(PAGE_SIZE - 1).getTransactionId());
        }

        state.setPageNumber(state.getPageNumber() + 1);
        state.setNextPageAvailable(hasNextPage);
        if (!hasNextPage) {
            message = MSG_BOTTOM_REACHED;
        }

        return success(state, message, rows(page));
    }

    /**
     * PROCESS-PAGE-BACKWARD. PF7 always steps past the record the browse is positioned on (the
     * first row of the page just shown) before filling the ten detail lines from line ten upwards.
     */
    private TransactionListResponse processPageBackward(TransactionListState state, String key) {
        List<Transaction> browsed =
                transactions.findByTransactionIdLessThanEqualOrderByTransactionIdDesc(
                        key, Limit.of(1 + PAGE_SIZE + 1));
        if (!browsed.isEmpty()) {
            browsed = browsed.subList(1, browsed.size());
        }

        if (browsed.isEmpty()) {
            // READPREV ENDFILE before a single detail line was filled: the page number stands.
            return success(state, MSG_TOP_REACHED, List.of());
        }

        List<Transaction> descending = browsed.subList(0, Math.min(PAGE_SIZE, browsed.size()));
        boolean morePrevious = browsed.size() > PAGE_SIZE;

        // Detail line ten holds the first record read backwards, line one the last one.
        state.setLastTransactionId(descending.get(0).getTransactionId());
        if (descending.size() == PAGE_SIZE) {
            state.setFirstTransactionId(descending.get(PAGE_SIZE - 1).getTransactionId());
        }

        String message = null;
        if (descending.size() < PAGE_SIZE) {
            // The browse hit the start of the file, so the page number is left untouched.
            message = MSG_TOP_REACHED;
        } else if (morePrevious && state.getPageNumber() > 1) {
            state.setPageNumber(state.getPageNumber() - 1);
        } else {
            state.setPageNumber(1);
        }

        List<Transaction> ascending = new ArrayList<>(descending);
        Collections.reverse(ascending);
        return success(state, message, rows(ascending));
    }

    /** POPULATE-TRAN-DATA. */
    private List<TransactionListRow> rows(List<Transaction> page) {
        List<TransactionListRow> rows = new ArrayList<>(page.size());
        for (Transaction transaction : page) {
            rows.add(TransactionListRow.builder()
                    .transactionId(transaction.getTransactionId())
                    .date(TransactionFormats.listDate(transaction.getOriginTimestamp()))
                    .description(transaction.getDescription())
                    .amount(TransactionFormats.amount(transaction.getAmount()))
                    .build());
        }
        return rows;
    }

    private TransactionListResponse success(
            TransactionListState state, String message, List<TransactionListRow> rows) {
        return TransactionListResponse.builder()
                .success(true)
                .errorMessage(message)
                .pageNumber(state.getPageNumber())
                .nextPageAvailable(state.isNextPageAvailable())
                .transactions(rows)
                .build();
    }

    private TransactionListResponse failure(TransactionListState state, String message) {
        return TransactionListResponse.builder()
                .success(false)
                .errorMessage(message)
                .pageNumber(state.getPageNumber())
                .nextPageAvailable(state.isNextPageAvailable())
                .transactions(List.of())
                .build();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isNumeric(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }
}
