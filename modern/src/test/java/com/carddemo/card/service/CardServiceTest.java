package com.carddemo.card.service;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.entity.CardEntity;
import com.carddemo.card.exception.CardNotFoundException;
import com.carddemo.card.exception.CardValidationException;
import com.carddemo.card.repository.CardRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CardService.
 *
 * Tests cover business logic migrated from:
 *   COCRDLIC.cbl — list with pagination
 *   COCRDSLC.cbl — detail view
 *   COCRDUPC.cbl — update with validation
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private CardEntity sampleCard;

    @BeforeEach
    void setUp() {
        sampleCard = new CardEntity(
                "4111111111111111", 1L, 123,
                "JOHN DOE", "12-31-2026", "Y"
        );
    }

    // --- List cards tests (COCRDLIC.cbl) ---

    @Test
    void listCards_allCards_returnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardEntity> page = new PageImpl<>(List.of(sampleCard), pageable, 1);
        when(cardRepository.findAll(pageable)).thenReturn(page);

        Page<CardResponse> result = cardService.listCards(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("4111111111111111", result.getContent().get(0).cardNumber());
        verify(cardRepository).findAll(pageable);
        verify(cardRepository, never()).findByAccountId(any(), any());
    }

    @Test
    void listCards_filteredByAccountId_returnsFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardEntity> page = new PageImpl<>(List.of(sampleCard), pageable, 1);
        when(cardRepository.findByAccountId(1L, pageable)).thenReturn(page);

        Page<CardResponse> result = cardService.listCards(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).accountId());
        verify(cardRepository).findByAccountId(1L, pageable);
        verify(cardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listCards_emptyResult_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardEntity> page = new PageImpl<>(List.of(), pageable, 0);
        when(cardRepository.findAll(pageable)).thenReturn(page);

        Page<CardResponse> result = cardService.listCards(null, pageable);

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void listCards_pagination_secondPage() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<CardEntity> page = new PageImpl<>(List.of(sampleCard), pageable, 6);
        when(cardRepository.findAll(pageable)).thenReturn(page);

        Page<CardResponse> result = cardService.listCards(null, pageable);

        assertEquals(6, result.getTotalElements());
        assertEquals(1, result.getNumber());
    }

    // --- Get card detail tests (COCRDSLC.cbl) ---

    @Test
    void getCard_found_returnsCardResponse() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardResponse result = cardService.getCard("4111111111111111");

        assertNotNull(result);
        assertEquals("4111111111111111", result.cardNumber());
        assertEquals(1L, result.accountId());
        assertEquals(123, result.cvvCode());
        assertEquals("JOHN DOE", result.embossedName());
        assertEquals("12-31-2026", result.expirationDate());
        assertEquals("Y", result.activeStatus());
    }

    @Test
    void getCard_notFound_throwsCardNotFoundException() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCard("0000000000000000"));
    }

    // --- Update card tests (COCRDUPC.cbl) ---

    @Test
    void updateCard_success_updatesAllFields() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(CardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CardUpdateRequest request = new CardUpdateRequest("JOHN A DOE", "06-30-2027", "N");
        CardResponse result = cardService.updateCard("4111111111111111", request);

        assertEquals("JOHN A DOE", result.embossedName());
        assertEquals("06-30-2027", result.expirationDate());
        assertEquals("N", result.activeStatus());
        // Card number must remain unchanged (immutable PK)
        assertEquals("4111111111111111", result.cardNumber());
    }

    @Test
    void updateCard_partialUpdate_onlyUpdatesProvidedFields() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(CardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CardUpdateRequest request = new CardUpdateRequest("JANE DOE", null, null);
        CardResponse result = cardService.updateCard("4111111111111111", request);

        assertEquals("JANE DOE", result.embossedName());
        assertEquals("12-31-2026", result.expirationDate()); // unchanged
        assertEquals("Y", result.activeStatus()); // unchanged
    }

    @Test
    void updateCard_notFound_throwsCardNotFoundException() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        CardUpdateRequest request = new CardUpdateRequest("JANE DOE", null, null);
        assertThrows(CardNotFoundException.class,
                () -> cardService.updateCard("0000000000000000", request));
    }

    @Test
    void updateCard_blankEmbossedName_throwsValidationException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest("   ", null, null);
        assertThrows(CardValidationException.class,
                () -> cardService.updateCard("4111111111111111", request));
    }

    @Test
    void updateCard_invalidDateFormat_throwsValidationException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(null, "2027-06-30", null);
        assertThrows(CardValidationException.class,
                () -> cardService.updateCard("4111111111111111", request));
    }

    @Test
    void updateCard_invalidMonth_throwsValidationException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(null, "13-01-2027", null);
        assertThrows(CardValidationException.class,
                () -> cardService.updateCard("4111111111111111", request));
    }

    @Test
    void updateCard_invalidDay_throwsValidationException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(null, "01-32-2027", null);
        assertThrows(CardValidationException.class,
                () -> cardService.updateCard("4111111111111111", request));
    }

    @Test
    void updateCard_invalidStatus_throwsValidationException() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(null, null, "X");
        assertThrows(CardValidationException.class,
                () -> cardService.updateCard("4111111111111111", request));
    }

    @Test
    void updateCard_cardNumberNotUpdatable_remainsImmutable() {
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(CardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // CardUpdateRequest has no cardNumber field — it is immutable by design
        CardUpdateRequest request = new CardUpdateRequest("NEW NAME", null, null);
        CardResponse result = cardService.updateCard("4111111111111111", request);

        assertEquals("4111111111111111", result.cardNumber());
    }
}
