package com.carddemo.service.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carddemo.domain.Card;
import com.carddemo.repository.CardRepository;
import com.carddemo.web.card.dto.CardDetailResponse;
import com.carddemo.web.card.dto.CardListResponse;
import com.carddemo.web.card.dto.CardUpdateRequest;
import com.carddemo.web.card.dto.CardUpdateResponse;

/**
 * Unit tests for {@link CardService} covering the ported COBOL edits, the 7-row paged browse
 * and the update read-modify-write, with the {@link CardRepository} mocked.
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private static Card card(String num, String acct, String name, String expiry, String status) {
        Card c = new Card();
        c.setCardNum(num);
        c.setCardAcctId(acct);
        c.setCardCvvCd("123");
        c.setCardEmbossedName(name);
        c.setCardExpirationDate(expiry);
        c.setCardActiveStatus(status);
        return c;
    }

    private static String cardNum(int i) {
        return String.format("%016d", 1_000_000_000L + i);
    }

    // ---- list -----------------------------------------------------------------------

    @Test
    void listPagesSevenRowsWithNextFlag() {
        List<Card> all = IntStream.range(0, 10)
            .mapToObj(i -> card(cardNum(i), "00000000001", "NAME", "2025-01-01", "Y"))
            .toList();
        when(cardRepository.findAll()).thenReturn(all);

        CardListResponse page0 = cardService.listCards(null, null, 0);
        assertThat(page0.cards()).hasSize(7);
        assertThat(page0.pageSize()).isEqualTo(7);
        assertThat(page0.hasNextPage()).isTrue();
        assertThat(page0.hasPreviousPage()).isFalse();
        assertThat(page0.cards().get(0).cardNumber()).isEqualTo(cardNum(0));

        CardListResponse page1 = cardService.listCards(null, null, 1);
        assertThat(page1.cards()).hasSize(3);
        assertThat(page1.hasNextPage()).isFalse();
        assertThat(page1.hasPreviousPage()).isTrue();
    }

    @Test
    void listSortsAscendingByCardNumber() {
        when(cardRepository.findAll()).thenReturn(List.of(
            card(cardNum(3), "00000000001", "C", "2025-01-01", "Y"),
            card(cardNum(1), "00000000001", "A", "2025-01-01", "Y"),
            card(cardNum(2), "00000000001", "B", "2025-01-01", "Y")));

        CardListResponse response = cardService.listCards(null, null, 0);
        assertThat(response.cards()).extracting("cardNumber")
            .containsExactly(cardNum(1), cardNum(2), cardNum(3));
    }

    @Test
    void listByAccountUsesAccountFinder() {
        when(cardRepository.findByCardAcctId("00000000050"))
            .thenReturn(List.of(card(cardNum(0), "00000000050", "N", "2025-01-01", "Y")));

        CardListResponse response = cardService.listCards("00000000050", null, 0);
        assertThat(response.cards()).hasSize(1);
        verify(cardRepository, never()).findAll();
    }

    @Test
    void listEmptyReturnsNoRecordsMessage() {
        when(cardRepository.findAll()).thenReturn(List.of());
        CardListResponse response = cardService.listCards(null, null, 0);
        assertThat(response.cards()).isEmpty();
        assertThat(response.message()).isEqualTo(CardMessages.NO_RECORDS_FOUND);
    }

    @Test
    void listRejectsBadAccountFilter() {
        assertThatThrownBy(() -> cardService.listCards("123", null, 0))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.ACCOUNT_FILTER_INVALID);
    }

    @Test
    void listRejectsBadCardFilter() {
        assertThatThrownBy(() -> cardService.listCards(null, "123", 0))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.CARD_FILTER_INVALID);
    }

    // ---- detail ---------------------------------------------------------------------

    @Test
    void getCardReturnsDetail() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "ANIYA VON", "2023-03-09", "Y")));

        CardDetailResponse detail = cardService.getCard(cardNum(0), null);
        assertThat(detail.accountId()).isEqualTo("00000000050");
        assertThat(detail.embossedName()).isEqualTo("ANIYA VON");
        assertThat(detail.expiryYear()).isEqualTo("2023");
        assertThat(detail.expiryMonth()).isEqualTo("03");
        assertThat(detail.expiryDay()).isEqualTo("09");
    }

    @Test
    void getCardNotFound() {
        when(cardRepository.findById(cardNum(9))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.getCard(cardNum(9), null))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessage(CardMessages.DID_NOT_FIND_CARDS);
    }

    @Test
    void getCardRejectsNonNumericCardNumber() {
        assertThatThrownBy(() -> cardService.getCard("abc", null))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.CARD_16_DIGIT);
    }

    @Test
    void getCardRejectsBlankCardNumber() {
        assertThatThrownBy(() -> cardService.getCard("   ", null))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.CARD_NOT_PROVIDED);
    }

    @Test
    void getCardRejectsZeroAccount() {
        assertThatThrownBy(() -> cardService.getCard(cardNum(0), "00000000000"))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.ACCOUNT_NON_ZERO_11);
    }

    @Test
    void getCardAccountMismatchIsNotFound() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "N", "2023-03-09", "Y")));
        assertThatThrownBy(() -> cardService.getCard(cardNum(0), "00000000099"))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessage(CardMessages.DID_NOT_FIND_CARDS);
    }

    // ---- update ---------------------------------------------------------------------

    @Test
    void updateAppliesEditableFieldsAndPreservesDay() {
        Card existing = card(cardNum(0), "00000000050", "OLD NAME", "2023-03-09", "Y");
        when(cardRepository.findById(cardNum(0))).thenReturn(Optional.of(existing));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardUpdateResponse result = cardService.updateCard(cardNum(0),
            new CardUpdateRequest("New Name", "N", "12", "2030"));

        assertThat(result.message()).isEqualTo(CardMessages.UPDATE_SUCCESS);
        ArgumentCaptor<Card> saved = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(saved.capture());
        assertThat(saved.getValue().getCardEmbossedName()).isEqualTo("New Name");
        assertThat(saved.getValue().getCardActiveStatus()).isEqualTo("N");
        // Day (09) carried from the fetched record; year/month replaced.
        assertThat(saved.getValue().getCardExpirationDate()).isEqualTo("2030-12-09");
    }

    @Test
    void updateRejectsBlankName() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "OLD", "2023-03-09", "Y")));
        assertThatThrownBy(() -> cardService.updateCard(cardNum(0),
                new CardUpdateRequest("  ", "Y", "01", "2025")))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.NAME_NOT_PROVIDED);
    }

    @Test
    void updateRejectsNonAlphaName() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "OLD", "2023-03-09", "Y")));
        assertThatThrownBy(() -> cardService.updateCard(cardNum(0),
                new CardUpdateRequest("Name123", "Y", "01", "2025")))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.NAME_MUST_BE_ALPHA);
    }

    @Test
    void updateRejectsBadStatus() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "OLD", "2023-03-09", "Y")));
        assertThatThrownBy(() -> cardService.updateCard(cardNum(0),
                new CardUpdateRequest("New Name", "X", "01", "2025")))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.STATUS_MUST_BE_YES_NO);
    }

    @Test
    void updateRejectsBadMonth() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "OLD", "2023-03-09", "Y")));
        assertThatThrownBy(() -> cardService.updateCard(cardNum(0),
                new CardUpdateRequest("New Name", "Y", "13", "2025")))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.EXPIRY_MONTH_NOT_VALID);
    }

    @Test
    void updateRejectsBadYear() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "OLD", "2023-03-09", "Y")));
        assertThatThrownBy(() -> cardService.updateCard(cardNum(0),
                new CardUpdateRequest("New Name", "Y", "01", "1800")))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.EXPIRY_YEAR_NOT_VALID);
    }

    @Test
    void updateDetectsNoChange() {
        when(cardRepository.findById(cardNum(0)))
            .thenReturn(Optional.of(card(cardNum(0), "00000000050", "ANIYA VON", "2023-03-09", "Y")));
        // Same values (name matched case-insensitively): the COBOL NO-CHANGES-DETECTED path.
        assertThatThrownBy(() -> cardService.updateCard(cardNum(0),
                new CardUpdateRequest("aniya von", "Y", "03", "2023")))
            .isInstanceOf(CardValidationException.class)
            .hasMessage(CardMessages.NO_CHANGES_DETECTED);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void updateNotFound() {
        when(cardRepository.findById(cardNum(9))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.updateCard(cardNum(9),
                new CardUpdateRequest("New Name", "Y", "01", "2025")))
            .isInstanceOf(CardNotFoundException.class)
            .hasMessage(CardMessages.DID_NOT_FIND_CARDS);
    }
}
