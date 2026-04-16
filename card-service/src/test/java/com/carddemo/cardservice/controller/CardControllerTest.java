package com.carddemo.cardservice.controller;

import com.carddemo.cardservice.dto.CardListResponse;
import com.carddemo.cardservice.dto.CardResponse;
import com.carddemo.cardservice.dto.CardUpdateRequest;
import com.carddemo.cardservice.exception.CardNotFoundException;
import com.carddemo.cardservice.exception.CardUpdateException;
import com.carddemo.cardservice.exception.GlobalExceptionHandler;
import com.carddemo.cardservice.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(GlobalExceptionHandler.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    private final CardResponse sampleCardResponse = new CardResponse(
            "4111111111111111", 12345678901L, 123,
            "JOHN DOE", "2027-06-15", "Y"
    );

    // =========================================================================
    // GET /api/v1/cards — List cards
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/cards — admin lists all cards")
    void listCards_admin_returnsOk() throws Exception {
        CardListResponse listResponse = new CardListResponse(
                List.of(sampleCardResponse), 0, 7, 1, 1, false, false);
        when(cardService.listCards(isNull(), eq(true), eq(0), isNull()))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/v1/cards")
                        .param("isAdmin", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards[0].cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.pageSize").value(7));
    }

    @Test
    @DisplayName("GET /api/v1/cards — user filtered by account")
    void listCards_userWithAccount_returnsOk() throws Exception {
        CardListResponse listResponse = new CardListResponse(
                List.of(sampleCardResponse), 0, 7, 1, 1, false, false);
        when(cardService.listCards(eq(12345678901L), eq(false), eq(0), isNull()))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/v1/cards")
                        .param("accountId", "12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].accountId").value(12345678901L));
    }

    @Test
    @DisplayName("GET /api/v1/cards — no results returns 404")
    void listCards_noResults_returns404() throws Exception {
        when(cardService.listCards(any(), anyBoolean(), anyInt(), any()))
                .thenThrow(new CardNotFoundException("no-results"));

        mockMvc.perform(get("/api/v1/cards")
                        .param("accountId", "99999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Did not find cards for this search condition"));
    }

    // =========================================================================
    // GET /api/v1/cards/{cardNum} — Card detail
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/cards/{cardNum} — returns card detail")
    void getCardDetail_existing_returnsOk() throws Exception {
        when(cardService.getCardDetail("4111111111111111"))
                .thenReturn(sampleCardResponse);

        mockMvc.perform(get("/api/v1/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.accountId").value(12345678901L))
                .andExpect(jsonPath("$.cvvCode").value(123))
                .andExpect(jsonPath("$.embossedName").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("2027-06-15"))
                .andExpect(jsonPath("$.activeStatus").value("Y"));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{cardNum} — missing card returns 404")
    void getCardDetail_missing_returns404() throws Exception {
        when(cardService.getCardDetail("9999999999999999"))
                .thenThrow(new CardNotFoundException("9999999999999999"));

        mockMvc.perform(get("/api/v1/cards/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Did not find cards for this search condition"));
    }

    // =========================================================================
    // PUT /api/v1/cards/{cardNum} — Update card
    // =========================================================================

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} — successful update")
    void updateCard_valid_returnsOk() throws Exception {
        CardResponse updatedResponse = new CardResponse(
                "4111111111111111", 12345678901L, 123,
                "JANE DOE", "2028-12-31", "N");
        when(cardService.updateCard(eq("4111111111111111"), any(CardUpdateRequest.class)))
                .thenReturn(updatedResponse);

        String body = objectMapper.writeValueAsString(
                new CardUpdateRequest("JANE DOE", "2028-12-31", "N"));

        mockMvc.perform(put("/api/v1/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("JANE DOE"))
                .andExpect(jsonPath("$.activeStatus").value("N"));
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} — blank name returns 400")
    void updateCard_blankName_returns400() throws Exception {
        String body = """
                {"embossedName":"","expirationDate":"2028-12-31","activeStatus":"Y"}
                """;

        mockMvc.perform(put("/api/v1/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} — invalid status returns 400")
    void updateCard_invalidStatus_returns400() throws Exception {
        String body = """
                {"embossedName":"JOHN DOE","expirationDate":"2028-12-31","activeStatus":"X"}
                """;

        mockMvc.perform(put("/api/v1/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} — no changes returns 400")
    void updateCard_noChanges_returns400() throws Exception {
        when(cardService.updateCard(eq("4111111111111111"), any(CardUpdateRequest.class)))
                .thenThrow(new CardUpdateException(
                        "No change detected with respect to values fetched."));

        String body = objectMapper.writeValueAsString(
                new CardUpdateRequest("JOHN DOE", "2027-06-15", "Y"));

        mockMvc.perform(put("/api/v1/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("No change detected with respect to values fetched."));
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} — name with digits returns 400")
    void updateCard_nameWithDigits_returns400() throws Exception {
        String body = """
                {"embossedName":"JOHN123","expirationDate":"2028-12-31","activeStatus":"Y"}
                """;

        mockMvc.perform(put("/api/v1/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} — missing card returns 404")
    void updateCard_missingCard_returns404() throws Exception {
        when(cardService.updateCard(eq("9999999999999999"), any(CardUpdateRequest.class)))
                .thenThrow(new CardNotFoundException("9999999999999999"));

        String body = objectMapper.writeValueAsString(
                new CardUpdateRequest("JANE DOE", "2028-12-31", "N"));

        mockMvc.perform(put("/api/v1/cards/9999999999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
