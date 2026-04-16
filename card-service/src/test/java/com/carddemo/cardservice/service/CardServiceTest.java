package com.carddemo.cardservice.service;

import com.carddemo.cardservice.dto.CardListResponse;
import com.carddemo.cardservice.dto.CardResponse;
import com.carddemo.cardservice.dto.CardUpdateRequest;
import com.carddemo.cardservice.entity.Card;
import com.carddemo.cardservice.exception.CardNotFoundException;
import com.carddemo.cardservice.exception.CardUpdateException;
import com.carddemo.cardservice.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    private CardService cardService;

    private Card sampleCard;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, 7);
        sampleCard = new Card(
                "4111111111111111",
                12345678901L,
                123,
                "JOHN DOE",
                "2027-06-15",
                "Y"
        );
    }

    // =========================================================================
    // listCards tests — ported from COCRDLIC.cbl business rules
    // =========================================================================

    @Test
    @DisplayName("Admin sees all cards when no account filter provided")
    void listCards_adminNoAccountFilter_returnsAllCards() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        CardListResponse response = cardService.listCards(null, true, 0, null);

        assertEquals(1, response.cards().size());
        assertEquals("4111111111111111", response.cards().get(0).cardNum());
        verify(cardRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Admin with account filter sees only that account's cards")
    void listCards_adminWithAccountFilter_returnsFilteredCards() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findByCardAcctId(eq(12345678901L), any(Pageable.class)))
                .thenReturn(page);

        CardListResponse response = cardService.listCards(12345678901L, true, 0, null);

        assertEquals(1, response.cards().size());
        verify(cardRepository).findByCardAcctId(eq(12345678901L), any(Pageable.class));
    }

    @Test
    @DisplayName("Regular user sees only their account's cards")
    void listCards_regularUser_returnsOwnCards() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findByCardAcctId(eq(12345678901L), any(Pageable.class)))
                .thenReturn(page);

        CardListResponse response = cardService.listCards(12345678901L, false, 0, null);

        assertEquals(1, response.cards().size());
        verify(cardRepository).findByCardAcctId(eq(12345678901L), any(Pageable.class));
    }

    @Test
    @DisplayName("Regular user without account ID throws not found")
    void listCards_regularUserNoAccount_throwsNotFound() {
        assertThrows(CardNotFoundException.class,
                () -> cardService.listCards(null, false, 0, null));
    }

    @Test
    @DisplayName("Empty result throws CardNotFoundException — COBOL: NO RECORDS FOUND")
    void listCards_noResults_throwsNotFound() {
        Page<Card> emptyPage = new PageImpl<>(List.of());
        when(cardRepository.findByCardAcctId(eq(99999999999L), any(Pageable.class)))
                .thenReturn(emptyPage);

        assertThrows(CardNotFoundException.class,
                () -> cardService.listCards(99999999999L, false, 0, null));
    }

    @Test
    @DisplayName("Default page size is 7 — COBOL: WS-MAX-SCREEN-LINES VALUE 7")
    void listCards_defaultPageSize_is7() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        cardService.listCards(null, true, 0, null);

        verify(cardRepository).findAll(argThat((Pageable p) -> p.getPageSize() == 7));
    }

    @Test
    @DisplayName("Custom page size overrides default")
    void listCards_customPageSize_respected() {
        Page<Card> page = new PageImpl<>(List.of(sampleCard));
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        cardService.listCards(null, true, 0, 10);

        verify(cardRepository).findAll(argThat((Pageable p) -> p.getPageSize() == 10));
    }

    @Test
    @DisplayName("Pagination metadata is correct")
    void listCards_paginationMetadata_correct() {
        Page<Card> page = new PageImpl<>(
                List.of(sampleCard),
                org.springframework.data.domain.PageRequest.of(0, 7),
                15
        );
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        CardListResponse response = cardService.listCards(null, true, 0, null);

        assertEquals(0, response.page());
        assertEquals(7, response.pageSize());
        assertEquals(15, response.totalElements());
        assertEquals(3, response.totalPages());
        assertTrue(response.hasNext());
        assertFalse(response.hasPrevious());
    }

    // =========================================================================
    // getCardDetail tests — ported from COCRDSLC.cbl
    // =========================================================================

    @Test
    @DisplayName("Get card detail returns correct data")
    void getCardDetail_existingCard_returnsDetail() {
        when(cardRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));

        CardResponse response = cardService.getCardDetail("4111111111111111");

        assertEquals("4111111111111111", response.cardNum());
        assertEquals(12345678901L, response.accountId());
        assertEquals(123, response.cvvCode());
        assertEquals("JOHN DOE", response.embossedName());
        assertEquals("2027-06-15", response.expirationDate());
        assertEquals("Y", response.activeStatus());
    }

    @Test
    @DisplayName("Get card detail for missing card throws not found")
    void getCardDetail_missingCard_throwsNotFound() {
        when(cardRepository.findById("9999999999999999"))
                .thenReturn(Optional.empty());

        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardService.getCardDetail("9999999999999999"));
        assertEquals("Did not find cards for this search condition", ex.getMessage());
    }

    // =========================================================================
    // updateCard tests — ported from COCRDUPC.cbl
    // =========================================================================

    @Test
    @DisplayName("Successful card update changes fields and saves")
    void updateCard_validChanges_updatesSuccessfully() {
        when(cardRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardUpdateRequest request = new CardUpdateRequest(
                "JANE DOE", "2028-12-31", "N");

        CardResponse response = cardService.updateCard("4111111111111111", request);

        verify(cardRepository).save(any(Card.class));
        assertNotNull(response);
    }

    @Test
    @DisplayName("Update with no changes throws — COBOL: No change detected")
    void updateCard_noChanges_throwsUpdateException() {
        when(cardRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(
                "JOHN DOE", "2027-06-15", "Y");

        CardUpdateException ex = assertThrows(CardUpdateException.class,
                () -> cardService.updateCard("4111111111111111", request));
        assertEquals("No change detected with respect to values fetched.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Update for non-existent card throws not found")
    void updateCard_missingCard_throwsNotFound() {
        when(cardRepository.findById("9999999999999999"))
                .thenReturn(Optional.empty());

        CardUpdateRequest request = new CardUpdateRequest(
                "JANE DOE", "2028-12-31", "N");

        assertThrows(CardNotFoundException.class,
                () -> cardService.updateCard("9999999999999999", request));
    }

    @Test
    @DisplayName("Update with invalid expiry year throws — COBOL: Invalid card expiry year")
    void updateCard_invalidExpiryYear_throwsException() {
        when(cardRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(
                "JANE DOE", "2100-06-15", "Y");

        CardUpdateException ex = assertThrows(CardUpdateException.class,
                () -> cardService.updateCard("4111111111111111", request));
        assertEquals("Invalid card expiry year", ex.getMessage());
    }

    @Test
    @DisplayName("Update with year below 1950 throws — COBOL: VALUES 1950 THRU 2099")
    void updateCard_expiryYearTooLow_throwsException() {
        when(cardRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(
                "JANE DOE", "1949-06-15", "Y");

        CardUpdateException ex = assertThrows(CardUpdateException.class,
                () -> cardService.updateCard("4111111111111111", request));
        assertEquals("Invalid card expiry year", ex.getMessage());
    }

    @Test
    @DisplayName("Update with invalid expiry month throws — COBOL: month 1-12")
    void updateCard_invalidExpiryMonth_throwsException() {
        when(cardRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(
                "JANE DOE", "2028-13-15", "Y");

        CardUpdateException ex = assertThrows(CardUpdateException.class,
                () -> cardService.updateCard("4111111111111111", request));
        assertEquals("Card expiry month must be between 1 and 12", ex.getMessage());
    }

    @Test
    @DisplayName("Update with month 0 throws — COBOL: VALUES 1 THRU 12")
    void updateCard_expiryMonthZero_throwsException() {
        when(cardRepository.findById("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));

        CardUpdateRequest request = new CardUpdateRequest(
                "JANE DOE", "2028-00-15", "Y");

        CardUpdateException ex = assertThrows(CardUpdateException.class,
                () -> cardService.updateCard("4111111111111111", request));
        assertEquals("Card expiry month must be between 1 and 12", ex.getMessage());
    }
}
