package com.carddemo.card.service;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.dto.CardXrefResponse;
import com.carddemo.card.entity.Card;
import com.carddemo.card.entity.CardXref;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private CardService cardService;

    private Card sampleCard;
    private CardXref sampleXref;

    @BeforeEach
    void setUp() {
        sampleCard = new Card(
                "4111111111111111",
                "00000000001",
                "123",
                "JOHN A SMITH",
                "2026-12-01",
                "Y"
        );

        sampleXref = new CardXref(
                "4111111111111111",
                "000000001",
                "00000000001"
        );
    }

    @Test
    void listCards_withoutAccountId_returnsAllCards() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<CardResponse> result = cardService.listCards(null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("4111111111111111", result.getContent().get(0).cardNum());
        verify(cardRepository).findAll(any(Pageable.class));
        verify(cardRepository, never()).findByCardAcctId(any(), any());
    }

    @Test
    void listCards_withAccountId_returnsFilteredCards() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findByCardAcctId(eq("00000000001"), any(Pageable.class)))
                .thenReturn(page);

        Page<CardResponse> result = cardService.listCards("00000000001", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("00000000001", result.getContent().get(0).cardAcctId());
        verify(cardRepository).findByCardAcctId(eq("00000000001"), any(Pageable.class));
    }

    @Test
    void getCardByNumber_found() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        Optional<CardResponse> result = cardService.getCardByNumber("4111111111111111");

        assertTrue(result.isPresent());
        assertEquals("4111111111111111", result.get().cardNum());
        assertEquals("JOHN A SMITH", result.get().cardEmbossedName());
        assertEquals("123", result.get().cardCvvCd());
    }

    @Test
    void getCardByNumber_notFound() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        Optional<CardResponse> result = cardService.getCardByNumber("9999999999999999");

        assertFalse(result.isPresent());
    }

    @Test
    void updateCard_found_updatesFields() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardUpdateRequest request = new CardUpdateRequest("JANE B DOE", "2028-01-15", "N");
        Optional<CardResponse> result = cardService.updateCard("4111111111111111", request);

        assertTrue(result.isPresent());
        assertEquals("JANE B DOE", result.get().cardEmbossedName());
        assertEquals("2028-01-15", result.get().cardExpirationDate());
        assertEquals("N", result.get().cardActiveStatus());
    }

    @Test
    void updateCard_notFound() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        CardUpdateRequest request = new CardUpdateRequest("NEW NAME", null, null);
        Optional<CardResponse> result = cardService.updateCard("9999999999999999", request);

        assertFalse(result.isPresent());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getXrefByAccountId_found() {
        CardXref xref2 = new CardXref("4111111111112222", "000000001", "00000000001");
        when(cardXrefRepository.findByXrefAcctId("00000000001"))
                .thenReturn(List.of(sampleXref, xref2));

        List<CardXrefResponse> result = cardService.getXrefByAccountId("00000000001");

        assertEquals(2, result.size());
        assertEquals("4111111111111111", result.get(0).cardNum());
        assertEquals("000000001", result.get(0).custId());
        assertEquals("00000000001", result.get(0).acctId());
        assertEquals("4111111111112222", result.get(1).cardNum());
    }

    @Test
    void getXrefByAccountId_empty() {
        when(cardXrefRepository.findByXrefAcctId("99999999999"))
                .thenReturn(Collections.emptyList());

        List<CardXrefResponse> result = cardService.getXrefByAccountId("99999999999");

        assertTrue(result.isEmpty());
    }
}
