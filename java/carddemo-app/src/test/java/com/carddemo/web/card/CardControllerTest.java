package com.carddemo.web.card;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CommareaSessionStore;

/**
 * End-to-end MockMvc tests for the ported card endpoints ({@code COCRDLIC}/{@code COCRDSLC}/
 * {@code COCRDUPC}), driven against the real seed cards ({@code carddata.txt}, 50 rows, one
 * card per account) with an authenticated principal.
 *
 * <p>Lowest card number in the seed set is {@code 0500024453765740} (account
 * {@code 00000000050}, "Aniya Von", expiry {@code 2023-03-09}, status {@code Y}).</p>
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
class CardControllerTest {

    private static final String FIRST_CARD = "0500024453765740";
    private static final String FIRST_ACCT = "00000000050";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CommareaSessionStore commareaStore;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/cards"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void adminListsFirstPageOfSevenWithNextFlag() throws Exception {
        mockMvc.perform(get("/api/cards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.pageSize").value(7))
            .andExpect(jsonPath("$.cards.length()").value(7))
            .andExpect(jsonPath("$.hasNextPage").value(true))
            .andExpect(jsonPath("$.hasPreviousPage").value(false))
            .andExpect(jsonPath("$.cards[0].cardNumber").value(FIRST_CARD))
            .andExpect(jsonPath("$.cards[0].accountId").value(FIRST_ACCT));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void adminFiltersByCardNumber() throws Exception {
        mockMvc.perform(get("/api/cards").param("cardNumber", FIRST_CARD))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards.length()").value(1))
            .andExpect(jsonPath("$.cards[0].cardNumber").value(FIRST_CARD));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void listRejectsBadAccountFilter() throws Exception {
        mockMvc.perform(get("/api/cards").param("accountId", "123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void regularUserIsRestrictedToCommareaAccount() throws Exception {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.getAccountInfo().setAcctId(FIRST_ACCT);
        commareaStore.save(session, commarea);

        // No filter supplied, but the browse is pinned to the COMMAREA account (1 card).
        mockMvc.perform(get("/api/cards").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards.length()").value(1))
            .andExpect(jsonPath("$.cards[0].accountId").value(FIRST_ACCT));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void detailReturnsCardFields() throws Exception {
        mockMvc.perform(get("/api/cards/{cardNumber}", FIRST_CARD))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardNumber").value(FIRST_CARD))
            .andExpect(jsonPath("$.accountId").value(FIRST_ACCT))
            .andExpect(jsonPath("$.embossedName").value("Aniya Von"))
            .andExpect(jsonPath("$.expirationDate").value("2023-03-09"))
            .andExpect(jsonPath("$.activeStatus").value("Y"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void detailNotFoundReturns404WithCobolMessage() throws Exception {
        mockMvc.perform(get("/api/cards/{cardNumber}", "9999999999999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Did not find cards for this search condition"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void detailRejectsNonNumericCardNumber() throws Exception {
        mockMvc.perform(get("/api/cards/{cardNumber}", "notacardnumber00"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Card number if supplied must be a 16 digit number"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void updateAppliesChangesAndPersists() throws Exception {
        // Dedicated card (Enrico Rosenbaum, acct 00000000002, expiry 2024-08-11) so this
        // mutating test does not disturb the read-only assertions on FIRST_CARD.
        String card = "0923877193247330";
        String body = "{\"embossedName\":\"Renamed Holder\",\"activeStatus\":\"N\","
            + "\"expiryMonth\":\"11\",\"expiryYear\":\"2030\"}";

        mockMvc.perform(put("/api/cards/{cardNumber}", card)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Changes committed to database"))
            .andExpect(jsonPath("$.card.embossedName").value("Renamed Holder"))
            .andExpect(jsonPath("$.card.activeStatus").value("N"))
            // Expiry day (11) preserved from the stored record; year/month replaced.
            .andExpect(jsonPath("$.card.expirationDate").value("2030-11-11"));

        // Re-read confirms the write was committed.
        mockMvc.perform(get("/api/cards/{cardNumber}", card))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.embossedName").value("Renamed Holder"))
            .andExpect(jsonPath("$.expirationDate").value("2030-11-11"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void updateRejectsInvalidStatus() throws Exception {
        String body = "{\"embossedName\":\"Some Name\",\"activeStatus\":\"X\","
            + "\"expiryMonth\":\"11\",\"expiryYear\":\"2030\"}";

        mockMvc.perform(put("/api/cards/{cardNumber}", "0683586198171516")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Card Active Status must be Y or N"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void updateNotFoundReturns404() throws Exception {
        String body = "{\"embossedName\":\"Some Name\",\"activeStatus\":\"Y\","
            + "\"expiryMonth\":\"11\",\"expiryYear\":\"2030\"}";

        mockMvc.perform(put("/api/cards/{cardNumber}", "9999999999999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Did not find cards for this search condition"));
    }
}
