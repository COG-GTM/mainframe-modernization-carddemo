package com.carddemo.online.card;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COCRDLIC — "List Credit Cards" online transaction CCLI,
 * mapset COCRDLI, map CCRDLIA. File CARDDAT (CVACT02Y), work areas CVCRD01Y,
 * COMMAREA COCOM01Y.
 *
 * <p>Reproduces 2000-RECEIVE-MAP (2210-EDIT-ACCOUNT, 2220-EDIT-CARD, 2250-EDIT-ARRAY),
 * the paging decisions of 0000-MAIN, 9000-READ-FORWARD, 9100-READ-BACKWARDS,
 * 9500-FILTER-RECORDS and the message selection of 1400-SETUP-MESSAGE.</p>
 *
 * <p>One deviation: when a row is selected, COCRDLIC picks the account and card number
 * out of WS-SCREEN-DATA, which a fresh task has not filled in; here the page on display
 * is re-read from its first card key so that the selected row carries real data.</p>
 */
@Service
public class CardListService {

    public static final String INFORM_REC_ACTIONS = "TYPE S FOR DETAIL, U TO UPDATE ANY RECORD";
    public static final String EXIT_MESSAGE = "PF03 PRESSED.EXITING";
    public static final String NO_RECORDS_FOUND = "NO RECORDS FOUND FOR THIS SEARCH CONDITION.";
    public static final String MORE_THAN_1_ACTION = "PLEASE SELECT ONLY ONE RECORD TO VIEW OR UPDATE";
    public static final String INVALID_ACTION_CODE = "INVALID ACTION CODE";
    public static final String ACCOUNT_FILTER_INVALID = "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER";
    public static final String CARD_FILTER_INVALID = "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER";
    public static final String NO_PREVIOUS_PAGES = "NO PREVIOUS PAGES TO DISPLAY";
    public static final String NO_MORE_PAGES = "NO MORE PAGES TO DISPLAY";
    public static final String NO_MORE_RECORDS = "NO MORE RECORDS TO SHOW";

    /** DFHRESP(ENDFILE). */
    private static final int RESP_ENDFILE = 20;
    private static final int BROWSE_CHUNK = 50;

    private final CardBrowseRepository cardBrowseRepository;

    public CardListService(CardBrowseRepository cardBrowseRepository) {
        this.cardBrowseRepository = cardBrowseRepository;
    }

    /** 0000-MAIN for a single turn of CCLI. */
    public CardListResponse process(CardListRequest request, CardListState state, CardDemoCommarea commarea) {
        String action = resolveAid(request);
        if (CardListRequest.ACTION_PF3.equals(action)) {
            return exit(state, commarea);
        }

        if (!CardListRequest.ACTION_PF8.equals(action)) {
            state.setLastPageDisplayed(9);
        }

        Edits edits = editInputs(request);
        List<CardListRow> rows;

        if (edits.inputError) {
            rows = edits.accountFilterNotOk || edits.cardFilterNotOk
                    ? new ArrayList<>()
                    : readForward(state, edits, state.getFirstCardNumber());
        } else if (CardListRequest.ACTION_PF7.equals(action) && state.isFirstPage()) {
            rows = readForward(state, edits, state.getFirstCardNumber());
        } else if (CardListRequest.ACTION_PF8.equals(action) && state.isNextPageExists()) {
            String startKey = state.getLastCardNumber();
            state.setScreenNumber(state.getScreenNumber() + 1);
            rows = readForward(state, edits, startKey);
        } else if (CardListRequest.ACTION_PF7.equals(action) && !state.isFirstPage()) {
            state.setScreenNumber(state.getScreenNumber() - 1);
            rows = readBackwards(state, edits, state.getFirstCardNumber());
        } else {
            rows = readForward(state, edits, state.getFirstCardNumber());
            if (CardListRequest.ACTION_ENTER.equals(action) && edits.selectedRow > 0) {
                return select(rows, edits, state, commarea);
            }
        }

        applySelections(rows, edits);
        setupMessage(action, state, edits);
        return response(rows, state, edits, null, null, null);
    }

    private String resolveAid(CardListRequest request) {
        String action = request == null || request.getAction() == null
                ? CardListRequest.ACTION_ENTER
                : request.getAction().trim().toUpperCase();
        boolean valid = CardListRequest.ACTION_ENTER.equals(action)
                || CardListRequest.ACTION_PF3.equals(action)
                || CardListRequest.ACTION_PF7.equals(action)
                || CardListRequest.ACTION_PF8.equals(action);
        return valid ? action : CardListRequest.ACTION_ENTER;
    }

    /** 2200-EDIT-INPUTS. */
    private Edits editInputs(CardListRequest request) {
        Edits edits = new Edits();
        String account = request == null ? null : CardWorkArea.normalizeKey(request.getAccountIdFilter());
        String card = request == null ? null : CardWorkArea.normalizeKey(request.getCardNumberFilter());

        // 2210-EDIT-ACCOUNT
        if (account == null || CardWorkArea.isZeroes(account)) {
            edits.accountFilter = null;
        } else if (!CardWorkArea.isNumeric(account) || account.length() != 11) {
            edits.inputError = true;
            edits.accountFilterNotOk = true;
            edits.protectSelectRows = true;
            edits.errorMessage = ACCOUNT_FILTER_INVALID;
        } else {
            edits.accountFilter = account;
        }

        // 2220-EDIT-CARD
        if (card == null || CardWorkArea.isZeroes(card)) {
            edits.cardFilter = null;
        } else if (!CardWorkArea.isNumeric(card) || card.length() != 16) {
            edits.inputError = true;
            edits.cardFilterNotOk = true;
            edits.protectSelectRows = true;
            if (edits.errorMessage == null) {
                edits.errorMessage = CARD_FILTER_INVALID;
            }
        } else {
            edits.cardFilter = card;
        }

        edits.selections = new ArrayList<>();
        for (int i = 0; i < CardWorkArea.MAX_SCREEN_LINES; i++) {
            String value = request == null || request.getSelections() == null
                    || request.getSelections().size() <= i
                    ? null
                    : request.getSelections().get(i);
            edits.selections.add(value == null ? "" : value.trim().toUpperCase());
        }
        edits.selectionErrors = new boolean[CardWorkArea.MAX_SCREEN_LINES];

        // 2250-EDIT-ARRAY
        if (edits.inputError) {
            return edits;
        }
        long selected = edits.selections.stream().filter(s -> "S".equals(s) || "U".equals(s)).count();
        boolean moreThanOne = selected > 1;
        if (moreThanOne) {
            edits.inputError = true;
            edits.errorMessage = MORE_THAN_1_ACTION;
        }
        for (int i = 0; i < CardWorkArea.MAX_SCREEN_LINES; i++) {
            String value = edits.selections.get(i);
            if ("S".equals(value) || "U".equals(value)) {
                edits.selectedRow = i + 1;
                edits.selectedAction = value;
                if (moreThanOne) {
                    edits.selectionErrors[i] = true;
                }
            } else if (!value.isEmpty()) {
                edits.inputError = true;
                edits.selectionErrors[i] = true;
                if (edits.errorMessage == null) {
                    edits.errorMessage = INVALID_ACTION_CODE;
                }
            }
        }
        return edits;
    }

    /** 9000-READ-FORWARD: STARTBR GTEQ, READNEXT up to seven filtered rows, then probe. */
    private List<CardListRow> readForward(CardListState state, Edits edits, String startKey) {
        List<CardListRow> rows = new ArrayList<>();
        String key = startKey == null ? "" : startKey;
        state.setNextPageExists(true);

        String cursor = key;
        boolean strictlyAfterCursor = false;
        boolean endOfFile = false;
        Card lastRead = null;
        while (rows.size() < CardWorkArea.MAX_SCREEN_LINES && !endOfFile) {
            List<Card> chunk = strictlyAfterCursor
                    ? cardBrowseRepository.findByCardNumberGreaterThanOrderByCardNumberAsc(cursor,
                            PageRequest.of(0, BROWSE_CHUNK))
                    : cardBrowseRepository.findByCardNumberGreaterThanEqualOrderByCardNumberAsc(cursor,
                            PageRequest.of(0, BROWSE_CHUNK));
            if (chunk.isEmpty()) {
                endOfFile = true;
                break;
            }
            for (Card card : chunk) {
                lastRead = card;
                if (!excluded(card, edits)) {
                    rows.add(row(rows.size() + 1, card));
                    if (rows.size() == 1) {
                        state.setFirstCardNumber(card.getCardNumber());
                        state.setFirstCardAccountId(accountId(card));
                        if (state.getScreenNumber() == 0) {
                            state.setScreenNumber(1);
                        }
                    }
                    if (rows.size() == CardWorkArea.MAX_SCREEN_LINES) {
                        break;
                    }
                }
            }
            if (rows.size() < CardWorkArea.MAX_SCREEN_LINES) {
                cursor = lastRead.getCardNumber();
                strictlyAfterCursor = true;
                if (chunk.size() < BROWSE_CHUNK) {
                    endOfFile = true;
                }
            }
        }

        if (rows.size() == CardWorkArea.MAX_SCREEN_LINES) {
            CardListRow last = rows.get(rows.size() - 1);
            state.setLastCardNumber(last.getCardNumber());
            state.setLastCardAccountId(last.getAccountId());
            // The probe READNEXT of COCRDLIC is not filtered.
            List<Card> probe = cardBrowseRepository.findByCardNumberGreaterThanOrderByCardNumberAsc(
                    last.getCardNumber(), PageRequest.of(0, 1));
            if (probe.isEmpty()) {
                state.setNextPageExists(false);
                if (edits.errorMessage == null) {
                    edits.errorMessage = NO_MORE_RECORDS;
                }
            } else {
                state.setNextPageExists(true);
                state.setLastCardNumber(probe.get(0).getCardNumber());
                state.setLastCardAccountId(accountId(probe.get(0)));
            }
            return rows;
        }

        // ENDFILE reached before the screen was filled.
        state.setNextPageExists(false);
        if (lastRead != null) {
            state.setLastCardNumber(lastRead.getCardNumber());
            state.setLastCardAccountId(accountId(lastRead));
        }
        if (edits.errorMessage == null) {
            edits.errorMessage = NO_MORE_RECORDS;
        }
        if (state.getScreenNumber() == 1 && rows.isEmpty()) {
            edits.errorMessage = NO_RECORDS_FOUND;
            edits.noRecordsFound = true;
        }
        return rows;
    }

    /** 9100-READ-BACKWARDS: READPREV from the first key of the page on display. */
    private List<CardListRow> readBackwards(CardListState state, Edits edits, String startKey) {
        String key = startKey == null ? "" : startKey;
        state.setLastCardNumber(key);
        state.setNextPageExists(true);

        List<Card> collected = new ArrayList<>();
        String cursor = key;
        boolean startOfFile = false;
        Card lastRead = null;
        while (collected.size() < CardWorkArea.MAX_SCREEN_LINES && !startOfFile) {
            List<Card> chunk = cardBrowseRepository
                    .findByCardNumberLessThanOrderByCardNumberDesc(cursor, PageRequest.of(0, BROWSE_CHUNK));
            if (chunk.isEmpty()) {
                startOfFile = true;
                break;
            }
            for (Card card : chunk) {
                lastRead = card;
                if (!excluded(card, edits)) {
                    collected.add(card);
                    if (collected.size() == CardWorkArea.MAX_SCREEN_LINES) {
                        break;
                    }
                }
            }
            if (collected.size() < CardWorkArea.MAX_SCREEN_LINES) {
                cursor = lastRead.getCardNumber();
                if (chunk.size() < BROWSE_CHUNK) {
                    startOfFile = true;
                }
            }
        }

        if (collected.size() == CardWorkArea.MAX_SCREEN_LINES) {
            Card first = collected.get(collected.size() - 1);
            state.setFirstCardNumber(first.getCardNumber());
            state.setFirstCardAccountId(accountId(first));
        } else if (edits.errorMessage == null) {
            // READPREV hit the start of the file: COCRDLIC reports it as a file error.
            edits.errorMessage = CardWorkArea.fileErrorMessage("READ", CardWorkArea.CARD_FILE, RESP_ENDFILE, 0);
        }

        Collections.reverse(collected);
        List<CardListRow> rows = new ArrayList<>();
        for (Card card : collected) {
            rows.add(row(rows.size() + 1, card));
        }
        return rows;
    }

    /** 9500-FILTER-RECORDS. */
    private boolean excluded(Card card, Edits edits) {
        if (edits.accountFilter != null
                && (card.getAccountId() == null
                        || Long.parseLong(edits.accountFilter) != card.getAccountId())) {
            return true;
        }
        return edits.cardFilter != null && !edits.cardFilter.equals(card.getCardNumber());
    }

    /** 1400-SETUP-MESSAGE. */
    private void setupMessage(String action, CardListState state, Edits edits) {
        if (edits.accountFilterNotOk || edits.cardFilterNotOk) {
            return;
        }
        if (CardListRequest.ACTION_PF7.equals(action) && state.isFirstPage()) {
            edits.errorMessage = NO_PREVIOUS_PAGES;
        } else if (CardListRequest.ACTION_PF8.equals(action) && !state.isNextPageExists()
                && state.isLastPageShown()) {
            edits.errorMessage = NO_MORE_PAGES;
        } else if (CardListRequest.ACTION_PF8.equals(action) && !state.isNextPageExists()) {
            edits.infoMessage = INFORM_REC_ACTIONS;
            state.setLastPageDisplayed(0);
        } else if (state.isNextPageExists() || edits.infoMessage == null) {
            edits.infoMessage = INFORM_REC_ACTIONS;
        }
        if (edits.noRecordsFound) {
            edits.infoMessage = null;
        }
    }

    /** XCTL to COCRDSLC ('S') or COCRDUPC ('U'). */
    private CardListResponse select(List<CardListRow> rows, Edits edits, CardListState state,
            CardDemoCommarea commarea) {
        CardListRow selectedRow = edits.selectedRow <= rows.size() ? rows.get(edits.selectedRow - 1) : null;
        boolean view = "S".equals(edits.selectedAction);
        String program = view ? CardWorkArea.DETAIL_PROGRAM : CardWorkArea.UPDATE_PROGRAM;
        String transaction = view ? CardWorkArea.DETAIL_TRANSACTION : CardWorkArea.UPDATE_TRANSACTION;
        String mapset = view ? CardWorkArea.DETAIL_MAPSET : CardWorkArea.UPDATE_MAPSET;
        String map = view ? CardWorkArea.DETAIL_MAP : CardWorkArea.UPDATE_MAP;

        applySelections(rows, edits);
        if (commarea != null) {
            commarea.setFromTransactionId(CardWorkArea.LIST_TRANSACTION);
            commarea.setFromProgram(CardWorkArea.LIST_PROGRAM);
            commarea.setToProgram(program);
            commarea.setToTransactionId(transaction);
            commarea.setLastMapset(CardWorkArea.LIST_MAPSET);
            commarea.setLastMap(CardWorkArea.LIST_MAP);
            if (selectedRow != null) {
                commarea.setAccountId(selectedRow.getAccountId() == null
                        ? null : Long.parseLong(selectedRow.getAccountId()));
                commarea.setCardNumber(selectedRow.getCardNumber());
            }
        }
        CardListResponse response = response(rows, state, edits, program, transaction, selectedRow);
        response.setNextMapset(mapset);
        response.setNextMap(map);
        return response;
    }

    private CardListResponse exit(CardListState state, CardDemoCommarea commarea) {
        // INITIALIZE WS-THIS-PROGCOMMAREA, SET CA-FIRST-PAGE / CA-LAST-PAGE-NOT-SHOWN.
        state.setFirstCardNumber("");
        state.setLastCardNumber("");
        state.setScreenNumber(1);
        state.setLastPageDisplayed(9);
        state.setNextPageExists(false);
        if (commarea != null) {
            commarea.setFromTransactionId(CardWorkArea.LIST_TRANSACTION);
            commarea.setFromProgram(CardWorkArea.LIST_PROGRAM);
            commarea.setToProgram(CardWorkArea.MENU_PROGRAM);
            commarea.setToTransactionId(CardWorkArea.MENU_TRANSACTION);
            commarea.setLastMapset(CardWorkArea.LIST_MAPSET);
            commarea.setLastMap(CardWorkArea.LIST_MAP);
        }
        return CardListResponse.builder()
                .rows(new ArrayList<>())
                .pageNumber(state.getScreenNumber())
                .errorMessage(EXIT_MESSAGE)
                .nextProgram(CardWorkArea.MENU_PROGRAM)
                .nextTransaction(CardWorkArea.MENU_TRANSACTION)
                .build();
    }

    private CardListResponse response(List<CardListRow> rows, CardListState state, Edits edits,
            String nextProgram, String nextTransaction, CardListRow selectedRow) {
        return CardListResponse.builder()
                .rows(rows)
                .pageNumber(state.getScreenNumber())
                .nextPageExists(state.isNextPageExists())
                .infoMessage(edits.infoMessage)
                .errorMessage(edits.errorMessage)
                .selectRowsProtected(edits.protectSelectRows)
                .nextProgram(nextProgram == null ? CardWorkArea.LIST_PROGRAM : nextProgram)
                .nextTransaction(nextTransaction == null ? CardWorkArea.LIST_TRANSACTION : nextTransaction)
                .nextMapset(CardWorkArea.LIST_MAPSET)
                .nextMap(CardWorkArea.LIST_MAP)
                .selectedAccountId(selectedRow == null ? null : selectedRow.getAccountId())
                .selectedCardNumber(selectedRow == null ? null : selectedRow.getCardNumber())
                .build();
    }

    private void applySelections(List<CardListRow> rows, Edits edits) {
        for (CardListRow row : rows) {
            int index = row.getRowNumber() - 1;
            row.setSelection(edits.selections.get(index));
            row.setSelectionError(edits.selectionErrors[index]);
        }
    }

    private CardListRow row(int rowNumber, Card card) {
        return CardListRow.builder()
                .rowNumber(rowNumber)
                .accountId(accountId(card))
                .cardNumber(card.getCardNumber())
                .activeStatus(card.getActiveStatus())
                .build();
    }

    private static String accountId(Card card) {
        return card.getAccountId() == null ? "" : String.format("%011d", card.getAccountId());
    }

    /** Working storage of one turn: the edit flags of 2200-EDIT-INPUTS and the messages. */
    private static final class Edits {
        private boolean inputError;
        private boolean accountFilterNotOk;
        private boolean cardFilterNotOk;
        private boolean protectSelectRows;
        private boolean noRecordsFound;
        private String accountFilter;
        private String cardFilter;
        private String errorMessage;
        private String infoMessage;
        private List<String> selections;
        private boolean[] selectionErrors;
        private int selectedRow;
        private String selectedAction;
    }
}
