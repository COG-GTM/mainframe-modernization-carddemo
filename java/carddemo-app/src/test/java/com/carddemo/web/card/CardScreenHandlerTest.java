package com.carddemo.web.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carddemo.service.card.CardMessages;
import com.carddemo.service.card.CardNotFoundException;
import com.carddemo.service.card.CardService;
import com.carddemo.web.card.dto.CardDetailResponse;
import com.carddemo.web.card.dto.CardListResponse;
import com.carddemo.web.card.dto.CardUpdateResponse;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.PfKey;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;

/**
 * Unit tests for the three card {@link com.carddemo.session.ScreenHandler} beans, exercising
 * the navigation-turn behaviour (list paging/selection, detail fetch, update) with a mocked
 * {@link CardService}.
 */
@ExtendWith(MockitoExtension.class)
class CardScreenHandlerTest {

    private static final String CARD = "0500024453765740";

    @Mock
    private CardService cardService;

    private static CardListResponse emptyList(int page) {
        return new CardListResponse(page, 7, List.of(), false, false, CardMessages.NO_RECORDS_FOUND);
    }

    private static CardDetailResponse detail() {
        return new CardDetailResponse("00000000050", CARD, "747", "Aniya Von",
            "2023-03-09", "2023", "03", "09", "Y");
    }

    private static ScreenRequest request(PfKey key, Map<String, String> fields) {
        return new ScreenRequest(key, fields);
    }

    // ---- list handler ---------------------------------------------------------------

    @Test
    void listHandlerReturnsListModelOnEnter() {
        when(cardService.listCards(null, null, 0)).thenReturn(emptyList(0));
        CardListScreenHandler handler = new CardListScreenHandler(cardService);

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of()), new CardDemoCommarea());

        assertThat(result.type()).isEqualTo(ScreenResult.Type.STAY);
        assertThat(result.model()).isInstanceOf(CardListResponse.class);
    }

    @Test
    void listHandlerOwnsCardListProgram() {
        assertThat(new CardListScreenHandler(cardService).program())
            .isEqualTo(CardDemoProgram.CARD_LIST);
    }

    @Test
    void listHandlerPagesForwardOnPf8() {
        when(cardService.listCards(null, null, 1)).thenReturn(emptyList(1));
        CardListScreenHandler handler = new CardListScreenHandler(cardService);

        ScreenResult result = handler.handle(
            request(PfKey.PF8, Map.of(CardListScreenHandler.FIELD_PAGE, "0")), new CardDemoCommarea());

        assertThat(((CardListResponse) result.model()).page()).isEqualTo(1);
    }

    @Test
    void listHandlerTransfersToViewOnSelectS() {
        CardListScreenHandler handler = new CardListScreenHandler(cardService);
        CardDemoCommarea commarea = new CardDemoCommarea();

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of(
            CardListScreenHandler.FIELD_ACTION, "S",
            CardListScreenHandler.FIELD_SELECTED_CARD, CARD)), commarea);

        assertThat(result.type()).isEqualTo(ScreenResult.Type.TRANSFER);
        assertThat(result.target()).isEqualTo(CardDemoProgram.CARD_VIEW);
        assertThat(commarea.getCardInfo().getCardNum()).isEqualTo(CARD);
    }

    @Test
    void listHandlerTransfersToUpdateOnSelectU() {
        CardListScreenHandler handler = new CardListScreenHandler(cardService);
        CardDemoCommarea commarea = new CardDemoCommarea();

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of(
            CardListScreenHandler.FIELD_ACTION, "U",
            CardListScreenHandler.FIELD_SELECTED_CARD, CARD)), commarea);

        assertThat(result.type()).isEqualTo(ScreenResult.Type.TRANSFER);
        assertThat(result.target()).isEqualTo(CardDemoProgram.CARD_UPDATE);
    }

    @Test
    void listHandlerRejectsInvalidActionCode() {
        when(cardService.listCards(null, null, 0)).thenReturn(emptyList(0));
        CardListScreenHandler handler = new CardListScreenHandler(cardService);

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of(
            CardListScreenHandler.FIELD_ACTION, "X",
            CardListScreenHandler.FIELD_SELECTED_CARD, CARD)), new CardDemoCommarea());

        assertThat(result.message()).isEqualTo(CardMessages.INVALID_ACTION_CODE);
    }

    @Test
    void listHandlerReturnsBackOnPf3() {
        CardListScreenHandler handler = new CardListScreenHandler(cardService);
        ScreenResult result = handler.handle(request(PfKey.PF3, Map.of()), new CardDemoCommarea());
        assertThat(result.type()).isEqualTo(ScreenResult.Type.BACK);
    }

    // ---- view handler ---------------------------------------------------------------

    @Test
    void viewHandlerShowsDetailFromCommarea() {
        when(cardService.getCard(eq(CARD), any())).thenReturn(detail());
        CardViewScreenHandler handler = new CardViewScreenHandler(cardService);
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.getCardInfo().setCardNum(CARD);

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of()), commarea);

        assertThat(result.type()).isEqualTo(ScreenResult.Type.STAY);
        assertThat(result.model()).isInstanceOf(CardDetailResponse.class);
    }

    @Test
    void viewHandlerShowsMessageWhenNotFound() {
        when(cardService.getCard(eq(CARD), any()))
            .thenThrow(new CardNotFoundException(CardMessages.DID_NOT_FIND_CARDS));
        CardViewScreenHandler handler = new CardViewScreenHandler(cardService);
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.getCardInfo().setCardNum(CARD);

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of()), commarea);

        assertThat(result.message()).isEqualTo(CardMessages.DID_NOT_FIND_CARDS);
        assertThat(result.model()).isNull();
    }

    // ---- update handler -------------------------------------------------------------

    @Test
    void updateHandlerFetchesDetailWhenNoEdits() {
        when(cardService.getCard(eq(CARD), any())).thenReturn(detail());
        CardUpdateScreenHandler handler = new CardUpdateScreenHandler(cardService);
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.getCardInfo().setCardNum(CARD);

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of()), commarea);

        assertThat(result.model()).isInstanceOf(CardDetailResponse.class);
    }

    @Test
    void updateHandlerAppliesEditsAndReportsSuccess() {
        when(cardService.updateCard(eq(CARD), any()))
            .thenReturn(new CardUpdateResponse(CardMessages.UPDATE_SUCCESS, detail()));
        CardUpdateScreenHandler handler = new CardUpdateScreenHandler(cardService);
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.getCardInfo().setCardNum(CARD);

        ScreenResult result = handler.handle(request(PfKey.ENTER, Map.of(
            CardUpdateScreenHandler.FIELD_EMBOSSED_NAME, "New Name",
            CardUpdateScreenHandler.FIELD_ACTIVE_STATUS, "N",
            CardUpdateScreenHandler.FIELD_EXPIRY_MONTH, "12",
            CardUpdateScreenHandler.FIELD_EXPIRY_YEAR, "2030")), commarea);

        assertThat(result.message()).isEqualTo(CardMessages.UPDATE_SUCCESS);
        assertThat(result.model()).isInstanceOf(CardDetailResponse.class);
    }
}
