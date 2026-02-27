package com.carddemo.service;

import com.carddemo.entity.Card;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock private CardRepository cardRepository;
    @InjectMocks private CardService cardService;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testCard = new Card();
        testCard.setCardNum("4111111111111111");
        testCard.setCardAcctId(10000000001L);
        testCard.setCardCvvCd(123);
        testCard.setCardEmbossedName("JOHN DOE");
        testCard.setCardActiveStatus("Y");
    }

    @Test
    void listCards() {
        Page<Card> page = new PageImpl<>(List.of(testCard));
        when(cardRepository.findAll(any(PageRequest.class))).thenReturn(page);
        Page<Card> result = cardService.listCards(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCard_success() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(testCard));
        Card result = cardService.getCard("4111111111111111");
        assertEquals("JOHN DOE", result.getCardEmbossedName());
    }

    @Test
    void getCard_notFound() {
        when(cardRepository.findById("0000")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cardService.getCard("0000"));
    }

    @Test
    void updateCard_success() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        Card update = new Card();
        update.setCardActiveStatus("N");
        Card result = cardService.updateCard("4111111111111111", update);
        assertNotNull(result);
    }
}
