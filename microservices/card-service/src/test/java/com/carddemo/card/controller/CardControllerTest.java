package com.carddemo.card.controller;

import com.carddemo.card.dto.CardResponse;
import com.carddemo.card.dto.CardUpdateRequest;
import com.carddemo.card.dto.CardXrefResponse;
import com.carddemo.card.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("deprecation")
    @MockBean
    private CardService cardService;

    private final CardResponse sampleCard = new CardResponse(
            "4111111111111111",
            "00000000001",
            "123",
            "JOHN A SMITH",
            "2026-12-01",
            "Y"
    );

    @Test
    void listCards_noFilter_returnsAll() throws Exception {
        Page<CardResponse> page = new PageImpl<>(List.of(sampleCard));
        when(cardService.listCards(null, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.content[0].cardAcctId").value("00000000001"));
    }

    @Test
    void listCards_withAccountIdFilter() throws Exception {
        Page<CardResponse> page = new PageImpl<>(List.of(sampleCard));
        when(cardService.listCards("00000000001", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/cards")
                        .param("accountId", "00000000001")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardAcctId").value("00000000001"));
    }

    @Test
    void getCard_found() throws Exception {
        when(cardService.getCardByNumber("4111111111111111"))
                .thenReturn(Optional.of(sampleCard));

        mockMvc.perform(get("/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.cardEmbossedName").value("JOHN A SMITH"))
                .andExpect(jsonPath("$.cardCvvCd").value("123"));
    }

    @Test
    void getCard_notFound() throws Exception {
        when(cardService.getCardByNumber("9999999999999999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/cards/9999999999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_found() throws Exception {
        CardResponse updated = new CardResponse(
                "4111111111111111", "00000000001", "123",
                "UPDATED NAME", "2028-01-15", "N"
        );
        when(cardService.updateCard(eq("4111111111111111"), any(CardUpdateRequest.class)))
                .thenReturn(Optional.of(updated));

        CardUpdateRequest request = new CardUpdateRequest("UPDATED NAME", "2028-01-15", "N");

        mockMvc.perform(put("/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardEmbossedName").value("UPDATED NAME"))
                .andExpect(jsonPath("$.cardActiveStatus").value("N"));
    }

    @Test
    void updateCard_notFound() throws Exception {
        when(cardService.updateCard(eq("9999999999999999"), any(CardUpdateRequest.class)))
                .thenReturn(Optional.empty());

        CardUpdateRequest request = new CardUpdateRequest("NEW NAME", null, null);

        mockMvc.perform(put("/cards/9999999999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getXrefByAccountId_found() throws Exception {
        List<CardXrefResponse> xrefs = List.of(
                new CardXrefResponse("4111111111111111", "000000001", "00000000001"),
                new CardXrefResponse("4111111111112222", "000000001", "00000000001")
        );
        when(cardService.getXrefByAccountId("00000000001")).thenReturn(xrefs);

        mockMvc.perform(get("/cards/xref/00000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$[0].custId").value("000000001"))
                .andExpect(jsonPath("$[0].acctId").value("00000000001"))
                .andExpect(jsonPath("$[1].cardNum").value("4111111111112222"));
    }

    @Test
    void getXrefByAccountId_empty() throws Exception {
        when(cardService.getXrefByAccountId("99999999999"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/cards/xref/99999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void healthCheck() throws Exception {
        mockMvc.perform(get("/cards/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("card-service"));
    }
}
