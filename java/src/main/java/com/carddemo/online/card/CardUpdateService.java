package com.carddemo.online.card;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import com.carddemo.repository.CardRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: COCRDUPC — "Credit Card Update" online transaction CCUP, mapset
 * COCRDUP, map CCRDUPA. File CARDDAT (CVACT02Y), work areas CVCRD01Y, COMMAREA
 * COCOM01Y, customer layout CVCUS01Y.
 *
 * <p>Reproduces 1200-EDIT-MAP-INPUTS (1210-EDIT-ACCOUNT, 1220-EDIT-CARD, 1230-EDIT-NAME,
 * 1240-EDIT-CARDSTATUS, 1250-EDIT-EXPIRY-MON, 1260-EDIT-EXPIRY-YEAR), the state machine
 * of 2000-DECIDE-ACTION, the information messages of 3250-SETUP-INFOMSG and the
 * read-for-update / re-compare / rewrite sequence of 9200-WRITE-PROCESSING and
 * 9300-CHECK-CHANGE-IN-REC.</p>
 */
@Service
public class CardUpdateService {

    public static final String FOUND_CARDS_FOR_ACCOUNT = "Details of selected card shown above";
    public static final String PROMPT_FOR_SEARCH_KEYS = "Please enter Account and Card Number";
    public static final String PROMPT_FOR_CHANGES = "Update card details presented above.";
    public static final String PROMPT_FOR_CONFIRMATION = "Changes validated.Press F5 to save";
    public static final String CONFIRM_UPDATE_SUCCESS = "Changes committed to database";
    public static final String INFORM_FAILURE = "Changes unsuccessful. Please try again";

    public static final String EXIT_MESSAGE = "PF03 pressed.Exiting";
    public static final String PROMPT_FOR_ACCT = "Account number not provided";
    public static final String PROMPT_FOR_CARD = "Card number not provided";
    public static final String PROMPT_FOR_NAME = "Card name not provided";
    public static final String NAME_MUST_BE_ALPHA = "Card name can only contain alphabets and spaces";
    public static final String NO_SEARCH_CRITERIA_RECEIVED = "No input received";
    public static final String NO_CHANGES_DETECTED = "No change detected with respect to values fetched.";
    public static final String ACCOUNT_FILTER_INVALID = "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER";
    public static final String CARD_FILTER_INVALID = "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER";
    public static final String CARD_STATUS_MUST_BE_YES_NO = "Card Active Status must be Y or N";
    public static final String CARD_EXPIRY_MONTH_NOT_VALID = "Card expiry month must be between 1 and 12";
    public static final String CARD_EXPIRY_YEAR_NOT_VALID = "Invalid card expiry year";
    public static final String DID_NOT_FIND_ACCTCARD_COMBO = "Did not find cards for this search condition";
    public static final String COULD_NOT_LOCK_FOR_UPDATE = "Could not lock record for update";
    public static final String DATA_WAS_CHANGED_BEFORE_UPDATE = "Record changed by some one else. Please review";
    public static final String LOCKED_BUT_UPDATE_FAILED = "Update of record failed";

    private static final String F_ACCOUNT_ID = "accountId";
    private static final String F_CARD_NUMBER = "cardNumber";
    private static final String F_CARD_NAME = "cardName";
    private static final String F_ACTIVE_STATUS = "activeStatus";
    private static final String F_EXPIRY_MONTH = "expiryMonth";
    private static final String F_EXPIRY_YEAR = "expiryYear";

    private final CardRepository cardRepository;

    public CardUpdateService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /** 0000-MAIN of COCRDUPC for one turn of CCUP. */
    @Transactional
    public CardUpdateResponse process(CardUpdateRequest request, CardUpdateState state, CardDemoCommarea commarea) {
        String action = resolveAid(request, state);
        Turn turn = new Turn();

        boolean cameFromList = commarea != null && CardWorkArea.LIST_MAPSET.equals(commarea.getLastMapset());
        if (CardUpdateRequest.ACTION_PF3.equals(action)
                || ((CardUpdateState.CHANGES_OKAYED_AND_DONE.equals(state.getChangeAction())
                        || state.isChangesFailed()) && cameFromList)) {
            return exit(state, commarea);
        }

        if (CardUpdateState.CHANGES_OKAYED_AND_DONE.equals(state.getChangeAction()) || state.isChangesFailed()) {
            // Changes are done (or failed): start over and ask for fresh search keys.
            state.setChangeAction(CardUpdateState.DETAILS_NOT_FETCHED);
            state.setOldDetails(null);
            state.setNewDetails(null);
            turn.infoMessage = PROMPT_FOR_SEARCH_KEYS;
            return response(state, turn, null);
        }

        CardUpdateData input = receiveMap(request);
        editMapInputs(state, input, turn);
        decideAction(action, state, input, turn, commarea);

        CardUpdateData shown = state.isDetailsNotFetched() || state.getNewDetails() == null
                ? state.getOldDetails()
                : state.getNewDetails();
        turn.infoMessage = infoMessage(state, turn);
        return response(state, turn, shown);
    }

    /** AID validation: PF5 only when changes are pending confirmation, PF12 only after a fetch. */
    private String resolveAid(CardUpdateRequest request, CardUpdateState state) {
        String action = request == null || request.getAction() == null
                ? CardUpdateRequest.ACTION_ENTER
                : request.getAction().trim().toUpperCase();
        boolean valid = CardUpdateRequest.ACTION_ENTER.equals(action)
                || CardUpdateRequest.ACTION_PF3.equals(action)
                || (CardUpdateRequest.ACTION_PF5.equals(action)
                        && CardUpdateState.CHANGES_OK_NOT_CONFIRMED.equals(state.getChangeAction()))
                || (CardUpdateRequest.ACTION_PF12.equals(action) && !state.isDetailsNotFetched());
        return valid ? action : CardUpdateRequest.ACTION_ENTER;
    }

    /** 1100-RECEIVE-MAP: '*' and spaces are LOW-VALUES. */
    private CardUpdateData receiveMap(CardUpdateRequest request) {
        if (request == null) {
            return CardUpdateData.builder().build();
        }
        return CardUpdateData.builder()
                .accountId(CardWorkArea.normalizeKey(request.getAccountId()))
                .cardNumber(CardWorkArea.normalizeKey(request.getCardNumber()))
                .embossedName(CardWorkArea.normalizeKey(request.getCardName()))
                .activeStatus(CardWorkArea.normalizeKey(request.getActiveStatus()))
                .expiryMonth(CardWorkArea.normalizeKey(request.getExpiryMonth()))
                .expiryYear(CardWorkArea.normalizeKey(request.getExpiryYear()))
                .build();
    }

    /** 1200-EDIT-MAP-INPUTS. */
    private void editMapInputs(CardUpdateState state, CardUpdateData input, Turn turn) {
        if (state.isDetailsNotFetched()) {
            editAccount(input, turn);
            editCard(input, turn);
            if (turn.accountFilterBlank && turn.cardFilterBlank) {
                turn.returnMessage = NO_SEARCH_CRITERIA_RECEIVED;
            }
            return;
        }

        CardUpdateData old = state.getOldDetails();
        input.setAccountId(old == null ? input.getAccountId() : old.getAccountId());
        input.setCardNumber(old == null ? input.getCardNumber() : old.getCardNumber());
        input.setCvvCode(old == null ? null : old.getCvvCode());
        input.setExpiryDay(old == null ? null : old.getExpiryDay());
        state.setNewDetails(input);

        if (input.sameAs(old)) {
            turn.noChangesDetected = true;
            turn.returnMessage = NO_CHANGES_DETECTED;
        }

        if (turn.noChangesDetected
                || CardUpdateState.CHANGES_OK_NOT_CONFIRMED.equals(state.getChangeAction())
                || CardUpdateState.CHANGES_OKAYED_AND_DONE.equals(state.getChangeAction())) {
            return;
        }

        state.setChangeAction(CardUpdateState.CHANGES_NOT_OK);
        editName(input, turn);
        editCardStatus(input, turn);
        editExpiryMonth(input, turn);
        editExpiryYear(input, turn);
        if (!turn.inputError) {
            state.setChangeAction(CardUpdateState.CHANGES_OK_NOT_CONFIRMED);
        }
    }

    /** 1210-EDIT-ACCOUNT. */
    private void editAccount(CardUpdateData input, Turn turn) {
        String account = input.getAccountId();
        if (account == null || CardWorkArea.isZeroes(account)) {
            turn.inputError = true;
            turn.accountFilterBlank = true;
            turn.addError(F_ACCOUNT_ID, PROMPT_FOR_ACCT);
            input.setAccountId(null);
        } else if (!CardWorkArea.isNumeric(account) || account.length() != 11) {
            turn.inputError = true;
            turn.accountFilterNotOk = true;
            turn.addError(F_ACCOUNT_ID, ACCOUNT_FILTER_INVALID);
            input.setAccountId(null);
        } else {
            turn.accountFilterValid = true;
        }
    }

    /** 1220-EDIT-CARD. */
    private void editCard(CardUpdateData input, Turn turn) {
        String card = input.getCardNumber();
        if (card == null || CardWorkArea.isZeroes(card)) {
            turn.inputError = true;
            turn.cardFilterBlank = true;
            turn.addError(F_CARD_NUMBER, PROMPT_FOR_CARD);
            input.setCardNumber(null);
        } else if (!CardWorkArea.isNumeric(card) || card.length() != 16) {
            turn.inputError = true;
            turn.addError(F_CARD_NUMBER, CARD_FILTER_INVALID);
            input.setCardNumber(null);
        } else {
            turn.cardFilterValid = true;
        }
    }

    /** 1230-EDIT-NAME: alphabets and spaces only. */
    private void editName(CardUpdateData input, Turn turn) {
        String name = input.getEmbossedName();
        if (name == null || name.isBlank() || CardWorkArea.isZeroes(name)) {
            turn.inputError = true;
            turn.addError(F_CARD_NAME, PROMPT_FOR_NAME);
            return;
        }
        if (!name.chars().allMatch(c -> Character.isLetter(c) || c == ' ')) {
            turn.inputError = true;
            turn.addError(F_CARD_NAME, NAME_MUST_BE_ALPHA);
        }
    }

    /** 1240-EDIT-CARDSTATUS: FLG-YES-NO-VALID. */
    private void editCardStatus(CardUpdateData input, Turn turn) {
        String status = input.getActiveStatus();
        if (status == null || status.isBlank() || CardWorkArea.isZeroes(status)) {
            turn.inputError = true;
            turn.addError(F_ACTIVE_STATUS, CARD_STATUS_MUST_BE_YES_NO);
            return;
        }
        if (!"Y".equalsIgnoreCase(status) && !"N".equalsIgnoreCase(status)) {
            turn.inputError = true;
            turn.addError(F_ACTIVE_STATUS, CARD_STATUS_MUST_BE_YES_NO);
        }
    }

    /** 1250-EDIT-EXPIRY-MON: 88 VALID-MONTH VALUES 1 THRU 12. */
    private void editExpiryMonth(CardUpdateData input, Turn turn) {
        String month = input.getExpiryMonth();
        if (month == null || month.isBlank() || CardWorkArea.isZeroes(month)) {
            turn.inputError = true;
            turn.addError(F_EXPIRY_MONTH, CARD_EXPIRY_MONTH_NOT_VALID);
            return;
        }
        if (!CardWorkArea.isNumeric(month) || Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
            turn.inputError = true;
            turn.addError(F_EXPIRY_MONTH, CARD_EXPIRY_MONTH_NOT_VALID);
        }
    }

    /** 1260-EDIT-EXPIRY-YEAR: 88 VALID-YEAR VALUES 1950 THRU 2099. */
    private void editExpiryYear(CardUpdateData input, Turn turn) {
        String year = input.getExpiryYear();
        if (year == null || year.isBlank() || CardWorkArea.isZeroes(year)) {
            turn.inputError = true;
            turn.addError(F_EXPIRY_YEAR, CARD_EXPIRY_YEAR_NOT_VALID);
            return;
        }
        if (!CardWorkArea.isNumeric(year) || Integer.parseInt(year) < 1950 || Integer.parseInt(year) > 2099) {
            turn.inputError = true;
            turn.addError(F_EXPIRY_YEAR, CARD_EXPIRY_YEAR_NOT_VALID);
        }
    }

    /** 2000-DECIDE-ACTION. */
    private void decideAction(String action, CardUpdateState state, CardUpdateData input, Turn turn,
            CardDemoCommarea commarea) {
        if (state.isDetailsNotFetched() || CardUpdateRequest.ACTION_PF12.equals(action)) {
            if (turn.accountFilterValid && turn.cardFilterValid) {
                readData(state, input, turn, commarea);
                if (turn.foundCard) {
                    state.setChangeAction(CardUpdateState.SHOW_DETAILS);
                    state.setNewDetails(null);
                }
            } else if (CardUpdateRequest.ACTION_PF12.equals(action)) {
                readData(state, state.getOldDetails(), turn, commarea);
                if (turn.foundCard) {
                    state.setChangeAction(CardUpdateState.SHOW_DETAILS);
                    state.setNewDetails(null);
                }
            }
            return;
        }
        if (CardUpdateState.SHOW_DETAILS.equals(state.getChangeAction())) {
            if (!turn.inputError && !turn.noChangesDetected) {
                state.setChangeAction(CardUpdateState.CHANGES_OK_NOT_CONFIRMED);
            }
            return;
        }
        if (CardUpdateState.CHANGES_OK_NOT_CONFIRMED.equals(state.getChangeAction())
                && CardUpdateRequest.ACTION_PF5.equals(action)) {
            writeProcessing(state, turn);
            if (turn.couldNotLock) {
                state.setChangeAction(CardUpdateState.CHANGES_OKAYED_LOCK_ERROR);
            } else if (turn.updateFailed) {
                state.setChangeAction(CardUpdateState.CHANGES_OKAYED_BUT_FAILED);
            } else if (turn.dataWasChanged) {
                state.setChangeAction(CardUpdateState.SHOW_DETAILS);
            } else {
                state.setChangeAction(CardUpdateState.CHANGES_OKAYED_AND_DONE);
            }
        }
    }

    /** 9000-READ-DATA / 9100-GETCARD-BYACCTCARD: CARDDAT is read on the card number. */
    private void readData(CardUpdateState state, CardUpdateData input, Turn turn, CardDemoCommarea commarea) {
        if (input == null || input.getCardNumber() == null) {
            turn.inputError = true;
            turn.addError(F_CARD_NUMBER, DID_NOT_FIND_ACCTCARD_COMBO);
            return;
        }
        Optional<Card> found = cardRepository.findById(input.getCardNumber());
        if (found.isEmpty()) {
            turn.inputError = true;
            turn.addError(F_ACCOUNT_ID, DID_NOT_FIND_ACCTCARD_COMBO);
            turn.addError(F_CARD_NUMBER, DID_NOT_FIND_ACCTCARD_COMBO);
            return;
        }
        turn.foundCard = true;
        CardUpdateData old = CardUpdateData.fromEntity(found.get());
        old.setAccountId(input.getAccountId() == null ? old.getAccountId() : input.getAccountId());
        state.setOldDetails(old);
        if (commarea != null) {
            commarea.setAccountId(found.get().getAccountId());
            commarea.setCardNumber(found.get().getCardNumber());
            commarea.setLastMapset(CardWorkArea.UPDATE_MAPSET);
            commarea.setLastMap(CardWorkArea.UPDATE_MAP);
        }
    }

    /** 9200-WRITE-PROCESSING with the re-compare of 9300-CHECK-CHANGE-IN-REC. */
    private void writeProcessing(CardUpdateState state, Turn turn) {
        CardUpdateData updated = state.getNewDetails();
        if (updated == null || updated.getCardNumber() == null) {
            turn.couldNotLock = true;
            turn.returnMessage = COULD_NOT_LOCK_FOR_UPDATE;
            return;
        }
        Optional<Card> locked = cardRepository.findById(updated.getCardNumber());
        if (locked.isEmpty()) {
            turn.couldNotLock = true;
            turn.returnMessage = COULD_NOT_LOCK_FOR_UPDATE;
            return;
        }

        Card card = locked.get();
        CardUpdateData persisted = CardUpdateData.fromEntity(card);
        CardUpdateData old = state.getOldDetails();
        boolean unchanged = old != null
                && equalsExact(persisted.getCvvCode(), old.getCvvCode())
                && equalsExact(persisted.getEmbossedName(), old.getEmbossedName())
                && equalsExact(persisted.getExpiryYear(), old.getExpiryYear())
                && equalsExact(persisted.getExpiryMonth(), old.getExpiryMonth())
                && equalsExact(persisted.getExpiryDay(), old.getExpiryDay())
                && equalsExact(persisted.getActiveStatus(), old.getActiveStatus());
        if (!unchanged) {
            turn.dataWasChanged = true;
            turn.returnMessage = DATA_WAS_CHANGED_BEFORE_UPDATE;
            state.setOldDetails(persisted);
            return;
        }

        card.setEmbossedName(updated.getEmbossedName());
        card.setExpirationDate(updated.expirationDate());
        card.setActiveStatus(updated.getActiveStatus());
        try {
            cardRepository.save(card);
        } catch (RuntimeException failure) {
            turn.updateFailed = true;
            turn.returnMessage = LOCKED_BUT_UPDATE_FAILED;
            return;
        }
        state.setOldDetails(CardUpdateData.fromEntity(card));
    }

    /** 3250-SETUP-INFOMSG. */
    private String infoMessage(CardUpdateState state, Turn turn) {
        if (state.isDetailsNotFetched()) {
            return PROMPT_FOR_SEARCH_KEYS;
        }
        switch (state.getChangeAction()) {
            case CardUpdateState.SHOW_DETAILS:
                return FOUND_CARDS_FOR_ACCOUNT;
            case CardUpdateState.CHANGES_NOT_OK:
                return PROMPT_FOR_CHANGES;
            case CardUpdateState.CHANGES_OK_NOT_CONFIRMED:
                return PROMPT_FOR_CONFIRMATION;
            case CardUpdateState.CHANGES_OKAYED_AND_DONE:
                return CONFIRM_UPDATE_SUCCESS;
            case CardUpdateState.CHANGES_OKAYED_LOCK_ERROR:
            case CardUpdateState.CHANGES_OKAYED_BUT_FAILED:
                return INFORM_FAILURE;
            default:
                return PROMPT_FOR_SEARCH_KEYS;
        }
    }

    /** PF03, or a finished update reached from the card list: XCTL back to the caller. */
    private CardUpdateResponse exit(CardUpdateState state, CardDemoCommarea commarea) {
        String target = CardWorkArea.MENU_PROGRAM;
        String transaction = CardWorkArea.MENU_TRANSACTION;
        if (commarea != null) {
            if (commarea.getFromProgram() != null && !commarea.getFromProgram().trim().isEmpty()) {
                target = commarea.getFromProgram();
            }
            if (commarea.getFromTransactionId() != null && !commarea.getFromTransactionId().trim().isEmpty()) {
                transaction = commarea.getFromTransactionId();
            }
            commarea.setToProgram(target);
            commarea.setToTransactionId(transaction);
            commarea.setFromProgram(CardWorkArea.UPDATE_PROGRAM);
            commarea.setFromTransactionId(CardWorkArea.UPDATE_TRANSACTION);
            if (CardWorkArea.LIST_MAPSET.equals(commarea.getLastMapset())) {
                commarea.setAccountId(null);
                commarea.setCardNumber(null);
            }
            commarea.setLastMapset(CardWorkArea.UPDATE_MAPSET);
            commarea.setLastMap(CardWorkArea.UPDATE_MAP);
        }
        state.setChangeAction(CardUpdateState.DETAILS_NOT_FETCHED);
        state.setOldDetails(null);
        state.setNewDetails(null);
        return CardUpdateResponse.builder()
                .changeAction(state.getChangeAction())
                .infoMessage(PROMPT_FOR_SEARCH_KEYS)
                .errorMessage(EXIT_MESSAGE)
                .fieldErrors(new LinkedHashMap<>())
                .nextProgram(target)
                .nextTransaction(transaction)
                .build();
    }

    private CardUpdateResponse response(CardUpdateState state, Turn turn, CardUpdateData shown) {
        return CardUpdateResponse.builder()
                .changeAction(state.getChangeAction())
                .infoMessage(turn.infoMessage)
                .errorMessage(turn.returnMessage)
                .fieldErrors(turn.fieldErrors)
                .data(shown)
                .nextProgram(CardWorkArea.UPDATE_PROGRAM)
                .nextTransaction(CardWorkArea.UPDATE_TRANSACTION)
                .nextMapset(CardWorkArea.UPDATE_MAPSET)
                .nextMap(CardWorkArea.UPDATE_MAP)
                .build();
    }

    private static boolean equalsExact(String left, String right) {
        return (left == null ? "" : left).equals(right == null ? "" : right);
    }

    /** Working storage of one turn: the FLG-x flags plus WS-RETURN-MSG / WS-INFO-MSG. */
    private static final class Turn {
        private boolean inputError;
        private boolean accountFilterBlank;
        private boolean accountFilterNotOk;
        private boolean accountFilterValid;
        private boolean cardFilterBlank;
        private boolean cardFilterValid;
        private boolean noChangesDetected;
        private boolean foundCard;
        private boolean couldNotLock;
        private boolean dataWasChanged;
        private boolean updateFailed;
        private String returnMessage;
        private String infoMessage;
        private final Map<String, String> fieldErrors = new LinkedHashMap<>();

        private void addError(String field, String message) {
            fieldErrors.putIfAbsent(field, message);
            if (returnMessage == null) {
                returnMessage = message;
            }
        }
    }
}
