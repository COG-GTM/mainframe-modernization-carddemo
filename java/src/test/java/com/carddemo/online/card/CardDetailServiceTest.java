package com.carddemo.online.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import com.carddemo.repository.CardRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** COBOL program COCRDSLC — 2210-EDIT-ACCOUNT, 2220-EDIT-CARD and 9100-GETCARD-BYACCTCARD. */
@ExtendWith(MockitoExtension.class)
class CardDetailServiceTest {

    private static final String CARD = "4111111111111111";

    @Mock
    private CardRepository cardRepository;

    private CardDetailService service;
    private CardDemoCommarea commarea;

    @BeforeEach
    void setUp() {
        service = new CardDetailService(cardRepository);
        commarea = new CardDemoCommarea();
    }

    private CardDetailResponse view(String action, String account, String card) {
        return service.view(CardDetailRequest.builder()
                .action(action).accountId(account).cardNumber(card).build(), commarea);
    }

    @Test
    void showsTheRequestedCard() {
        when(cardRepository.findById(CARD)).thenReturn(Optional.of(Card.builder()
                .cardNumber(CARD)
                .accountId(11L)
                .cvvCode(123)
                .embossedName("ANIYA VON")
                .expirationDate("2023-03-09")
                .activeStatus("Y")
                .build()));

        CardDetailResponse response = view(CardDetailRequest.ACTION_ENTER, "00000000011", CARD);

        assertThat(response.isCardFound()).isTrue();
        assertThat(response.getInfoMessage()).isEqualTo(CardDetailService.FOUND_CARDS_FOR_ACCOUNT);
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getAccountId()).isEqualTo("00000000011");
        assertThat(response.getEmbossedName()).isEqualTo("ANIYA VON");
        assertThat(response.getExpiryYear()).isEqualTo("2023");
        assertThat(response.getExpiryMonth()).isEqualTo("03");
        assertThat(response.getActiveStatus()).isEqualTo("Y");
        assertThat(commarea.getCardNumber()).isEqualTo(CARD);
        assertThat(commarea.getLastMap()).isEqualTo(CardWorkArea.DETAIL_MAP);
    }

    @Test
    void reportsNoInputWhenBothKeysAreBlank() {
        CardDetailResponse response = view(CardDetailRequest.ACTION_ENTER, "*", "  ");

        assertThat(response.getErrorMessage()).isEqualTo(CardDetailService.NO_SEARCH_CRITERIA_RECEIVED);
        assertThat(response.getFieldErrors())
                .containsEntry("accountId", CardDetailService.PROMPT_FOR_ACCT)
                .containsEntry("cardNumber", CardDetailService.PROMPT_FOR_CARD);
    }

    @Test
    void promptsForTheAccountWhenOnlyTheCardIsKeyed() {
        CardDetailResponse response = view(CardDetailRequest.ACTION_ENTER, null, CARD);

        assertThat(response.getErrorMessage()).isEqualTo(CardDetailService.PROMPT_FOR_ACCT);
        assertThat(response.getFieldErrors()).doesNotContainKey("cardNumber");
    }

    @Test
    void promptsForTheCardWhenOnlyTheAccountIsKeyed() {
        CardDetailResponse response = view(CardDetailRequest.ACTION_ENTER, "00000000011", null);

        assertThat(response.getErrorMessage()).isEqualTo(CardDetailService.PROMPT_FOR_CARD);
    }

    @Test
    void rejectsAnAccountThatIsNotElevenDigits() {
        CardDetailResponse response = view(CardDetailRequest.ACTION_ENTER, "1234", CARD);

        assertThat(response.getErrorMessage()).isEqualTo(CardDetailService.ACCOUNT_FILTER_INVALID);
        assertThat(response.getFieldErrors()).containsEntry("accountId", CardDetailService.ACCOUNT_FILTER_INVALID);
    }

    @Test
    void rejectsACardThatIsNotSixteenDigits() {
        CardDetailResponse response = view(CardDetailRequest.ACTION_ENTER, "00000000011", "41111111");

        assertThat(response.getErrorMessage()).isEqualTo(CardDetailService.CARD_FILTER_INVALID);
        assertThat(response.getFieldErrors()).containsEntry("cardNumber", CardDetailService.CARD_FILTER_INVALID);
    }

    @Test
    void reportsWhenTheCardIsNotOnFile() {
        when(cardRepository.findById(CARD)).thenReturn(Optional.empty());

        CardDetailResponse response = view(CardDetailRequest.ACTION_ENTER, "00000000011", CARD);

        assertThat(response.isCardFound()).isFalse();
        assertThat(response.getErrorMessage()).isEqualTo(CardDetailService.DID_NOT_FIND_ACCTCARD_COMBO);
        assertThat(response.getFieldErrors())
                .containsEntry("accountId", CardDetailService.DID_NOT_FIND_ACCTCARD_COMBO)
                .containsEntry("cardNumber", CardDetailService.DID_NOT_FIND_ACCTCARD_COMBO);
    }

    @Test
    void pf3ReturnsToTheProgramThatCalledIt() {
        commarea.setFromProgram(CardWorkArea.LIST_PROGRAM);
        commarea.setFromTransactionId(CardWorkArea.LIST_TRANSACTION);

        CardDetailResponse response = view(CardDetailRequest.ACTION_PF3, null, null);

        assertThat(response.getErrorMessage()).isEqualTo(CardDetailService.EXIT_MESSAGE);
        assertThat(response.getNextProgram()).isEqualTo(CardWorkArea.LIST_PROGRAM);
        assertThat(commarea.getToProgram()).isEqualTo(CardWorkArea.LIST_PROGRAM);
        assertThat(commarea.getFromProgram()).isEqualTo(CardWorkArea.DETAIL_PROGRAM);
    }
}
