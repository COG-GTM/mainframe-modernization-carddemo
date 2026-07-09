package com.carddemo.web.card;

import org.springframework.stereotype.Component;

import com.carddemo.service.card.CardMessages;
import com.carddemo.service.card.CardService;
import com.carddemo.service.card.CardValidationException;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.CommonAction;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.session.UserType;
import com.carddemo.web.card.dto.CardListResponse;

/**
 * Navigation-framework handler for the card-list screen ({@code COCRDLI} / {@code COCRDLIC}).
 *
 * <p>Reproduces the online program's turn: PF3 returns to the caller, PF7/PF8 page the browse
 * backward/forward, and ENTER either re-lists or — when a row's selection code is set —
 * transfers to the detail ({@code S}) or update ({@code U}) screen, stashing the chosen card
 * on the {@link CardDemoCommarea} exactly as COCRDLIC moves it into {@code CDEMO-CARD-NUM}
 * before {@code XCTL}. A regular user's browse is pinned to the account in the COMMAREA; an
 * admin may filter freely.</p>
 */
@Component
public class CardListScreenHandler implements ScreenHandler {

    static final String FIELD_ACCOUNT_ID = "accountId";
    static final String FIELD_CARD_NUMBER = "cardNumber";
    static final String FIELD_PAGE = "page";
    static final String FIELD_ACTION = "action";
    static final String FIELD_SELECTED_CARD = "selectedCardNumber";

    private final CardService cardService;

    public CardListScreenHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.CARD_LIST;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        if (request.action() == CommonAction.BACK) {
            return ScreenResult.back();
        }

        int page = parsePage(request.field(FIELD_PAGE));
        if (request.pfKey().action() == CommonAction.PAGE_DOWN) {
            page += 1;
        } else if (request.pfKey().action() == CommonAction.PAGE_UP) {
            page = Math.max(0, page - 1);
        }

        String accountFilter = resolveAccountFilter(request.field(FIELD_ACCOUNT_ID), commarea);
        String cardFilter = request.field(FIELD_CARD_NUMBER);

        String action = trimToNull(request.field(FIELD_ACTION));
        String selectedCard = trimToNull(request.field(FIELD_SELECTED_CARD));
        if (request.action() == CommonAction.SUBMIT && action != null) {
            return handleSelection(action, selectedCard, commarea, accountFilter, cardFilter, page);
        }

        return listResult(accountFilter, cardFilter, page);
    }

    private ScreenResult handleSelection(String action, String selectedCard,
            CardDemoCommarea commarea, String accountFilter, String cardFilter, int page) {
        if (selectedCard == null) {
            return listResult(accountFilter, cardFilter, page);
        }
        return switch (action.toUpperCase()) {
            case "S" -> {
                commarea.getCardInfo().setCardNum(selectedCard);
                yield ScreenResult.transferTo(CardDemoProgram.CARD_VIEW);
            }
            case "U" -> {
                commarea.getCardInfo().setCardNum(selectedCard);
                yield ScreenResult.transferTo(CardDemoProgram.CARD_UPDATE);
            }
            default -> ScreenResult.stay(CardMessages.INVALID_ACTION_CODE,
                safeList(accountFilter, cardFilter, page));
        };
    }

    private ScreenResult listResult(String accountFilter, String cardFilter, int page) {
        try {
            CardListResponse list = cardService.listCards(accountFilter, cardFilter, page);
            return ScreenResult.stay(list.message(), list);
        } catch (CardValidationException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }

    private CardListResponse safeList(String accountFilter, String cardFilter, int page) {
        try {
            return cardService.listCards(accountFilter, cardFilter, page);
        } catch (CardValidationException ex) {
            return null;
        }
    }

    private static String resolveAccountFilter(String requested, CardDemoCommarea commarea) {
        if (commarea.getUserType() == UserType.ADMIN) {
            return requested;
        }
        String context = commarea.getAccountInfo().getAcctId();
        return context != null ? context : requested;
    }

    private static int parsePage(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null || !trimmed.chars().allMatch(Character::isDigit)) {
            return 0;
        }
        return Integer.parseInt(trimmed);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
