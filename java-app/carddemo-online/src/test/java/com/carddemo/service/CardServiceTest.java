package com.carddemo.service;

import com.carddemo.entity.Card;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CardService — validates parity with COCRDLIC, COCRDSLC, COCRDUPC.
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void getCard_found() {
        Card card = new Card();
        card.setCardNum("4111111111111111");
        card.setAcctId(1L);
        card.setActiveStatus("Y");

        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.empty());

        Map<String, Object> result = cardService.getCard("4111111111111111");
        assertNotNull(result);
        Card resultCard = (Card) result.get("card");
        assertEquals("4111111111111111", resultCard.getCardNum());
        assertEquals("Y", resultCard.getActiveStatus());
    }

    @Test
    void getCard_notFound() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> cardService.getCard("0000000000000000"));
    }

    @Test
    void listCards_pagination() {
        Card card = new Card();
        card.setCardNum("4111111111111111");
        card.setAcctId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findByAcctId(eq(1L), any(Pageable.class))).thenReturn(page);

        Page<Card> result = cardService.listCards(1L, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("4111111111111111", result.getContent().get(0).getCardNum());
    }
}
