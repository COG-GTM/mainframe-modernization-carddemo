package com.carddemo.online.card;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import com.carddemo.repository.CardRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COCRDSLC — "Credit Card Detail" online transaction CCDL,
 * mapset COCRDSL, map CCRDSLA. File CARDDAT (CVACT02Y), work areas CVCRD01Y,
 * COMMAREA COCOM01Y.
 *
 * <p>Reproduces 2200-EDIT-MAP-INPUTS (2210-EDIT-ACCOUNT, 2220-EDIT-CARD) and
 * 9000-READ-DATA / 9100-GETCARD-BYACCTCARD, which reads CARDDAT on the card number
 * alone — the account id is only an input edit, exactly as in the COBOL.</p>
 */
@Service
public class CardDetailService {

    public static final String FOUND_CARDS_FOR_ACCOUNT = "   Displaying requested details";
    public static final String PROMPT_FOR_INPUT = "Please enter Account and Card Number";
    public static final String EXIT_MESSAGE = "PF03 pressed.Exiting";
    public static final String PROMPT_FOR_ACCT = "Account number not provided";
    public static final String PROMPT_FOR_CARD = "Card number not provided";
    public static final String NO_SEARCH_CRITERIA_RECEIVED = "No input received";
    public static final String ACCOUNT_FILTER_INVALID = "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER";
    public static final String CARD_FILTER_INVALID = "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER";
    public static final String DID_NOT_FIND_ACCT_IN_CARDXREF = "Did not find this account in cards database";
    public static final String DID_NOT_FIND_ACCTCARD_COMBO = "Did not find cards for this search condition";
    public static final String XREF_READ_ERROR = "Error reading Card Data File";

    private static final String F_ACCOUNT_ID = "accountId";
    private static final String F_CARD_NUMBER = "cardNumber";

    private final CardRepository cardRepository;

    public CardDetailService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /** 0000-MAIN of COCRDSLC for one turn of CCDL. */
    public CardDetailResponse view(CardDetailRequest request, CardDemoCommarea commarea) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        String returnMessage = null;

        if (request != null && CardDetailRequest.ACTION_PF3.equals(
                request.getAction() == null ? null : request.getAction().trim().toUpperCase())) {
            return exit(commarea);
        }

        String account = request == null ? null : CardWorkArea.normalizeKey(request.getAccountId());
        String card = request == null ? null : CardWorkArea.normalizeKey(request.getCardNumber());

        boolean accountBlank = account == null || CardWorkArea.isZeroes(account);
        boolean cardBlank = card == null || CardWorkArea.isZeroes(card);

        // 2210-EDIT-ACCOUNT
        if (accountBlank) {
            returnMessage = PROMPT_FOR_ACCT;
            fieldErrors.put(F_ACCOUNT_ID, PROMPT_FOR_ACCT);
        } else if (!CardWorkArea.isNumeric(account) || account.length() != 11) {
            fieldErrors.put(F_ACCOUNT_ID, ACCOUNT_FILTER_INVALID);
            returnMessage = ACCOUNT_FILTER_INVALID;
        }

        // 2220-EDIT-CARD
        if (cardBlank) {
            fieldErrors.put(F_CARD_NUMBER, PROMPT_FOR_CARD);
            if (returnMessage == null) {
                returnMessage = PROMPT_FOR_CARD;
            }
        } else if (!CardWorkArea.isNumeric(card) || card.length() != 16) {
            fieldErrors.put(F_CARD_NUMBER, CARD_FILTER_INVALID);
            if (returnMessage == null) {
                returnMessage = CARD_FILTER_INVALID;
            }
        }

        // Cross field edit: nothing at all was keyed.
        if (accountBlank && cardBlank) {
            returnMessage = NO_SEARCH_CRITERIA_RECEIVED;
        }

        if (!fieldErrors.isEmpty()) {
            return CardDetailResponse.builder()
                    .infoMessage(PROMPT_FOR_INPUT)
                    .errorMessage(returnMessage)
                    .fieldErrors(fieldErrors)
                    .accountId(account)
                    .cardNumber(card)
                    .nextProgram(CardWorkArea.DETAIL_PROGRAM)
                    .nextTransaction(CardWorkArea.DETAIL_TRANSACTION)
                    .nextMapset(CardWorkArea.DETAIL_MAPSET)
                    .nextMap(CardWorkArea.DETAIL_MAP)
                    .build();
        }

        Optional<Card> found = cardRepository.findById(card);
        if (found.isEmpty()) {
            fieldErrors.put(F_ACCOUNT_ID, DID_NOT_FIND_ACCTCARD_COMBO);
            fieldErrors.put(F_CARD_NUMBER, DID_NOT_FIND_ACCTCARD_COMBO);
            return CardDetailResponse.builder()
                    .infoMessage(PROMPT_FOR_INPUT)
                    .errorMessage(DID_NOT_FIND_ACCTCARD_COMBO)
                    .fieldErrors(fieldErrors)
                    .accountId(account)
                    .cardNumber(card)
                    .nextProgram(CardWorkArea.DETAIL_PROGRAM)
                    .nextTransaction(CardWorkArea.DETAIL_TRANSACTION)
                    .nextMapset(CardWorkArea.DETAIL_MAPSET)
                    .nextMap(CardWorkArea.DETAIL_MAP)
                    .build();
        }

        Card record = found.get();
        if (commarea != null) {
            commarea.setAccountId(record.getAccountId());
            commarea.setCardNumber(record.getCardNumber());
            commarea.setLastMapset(CardWorkArea.DETAIL_MAPSET);
            commarea.setLastMap(CardWorkArea.DETAIL_MAP);
        }
        String expiry = record.getExpirationDate() == null ? "" : record.getExpirationDate();
        return CardDetailResponse.builder()
                .infoMessage(FOUND_CARDS_FOR_ACCOUNT)
                .fieldErrors(fieldErrors)
                .cardFound(true)
                .accountId(record.getAccountId() == null ? null : String.format("%011d", record.getAccountId()))
                .cardNumber(record.getCardNumber())
                .embossedName(record.getEmbossedName())
                .expiryYear(expiry.length() >= 4 ? expiry.substring(0, 4) : null)
                .expiryMonth(expiry.length() >= 7 ? expiry.substring(5, 7) : null)
                .activeStatus(record.getActiveStatus())
                .nextProgram(CardWorkArea.DETAIL_PROGRAM)
                .nextTransaction(CardWorkArea.DETAIL_TRANSACTION)
                .nextMapset(CardWorkArea.DETAIL_MAPSET)
                .nextMap(CardWorkArea.DETAIL_MAP)
                .build();
    }

    /** PF03: back to the calling program, or the card list. */
    private CardDetailResponse exit(CardDemoCommarea commarea) {
        String target = CardWorkArea.LIST_PROGRAM;
        String transaction = CardWorkArea.LIST_TRANSACTION;
        if (commarea != null) {
            if (commarea.getFromProgram() != null && !commarea.getFromProgram().trim().isEmpty()) {
                target = commarea.getFromProgram();
                transaction = commarea.getFromTransactionId();
            }
            commarea.setToProgram(target);
            commarea.setToTransactionId(transaction);
            commarea.setFromProgram(CardWorkArea.DETAIL_PROGRAM);
            commarea.setFromTransactionId(CardWorkArea.DETAIL_TRANSACTION);
            commarea.setLastMapset(CardWorkArea.DETAIL_MAPSET);
            commarea.setLastMap(CardWorkArea.DETAIL_MAP);
        }
        return CardDetailResponse.builder()
                .infoMessage(PROMPT_FOR_INPUT)
                .errorMessage(EXIT_MESSAGE)
                .nextProgram(target)
                .nextTransaction(transaction)
                .build();
    }
}
