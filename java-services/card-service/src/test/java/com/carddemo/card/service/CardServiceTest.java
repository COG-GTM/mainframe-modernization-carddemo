package com.carddemo.card.service;

import com.carddemo.card.dto.CardDto;
import com.carddemo.card.dto.CardListResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.exception.ResourceNotFoundException;
import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardCrossReference;
import com.carddemo.card.repository.CardCrossReferenceRepository;
import com.carddemo.card.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardCrossReferenceRepository crossReferenceRepository;

    @InjectMocks
    private CardService cardService;

    private Card sampleCard;
    private CardCrossReference sampleXref;

    @BeforeEach
    void setUp() {
        sampleCard = Card.builder()
                .cardNumber("4111111111111111")
                .accountId("00000000001")
                .cvvCode("123")
                .embossedName("JOHN DOE")
                .expirationDate("2026-12-01")
                .activeStatus("Y")
                .build();

        sampleXref = CardCrossReference.builder()
                .cardNumber("4111111111111111")
                .customerId("000000001")
                .accountId("00000000001")
                .build();
    }

    @Test
    void getCardsByAccountId_returnsPagedResults() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findByAccountId(eq("00000000001"), any(Pageable.class)))
                .thenReturn(page);

        CardListResponse response = cardService.getCardsByAccountId("00000000001", 0, 7);

        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getCards().get(0).getCardNumber()).isEqualTo("4111111111111111");
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getPage()).isEqualTo(0);
    }

    @Test
    void getCardsByAccountId_emptyResult() {
        Page<Card> emptyPage = Page.empty();
        when(cardRepository.findByAccountId(eq("99999999999"), any(Pageable.class)))
                .thenReturn(emptyPage);

        CardListResponse response = cardService.getCardsByAccountId("99999999999", 0, 7);

        assertThat(response.getCards()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    void getCardByNumber_found() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardDto dto = cardService.getCardByNumber("4111111111111111");

        assertThat(dto.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(dto.getAccountId()).isEqualTo("00000000001");
        assertThat(dto.getEmbossedName()).isEqualTo("JOHN DOE");
        assertThat(dto.getActiveStatus()).isEqualTo("Y");
    }

    @Test
    void getCardByNumber_notFound_throwsException() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardByNumber("0000000000000000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Card");
    }

    @Test
    void updateCard_validUpdate_embossedName() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardUpdateRequest request = CardUpdateRequest.builder()
                .embossedName("JANE DOE")
                .build();

        CardDto result = cardService.updateCard("4111111111111111", request);

        verify(cardRepository).save(any(Card.class));
        assertThat(result).isNotNull();
    }

    @Test
    void updateCard_validUpdate_activeStatus() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardUpdateRequest request = CardUpdateRequest.builder()
                .activeStatus("N")
                .build();

        CardDto result = cardService.updateCard("4111111111111111", request);

        verify(cardRepository).save(any(Card.class));
        assertThat(result).isNotNull();
    }

    @Test
    void updateCard_validUpdate_expirationDate() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardUpdateRequest request = CardUpdateRequest.builder()
                .expirationDate("2027-06-15")
                .build();

        CardDto result = cardService.updateCard("4111111111111111", request);

        verify(cardRepository).save(any(Card.class));
        assertThat(result).isNotNull();
    }

    @Test
    void updateCard_invalidExpiryMonth_throwsException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = CardUpdateRequest.builder()
                .expirationDate("2026-13-01")
                .build();

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("month");
    }

    @Test
    void updateCard_invalidExpiryYear_throwsException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = CardUpdateRequest.builder()
                .expirationDate("2100-06-01")
                .build();

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("year");
    }

    @Test
    void updateCard_invalidExpiryDay_throwsException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = CardUpdateRequest.builder()
                .expirationDate("2026-06-32")
                .build();

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("day");
    }

    @Test
    void updateCard_notFound_throwsException() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        CardUpdateRequest request = CardUpdateRequest.builder()
                .embossedName("TEST")
                .build();

        assertThatThrownBy(() -> cardService.updateCard("0000000000000000", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCrossReference_found() {
        when(crossReferenceRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleXref));

        CardCrossReference xref = cardService.getCrossReference("4111111111111111");

        assertThat(xref.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(xref.getCustomerId()).isEqualTo("000000001");
        assertThat(xref.getAccountId()).isEqualTo("00000000001");
    }

    @Test
    void getCrossReference_notFound_throwsException() {
        when(crossReferenceRepository.findById("0000000000000000"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCrossReference("0000000000000000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CardCrossReference");
    }
}
