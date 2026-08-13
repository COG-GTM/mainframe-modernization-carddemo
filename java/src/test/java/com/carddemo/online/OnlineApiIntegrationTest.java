package com.carddemo.online;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.model.entity.Card;
import com.carddemo.model.entity.CardXref;
import com.carddemo.online.account.AccountViewService;
import com.carddemo.online.card.CardDetailService;
import com.carddemo.online.card.CardListService;
import com.carddemo.online.card.CardWorkArea;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End to end checks of the online account and card transactions (COACTVWC, COCRDLIC,
 * COCRDSLC) over the REST layer, against the sample VSAM extracts in app/data/ASCII.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OnlineApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;

    private CardXref sampleXref;
    private Card lowestCard;

    @BeforeEach
    void setUp() {
        sampleXref = cardXrefRepository.findAll().get(0);
        lowestCard = cardRepository.findAll().stream()
                .min((left, right) -> left.getCardNumber().compareTo(right.getCardNumber()))
                .orElseThrow();
    }

    @Test
    void viewsAnAccountFromTheSampleData() throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("accountId", String.format("%011d", sampleXref.getAccountId())));

        mockMvc.perform(post("/api/accounts/view").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountFound").value(true))
                .andExpect(jsonPath("$.customerFound").value(true))
                .andExpect(jsonPath("$.infoMessage").value(AccountViewService.INFORM_OUTPUT))
                .andExpect(jsonPath("$.cardNumber").value(sampleXref.getCardNumber()));
    }

    @Test
    void rejectsAnInvalidAccountFilter() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("accountId", "abc"));

        mockMvc.perform(post("/api/accounts/view").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountFound").value(false))
                .andExpect(jsonPath("$.errorMessage").value(AccountViewService.ACCT_FILTER_NOT_VALID));
    }

    @Test
    void pagesThroughTheCardListAndKeepsThePageKeysInTheSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/cards/list").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("action", "ENTER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.rows.length()").value(CardWorkArea.MAX_SCREEN_LINES))
                .andExpect(jsonPath("$.rows[0].cardNumber").value(lowestCard.getCardNumber()))
                .andExpect(jsonPath("$.infoMessage").value(CardListService.INFORM_REC_ACTIONS));

        List<String> firstPage = cardRepository.findAll().stream()
                .map(Card::getCardNumber)
                .sorted()
                .toList();

        mockMvc.perform(post("/api/cards/list").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("action", "PF8"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.rows[0].cardNumber").value(firstPage.get(7)));

        mockMvc.perform(post("/api/cards/list").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("action", "PF7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.rows[0].cardNumber").value(firstPage.get(0)));
    }

    @Test
    void showsCardDetailForACardFromTheList() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "action", "ENTER",
                "accountId", String.format("%011d", lowestCard.getAccountId()),
                "cardNumber", lowestCard.getCardNumber()));

        mockMvc.perform(post("/api/cards/detail").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardFound").value(true))
                .andExpect(jsonPath("$.cardNumber").value(lowestCard.getCardNumber()))
                .andExpect(jsonPath("$.embossedName").value(lowestCard.getEmbossedName()))
                .andExpect(jsonPath("$.infoMessage").value(CardDetailService.FOUND_CARDS_FOR_ACCOUNT));
    }

    @Test
    void cardUpdateShowsDetailsBeforeItAsksForConfirmation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String key = objectMapper.writeValueAsString(java.util.Map.of(
                "action", "ENTER",
                "accountId", String.format("%011d", lowestCard.getAccountId()),
                "cardNumber", lowestCard.getCardNumber()));

        mockMvc.perform(post("/api/cards/update").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changeAction").value("S"))
                .andExpect(jsonPath("$.data.cardNumber").value(lowestCard.getCardNumber()));

        String changed = objectMapper.writeValueAsString(java.util.Map.of(
                "action", "ENTER",
                "accountId", String.format("%011d", lowestCard.getAccountId()),
                "cardNumber", lowestCard.getCardNumber(),
                "cardName", "NEW CARD HOLDER",
                "activeStatus", "N",
                "expiryMonth", "12",
                "expiryYear", "2030"));

        mockMvc.perform(post("/api/cards/update").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(changed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changeAction").value("N"));

        String confirm = changed.replace("\"ENTER\"", "\"PF5\"");
        mockMvc.perform(post("/api/cards/update").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(confirm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changeAction").value("C"));

        Card stored = cardRepository.findById(lowestCard.getCardNumber()).orElseThrow();
        assertThat(stored.getEmbossedName()).isEqualTo("NEW CARD HOLDER");
        assertThat(stored.getActiveStatus()).isEqualTo("N");
        assertThat(stored.getExpirationDate()).startsWith("2030-12-");
    }
}
