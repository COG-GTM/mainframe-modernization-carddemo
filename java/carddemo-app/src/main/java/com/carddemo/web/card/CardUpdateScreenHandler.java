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
import com.carddemo.web.card.dto.CardUpdateRequest;
import com.carddemo.web.card.dto.CardUpdateResponse;

/**
 * Navigation-framework handler for the card-update screen ({@code COCRDUP} / {@code COCRDUPC}).
 *
 * <p>The card number comes from the COMMAREA ({@code CDEMO-CARD-NUM}). On a plain fetch
 * (no edited fields submitted) it presents the current detail — the equivalent of COCRDUPC's
 * {@code CCUP-DETAILS-NOT-FETCHED} then {@code CCUP-SHOW-DETAILS} state. When the editable
 * fields are submitted it validates and rewrites the record ({@code 1200-EDIT-MAP-INPUTS}
 * then {@code 9200-WRITE-PROCESSING}); on any edit failure it re-displays with the message.
 *
 * <p>Because the shared {@code PfKey} enum has no PF5 (the COBOL "save" key), the submit is
 * driven by ENTER carrying the edited fields, which the framework already normalises unknown
 * attention keys to.</p>
 */
@Component
public class CardUpdateScreenHandler implements ScreenHandler {

    static final String FIELD_EMBOSSED_NAME = "embossedName";
    static final String FIELD_ACTIVE_STATUS = "activeStatus";
    static final String FIELD_EXPIRY_MONTH = "expiryMonth";
    static final String FIELD_EXPIRY_YEAR = "expiryYear";

    private final CardService cardService;

    public CardUpdateScreenHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.CARD_UPDATE;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        if (request.action() == CommonAction.BACK) {
            return ScreenResult.back();
        }

        String cardNumber = commarea.getCardInfo().getCardNum();

        boolean editsSubmitted = anyPresent(request,
            FIELD_EMBOSSED_NAME, FIELD_ACTIVE_STATUS, FIELD_EXPIRY_MONTH, FIELD_EXPIRY_YEAR);

        if (!editsSubmitted) {
            return fetchDetail(cardNumber, commarea);
        }

        CardUpdateRequest updateRequest = new CardUpdateRequest(
            request.field(FIELD_EMBOSSED_NAME),
            request.field(FIELD_ACTIVE_STATUS),
            request.field(FIELD_EXPIRY_MONTH),
            request.field(FIELD_EXPIRY_YEAR));

        try {
            CardUpdateResponse result = cardService.updateCard(cardNumber, updateRequest);
            return ScreenResult.stay(result.message(), result.card());
        } catch (CardValidationException | CardNotFoundException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }

    private ScreenResult fetchDetail(String cardNumber, CardDemoCommarea commarea) {
        try {
            CardDetailResponse detail = cardService.getCard(cardNumber,
                commarea.getAccountInfo().getAcctId());
            return ScreenResult.stay(null, detail);
        } catch (CardValidationException | CardNotFoundException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }

    private static boolean anyPresent(ScreenRequest request, String... fields) {
        for (String field : fields) {
            String value = request.field(field);
            if (value != null && !value.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
