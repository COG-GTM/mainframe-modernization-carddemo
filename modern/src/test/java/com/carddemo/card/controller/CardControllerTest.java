package com.carddemo.card.controller;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.exception.CardNotFoundException;
import com.carddemo.card.exception.CardValidationException;
import com.carddemo.card.exception.GlobalExceptionHandler;
import com.carddemo.card.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller layer tests for CardController.
 *
 * Verifies HTTP request/response mapping for endpoints migrated from:
 *   GET  /api/v1/cards           — COCRDLIC.cbl (list)
 *   GET  /api/v1/cards/{num}     — COCRDSLC.cbl (detail)
 *   PUT  /api/v1/cards/{num}     — COCRDUPC.cbl (update)
 */
@WebMvcTest(CardController.class)
@Import({GlobalExceptionHandler.class, CardControllerTest.TestConfig.class})
class CardControllerTest {

    static class TestConfig {
        @Bean
        public CardService cardService() {
            return org.mockito.Mockito.mock(CardService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    private final CardResponse sampleCard = new CardResponse(
            "4111111111111111", 1L, 123,
            "JOHN DOE", "12-31-2026", "Y"
    );

    // --- GET /api/v1/cards ---

    @Test
    void listCards_returnsPagedResults() throws Exception {
        Page<CardResponse> page = new PageImpl<>(List.of(sampleCard));
        when(cardService.listCards(eq(null), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.content[0].accountId").value(1))
                .andExpect(jsonPath("$.content[0].embossedName").value("JOHN DOE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listCards_withAccountIdFilter_returnsFilteredResults() throws Exception {
        Page<CardResponse> page = new PageImpl<>(List.of(sampleCard));
        when(cardService.listCards(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards").param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountId").value(1));
    }

    @Test
    void listCards_withPagination_passesPageable() throws Exception {
        Page<CardResponse> page = new PageImpl<>(List.of(sampleCard));
        when(cardService.listCards(eq(null), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    // --- GET /api/v1/cards/{cardNumber} ---

    @Test
    void getCard_found_returnsCard() throws Exception {
        when(cardService.getCard("4111111111111111")).thenReturn(sampleCard);

        mockMvc.perform(get("/api/v1/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.cvvCode").value(123))
                .andExpect(jsonPath("$.embossedName").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("12-31-2026"))
                .andExpect(jsonPath("$.activeStatus").value("Y"));
    }

    @Test
    void getCard_notFound_returns404() throws Exception {
        when(cardService.getCard("0000000000000000"))
                .thenThrow(new CardNotFoundException("0000000000000000"));

        mockMvc.perform(get("/api/v1/cards/0000000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Card not found: 0000000000000000"));
    }

    // --- PUT /api/v1/cards/{cardNumber} ---

    @Test
    void updateCard_success_returnsUpdatedCard() throws Exception {
        CardResponse updated = new CardResponse(
                "4111111111111111", 1L, 123,
                "JOHN A DOE", "06-30-2027", "N"
        );
        when(cardService.updateCard(eq("4111111111111111"), any(CardUpdateRequest.class)))
                .thenReturn(updated);

        CardUpdateRequest request = new CardUpdateRequest("JOHN A DOE", "06-30-2027", "N");

        mockMvc.perform(put("/api/v1/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("JOHN A DOE"))
                .andExpect(jsonPath("$.expirationDate").value("06-30-2027"))
                .andExpect(jsonPath("$.activeStatus").value("N"));
    }

    @Test
    void updateCard_notFound_returns404() throws Exception {
        when(cardService.updateCard(eq("0000000000000000"), any(CardUpdateRequest.class)))
                .thenThrow(new CardNotFoundException("0000000000000000"));

        CardUpdateRequest request = new CardUpdateRequest("JANE DOE", null, null);

        mockMvc.perform(put("/api/v1/cards/0000000000000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_validationFails_returns400() throws Exception {
        when(cardService.updateCard(eq("4111111111111111"), any(CardUpdateRequest.class)))
                .thenThrow(new CardValidationException("Active status must be 'Y' or 'N'"));

        CardUpdateRequest request = new CardUpdateRequest(null, null, "X");

        mockMvc.perform(put("/api/v1/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
