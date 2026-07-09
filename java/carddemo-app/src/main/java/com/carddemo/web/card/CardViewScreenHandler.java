package com.carddemo.web.card;

import org.springframework.stereotype.Component;

import com.carddemo.service.card.CardNotFoundException;
import com.carddemo.service.card.CardService;
import com.carddemo.service.card.CardValidationException;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.CommonAction;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.card.dto.CardDetailResponse;

/**
 * Navigation-framework handler for the card-detail screen ({@code COCRDSL} / {@code COCRDSLC}).
 *
 * <p>The card number comes from the COMMAREA ({@code CDEMO-CARD-NUM}, set by the list screen)
 * or from a submitted {@code cardNumber} field; the account, when carried, comes from
 * {@code CDEMO-ACCT-ID}. It reads the record ({@code 9100-GETCARD-BYACCTCARD}) and displays
 * the detail, or re-displays with the not-found/edit message. PF3 returns to the caller.</p>
 */
@Component
public class CardViewScreenHandler implements ScreenHandler {

    static final String FIELD_CARD_NUMBER = "cardNumber";
    static final String FIELD_ACCOUNT_ID = "accountId";

    private final CardService cardService;

    public CardViewScreenHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.CARD_VIEW;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        if (request.action() == CommonAction.BACK) {
            return ScreenResult.back();
        }

        String cardNumber = firstNonBlank(request.field(FIELD_CARD_NUMBER),
            commarea.getCardInfo().getCardNum());
        String accountId = firstNonBlank(request.field(FIELD_ACCOUNT_ID),
            commarea.getAccountInfo().getAcctId());

        try {
            CardDetailResponse detail = cardService.getCard(cardNumber, accountId);
            commarea.getCardInfo().setCardNum(detail.cardNumber());
            return ScreenResult.stay(null, detail);
        } catch (CardValidationException | CardNotFoundException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
