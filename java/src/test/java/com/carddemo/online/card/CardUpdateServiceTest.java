package com.carddemo.online.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import com.carddemo.repository.CardRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * COBOL program COCRDUPC — 1200-EDIT-MAP-INPUTS, 2000-DECIDE-ACTION,
 * 9200-WRITE-PROCESSING and 9300-CHECK-CHANGE-IN-REC.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardUpdateServiceTest {

    private static final String CARD = "4111111111111111";
    private static final String ACCOUNT = "00000000011";

    @Mock
    private CardRepository cardRepository;

    private CardUpdateService service;
    private CardUpdateState state;
    private CardDemoCommarea commarea;

    @BeforeEach
    void setUp() {
        service = new CardUpdateService(cardRepository);
        state = new CardUpdateState();
        commarea = new CardDemoCommarea();
        when(cardRepository.findById(CARD)).thenReturn(Optional.of(card()));
    }

    private static Card card() {
        return Card.builder()
                .cardNumber(CARD)
                .accountId(11L)
                .cvvCode(123)
                .embossedName("Aniya Von")
                .expirationDate("2023-03-09")
                .activeStatus("Y")
                .build();
    }

    private CardUpdateResponse send(CardUpdateRequest.CardUpdateRequestBuilder builder) {
        return service.process(builder.build(), state, commarea);
    }

    private CardUpdateResponse fetch() {
        return send(CardUpdateRequest.builder()
                .action(CardUpdateRequest.ACTION_ENTER)
                .accountId(ACCOUNT)
                .cardNumber(CARD));
    }

    private CardUpdateRequest.CardUpdateRequestBuilder unchangedInput() {
        return CardUpdateRequest.builder()
                .action(CardUpdateRequest.ACTION_ENTER)
                .accountId(ACCOUNT)
                .cardNumber(CARD)
                .cardName("ANIYA VON")
                .activeStatus("Y")
                .expiryMonth("03")
                .expiryYear("2023");
    }

    @Test
    void fetchesTheCardOnTheFirstEnter() {
        CardUpdateResponse response = fetch();

        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.SHOW_DETAILS);
        assertThat(response.getInfoMessage()).isEqualTo(CardUpdateService.FOUND_CARDS_FOR_ACCOUNT);
        assertThat(response.getData().getEmbossedName()).isEqualTo("ANIYA VON");
        assertThat(response.getData().getExpiryYear()).isEqualTo("2023");
        assertThat(response.getData().getExpiryDay()).isEqualTo("09");
        assertThat(commarea.getCardNumber()).isEqualTo(CARD);
    }

    @Test
    void reportsNoInputWhenNothingIsKeyed() {
        CardUpdateResponse response = send(CardUpdateRequest.builder()
                .action(CardUpdateRequest.ACTION_ENTER));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.NO_SEARCH_CRITERIA_RECEIVED);
        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.DETAILS_NOT_FETCHED);
    }

    @Test
    void reportsWhenTheCardIsNotOnFile() {
        when(cardRepository.findById(CARD)).thenReturn(Optional.empty());

        CardUpdateResponse response = fetch();

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.DID_NOT_FIND_ACCTCARD_COMBO);
        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.DETAILS_NOT_FETCHED);
    }

    @Test
    void detectsThatNothingWasChanged() {
        fetch();
        CardUpdateResponse response = send(unchangedInput());

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.NO_CHANGES_DETECTED);
        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.SHOW_DETAILS);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void comparesTheOldAndNewDataCaseInsensitively() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().cardName("aniya von"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.NO_CHANGES_DETECTED);
    }

    @Test
    void validChangesWaitForConfirmation() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().cardName("ANIYA VONN"));

        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.CHANGES_OK_NOT_CONFIRMED);
        assertThat(response.getInfoMessage()).isEqualTo(CardUpdateService.PROMPT_FOR_CONFIRMATION);
        assertThat(response.getErrorMessage()).isNull();
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void rejectsABlankCardName() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().cardName("   ").activeStatus("N"));

        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.CHANGES_NOT_OK);
        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.PROMPT_FOR_NAME);
        assertThat(response.getFieldErrors()).containsEntry("cardName", CardUpdateService.PROMPT_FOR_NAME);
    }

    @Test
    void rejectsACardNameWithDigits() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().cardName("AN1YA VON"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.NAME_MUST_BE_ALPHA);
        assertThat(response.getFieldErrors()).containsEntry("cardName", CardUpdateService.NAME_MUST_BE_ALPHA);
    }

    @Test
    void rejectsAnActiveStatusOtherThanYorN() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().activeStatus("X"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.CARD_STATUS_MUST_BE_YES_NO);
        assertThat(response.getFieldErrors())
                .containsEntry("activeStatus", CardUpdateService.CARD_STATUS_MUST_BE_YES_NO);
    }

    @Test
    void rejectsAnExpiryMonthOutsideOneToTwelve() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().expiryMonth("13"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.CARD_EXPIRY_MONTH_NOT_VALID);
        assertThat(response.getFieldErrors())
                .containsEntry("expiryMonth", CardUpdateService.CARD_EXPIRY_MONTH_NOT_VALID);
    }

    @Test
    void rejectsAnExpiryYearOutsideNineteenFiftyToTwentyNinetyNine() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().expiryYear("1949"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.CARD_EXPIRY_YEAR_NOT_VALID);
        assertThat(response.getFieldErrors()).containsEntry("expiryYear", CardUpdateService.CARD_EXPIRY_YEAR_NOT_VALID);
    }

    @Test
    void reportsTheFirstFailingEditInTheCobolOrder() {
        fetch();
        CardUpdateResponse response = send(unchangedInput().cardName("AN1YA VON").activeStatus("X")
                .expiryMonth("13").expiryYear("1900"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.NAME_MUST_BE_ALPHA);
        assertThat(response.getFieldErrors()).containsOnlyKeys("cardName", "activeStatus", "expiryMonth",
                "expiryYear");
    }

    @Test
    void pf5CommitsTheConfirmedChanges() {
        fetch();
        send(unchangedInput().cardName("ANIYA VONN").activeStatus("N").expiryMonth("04").expiryYear("2027"));

        CardUpdateResponse response = send(unchangedInput().action(CardUpdateRequest.ACTION_PF5)
                .cardName("ANIYA VONN").activeStatus("N").expiryMonth("04").expiryYear("2027"));

        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.CHANGES_OKAYED_AND_DONE);
        assertThat(response.getInfoMessage()).isEqualTo(CardUpdateService.CONFIRM_UPDATE_SUCCESS);

        ArgumentCaptor<Card> saved = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(saved.capture());
        assertThat(saved.getValue().getEmbossedName()).isEqualTo("ANIYA VONN");
        assertThat(saved.getValue().getActiveStatus()).isEqualTo("N");
        // The expiry day is not on the map, so it is carried over from the fetched record.
        assertThat(saved.getValue().getExpirationDate()).isEqualTo("2027-04-09");
    }

    @Test
    void refusesToCommitWhenTheRecordChangedInTheMeantime() {
        fetch();
        send(unchangedInput().cardName("ANIYA VONN"));

        Card meanwhile = card();
        meanwhile.setActiveStatus("N");
        when(cardRepository.findById(CARD)).thenReturn(Optional.of(meanwhile));

        CardUpdateResponse response = send(unchangedInput().action(CardUpdateRequest.ACTION_PF5)
                .cardName("ANIYA VONN"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.DATA_WAS_CHANGED_BEFORE_UPDATE);
        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.SHOW_DETAILS);
        assertThat(state.getOldDetails().getActiveStatus()).isEqualTo("N");
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void reportsWhenTheRecordCannotBeLocked() {
        fetch();
        send(unchangedInput().cardName("ANIYA VONN"));
        when(cardRepository.findById(CARD)).thenReturn(Optional.empty());

        CardUpdateResponse response = send(unchangedInput().action(CardUpdateRequest.ACTION_PF5)
                .cardName("ANIYA VONN"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.COULD_NOT_LOCK_FOR_UPDATE);
        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.CHANGES_OKAYED_LOCK_ERROR);
        assertThat(response.getInfoMessage()).isEqualTo(CardUpdateService.INFORM_FAILURE);
    }

    @Test
    void reportsWhenTheRewriteFails() {
        fetch();
        send(unchangedInput().cardName("ANIYA VONN"));
        when(cardRepository.save(any(Card.class))).thenThrow(new IllegalStateException("db down"));

        CardUpdateResponse response = send(unchangedInput().action(CardUpdateRequest.ACTION_PF5)
                .cardName("ANIYA VONN"));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.LOCKED_BUT_UPDATE_FAILED);
        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.CHANGES_OKAYED_BUT_FAILED);
    }

    @Test
    void pf12DiscardsTheKeyedChangesAndShowsTheStoredCardAgain() {
        fetch();
        send(unchangedInput().cardName("ANIYA VONN"));

        CardUpdateResponse response = send(unchangedInput().action(CardUpdateRequest.ACTION_PF12)
                .cardName("ANIYA VONN"));

        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.SHOW_DETAILS);
        assertThat(response.getData().getEmbossedName()).isEqualTo("ANIYA VON");
    }

    @Test
    void anEnterAfterASuccessfulUpdateStartsAFreshSearch() {
        fetch();
        send(unchangedInput().cardName("ANIYA VONN"));
        send(unchangedInput().action(CardUpdateRequest.ACTION_PF5).cardName("ANIYA VONN"));

        CardUpdateResponse response = send(unchangedInput());

        assertThat(response.getChangeAction()).isEqualTo(CardUpdateState.DETAILS_NOT_FETCHED);
        assertThat(response.getInfoMessage()).isEqualTo(CardUpdateService.PROMPT_FOR_SEARCH_KEYS);
    }

    @Test
    void pf3ReturnsToTheCallingProgram() {
        commarea.setFromProgram(CardWorkArea.LIST_PROGRAM);
        commarea.setFromTransactionId(CardWorkArea.LIST_TRANSACTION);

        CardUpdateResponse response = send(CardUpdateRequest.builder().action(CardUpdateRequest.ACTION_PF3));

        assertThat(response.getErrorMessage()).isEqualTo(CardUpdateService.EXIT_MESSAGE);
        assertThat(response.getNextProgram()).isEqualTo(CardWorkArea.LIST_PROGRAM);
        assertThat(state.getChangeAction()).isEqualTo(CardUpdateState.DETAILS_NOT_FETCHED);
    }
}
