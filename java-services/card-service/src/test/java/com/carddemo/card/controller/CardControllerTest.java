package com.carddemo.card.controller;

import com.carddemo.card.dto.CardDto;
import com.carddemo.card.dto.CardListResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.exception.GlobalExceptionHandler;
import com.carddemo.card.exception.ResourceNotFoundException;
import com.carddemo.card.model.CardCrossReference;
import com.carddemo.card.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(CardController.class)
@Import(GlobalExceptionHandler.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    private CardDto sampleCardDto() {
        return CardDto.builder()
                .cardNumber("4111111111111111")
                .accountId("00000000001")
                .cvvCode("123")
                .embossedName("JOHN DOE")
                .expirationDate("2026-12-01")
                .activeStatus("Y")
                .build();
    }

    @Test
    void listCards_returnsPagedResults() throws Exception {
        CardListResponse response = CardListResponse.builder()
                .cards(List.of(sampleCardDto()))
                .page(0)
                .size(7)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        when(cardService.getCardsByAccountId("00000000001", 0, 7)).thenReturn(response);

        mockMvc.perform(get("/api/cards")
                        .param("accountId", "00000000001")
                        .param("page", "0")
                        .param("size", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards[0].cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void listCards_missingAccountId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCard_found() throws Exception {
        when(cardService.getCardByNumber("4111111111111111")).thenReturn(sampleCardDto());

        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.embossedName").value("JOHN DOE"))
                .andExpect(jsonPath("$.activeStatus").value("Y"));
    }

    @Test
    void getCard_notFound() throws Exception {
        when(cardService.getCardByNumber("0000000000000000"))
                .thenThrow(new ResourceNotFoundException("Card", "cardNumber", "0000000000000000"));

        mockMvc.perform(get("/api/cards/0000000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateCard_validRequest() throws Exception {
        CardDto updated = sampleCardDto();
        updated.setEmbossedName("JANE DOE");
        updated.setActiveStatus("N");

        when(cardService.updateCard(eq("4111111111111111"), any(CardUpdateRequest.class)))
                .thenReturn(updated);

        CardUpdateRequest request = CardUpdateRequest.builder()
                .embossedName("JANE DOE")
                .activeStatus("N")
                .expirationDate("2027-06-15")
                .build();

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("JANE DOE"))
                .andExpect(jsonPath("$.activeStatus").value("N"));
    }

    @Test
    void updateCard_invalidActiveStatus_returnsBadRequest() throws Exception {
        CardUpdateRequest request = CardUpdateRequest.builder()
                .activeStatus("X")
                .build();

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCard_invalidEmbossedName_returnsBadRequest() throws Exception {
        CardUpdateRequest request = CardUpdateRequest.builder()
                .embossedName("JOHN123")
                .build();

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCard_invalidExpirationDateFormat_returnsBadRequest() throws Exception {
        CardUpdateRequest request = CardUpdateRequest.builder()
                .expirationDate("12-2026-01")
                .build();

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCrossReference_found() throws Exception {
        CardCrossReference xref = CardCrossReference.builder()
                .cardNumber("4111111111111111")
                .customerId("000000001")
                .accountId("00000000001")
                .build();

        when(cardService.getCrossReference("4111111111111111")).thenReturn(xref);

        mockMvc.perform(get("/api/cards/xref/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.customerId").value("000000001"))
                .andExpect(jsonPath("$.accountId").value("00000000001"));
    }

    @Test
    void getCrossReference_notFound() throws Exception {
        when(cardService.getCrossReference("0000000000000000"))
                .thenThrow(new ResourceNotFoundException("CardCrossReference", "cardNumber", "0000000000000000"));

        mockMvc.perform(get("/api/cards/xref/0000000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
