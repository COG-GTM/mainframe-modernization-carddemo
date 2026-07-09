package com.carddemo.e2e;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * CS-15 end-to-end <em>online golden path</em>: a single realistic conversation that spans every
 * WAVE 1–3 online module, driven through the real REST surface with the real seed data
 * ({@code carddemo.seed.enabled=true}) and a persisted {@link MockHttpSession} — so the CICS
 * pseudo-conversational session/COMMAREA behaviour (sign-on routing, security context reuse) is
 * exercised, not just individual endpoints.
 *
 * <p>The flow reproduces the legacy screen path an operator would walk:
 * {@code COSGN00C} sign-on → {@code COADM01C}/{@code COMEN01C} menu routing → {@code COACTVWC}
 * account view → {@code COCRDLIC} card list → {@code COTRN02C}/{@code COTRN01C} add + view a
 * transaction → {@code COBIL00C} bill payment → {@code CORPT00C} report → the admin
 * {@code COUSR00C}/{@code 01C}/{@code 02C}/{@code 03C} user-maintenance CRUD.</p>
 *
 * <p>Seed reference facts used as anchors: account {@code 00000000001} (customer
 * {@code 000000001} "Immanuel", card {@code 9680294154603697}, current balance {@code 194.00}),
 * admin {@code ADMIN001} / user {@code USER0001} (password {@code PASSWORD}). The class is
 * {@code @Transactional} so the mutations (bill-payment, transaction add, user CRUD) roll back
 * and leave the shared in-memory seed untouched for sibling test classes.</p>
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@Transactional
class OnlineGoldenPathE2ETest {

    private static final String ADMIN = "ADMIN001";
    private static final String USER = "USER0001";
    private static final String PASSWORD = "PASSWORD";
    private static final String ACCT = "00000000001";
    private static final String CARD = "9680294154603697";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static String signonBody(String userId, String password) {
        return "{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}";
    }

    /** Authenticate through {@code COSGN00C} and return the established (authenticated) session. */
    private MockHttpSession signon(String userId, String expectedRole, String expectedDestination)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signon")
                    .contentType(MediaType.APPLICATION_JSON).content(signonBody(userId, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.role").value(expectedRole))
                .andExpect(jsonPath("$.destination").value(expectedDestination))
                .andReturn();
        HttpSession session = result.getRequest().getSession(false);
        return (MockHttpSession) session;
    }

    @Test
    void adminWalksTheFullOnlineGoldenPath() throws Exception {
        // 1. COSGN00C sign-on: admin routes to the admin menu, security context lives in session.
        MockHttpSession session = signon(ADMIN, "ROLE_ADMIN", "ADMIN_MENU");

        // 2. COSGN00C post-auth routing (COMMAREA established in the session) → COADM01C.
        mockMvc.perform(post("/api/nav/signon").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(ADMIN))
            .andExpect(jsonPath("$.userType").value("ADMIN"))
            .andExpect(jsonPath("$.currentProgram").value("ADMIN_MENU"))
            .andExpect(jsonPath("$.toProgram").value("COADM01C"));

        // 3. COADM01C admin menu is reachable and lists options.
        mockMvc.perform(get("/api/menu/admin").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.programName").value("COADM01C"))
            .andExpect(jsonPath("$.options").isArray());

        // 4. COACTVWC account view → the seed account + its customer.
        mockMvc.perform(get("/api/accounts/{acctId}", ACCT).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctId").value(ACCT))
            .andExpect(jsonPath("$.custId").value("000000001"))
            .andExpect(jsonPath("$.firstName").value("Immanuel"));

        // 5. COCRDLIC card list filtered to that account (admin may filter freely) → includes CARD.
        mockMvc.perform(get("/api/cards").param("accountId", ACCT).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards").isArray())
            .andExpect(jsonPath("$.cards[?(@.cardNumber == '" + CARD + "')]").exists());

        // 6. COCRDSLC card detail.
        mockMvc.perform(get("/api/cards/{cardNumber}", CARD).param("accountId", ACCT).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardNumber").value(CARD));

        // 7. COTRN02C add a transaction against the account; a new 16-digit id is generated.
        MvcResult added = mockMvc.perform(post("/api/transactions").session(session)
                    .contentType(MediaType.APPLICATION_JSON).content(addTransactionBody()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tranId").isNotEmpty())
            .andReturn();
        String newTranId = com.jayway.jsonpath.JsonPath.read(
                added.getResponse().getContentAsString(), "$.tranId");

        // 8. COTRN01C view the transaction just added (resolves back to the account's card).
        mockMvc.perform(get("/api/transactions/{tranId}", newTranId).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranId").value(newTranId))
            .andExpect(jsonPath("$.cardNum").value(CARD))
            .andExpect(jsonPath("$.amount").value(100.50));

        // 9. COBIL00C bill payment: unconfirmed prompt shows the 194.00 balance, then pay to zero.
        mockMvc.perform(post("/api/billpay").session(session)
                    .contentType(MediaType.APPLICATION_JSON).content(billPayBody(ACCT, null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmationRequired").value(true))
            .andExpect(jsonPath("$.currentBalance").value(194.00));
        mockMvc.perform(post("/api/billpay").session(session)
                    .contentType(MediaType.APPLICATION_JSON).content(billPayBody(ACCT, "Y")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paid").value(true))
            .andExpect(jsonPath("$.newBalance").value(0.00));

        // 10. CORPT00C generate a custom transaction report for the current-year window.
        mockMvc.perform(post("/api/reports/transactions").session(session)
                    .contentType(MediaType.APPLICATION_JSON).content(reportBody()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.submitted").value(true))
            .andExpect(jsonPath("$.reportName").value("Custom"));

        // 11. COUSR00C list → COUSR01C add → COUSR02C update → COUSR03C delete (admin only).
        mockMvc.perform(get("/api/admin/users").session(session).param("size", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users[0].userId").value("ADMIN001"));

        String createBody = "{\"userId\":\"E2EUSR01\",\"firstName\":\"E2E\",\"lastName\":\"USER\","
                + "\"password\":\"PASS1\",\"userType\":\"U\"}";
        mockMvc.perform(post("/api/admin/users").session(session)
                    .contentType(MediaType.APPLICATION_JSON).content(createBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value("E2EUSR01"))
            .andExpect(jsonPath("$.message").value("User E2EUSR01 has been added ..."));

        String updateBody = "{\"firstName\":\"E2E\",\"lastName\":\"RENAMED\","
                + "\"password\":\"PASS2\",\"userType\":\"U\"}";
        mockMvc.perform(put("/api/admin/users/{userId}", "E2EUSR01").session(session)
                    .contentType(MediaType.APPLICATION_JSON).content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastName").value("RENAMED"))
            .andExpect(jsonPath("$.message").value("User E2EUSR01 has been updated ..."));

        mockMvc.perform(delete("/api/admin/users/{userId}", "E2EUSR01").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User E2EUSR01 has been deleted ..."));
    }

    @Test
    void regularUserWalksTheUserGoldenPathAndIsDeniedAdminFunctions() throws Exception {
        // COSGN00C sign-on: a regular user routes to the main menu (COMEN01C), not the admin menu.
        MockHttpSession session = signon(USER, "ROLE_USER", "MAIN_MENU");

        mockMvc.perform(post("/api/nav/signon").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userType").value("USER"))
            .andExpect(jsonPath("$.currentProgram").value("MAIN_MENU"))
            .andExpect(jsonPath("$.toProgram").value("COMEN01C"));

        // COMEN01C main menu is visible to the user.
        mockMvc.perform(get("/api/menu").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.programName").value("COMEN01C"));

        // Account view + card list + report all work for a signed-on user.
        mockMvc.perform(get("/api/accounts/{acctId}", ACCT).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctId").value(ACCT));
        mockMvc.perform(get("/api/transactions").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(0)));

        // The admin menu and the user-admin CRUD are gated (COADM01C / CA00) → 403.
        mockMvc.perform(get("/api/menu/admin").session(session))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users").session(session))
            .andExpect(status().isForbidden());
    }

    /** A fully valid {@code COTRN02C} add payload against the seed account, confirmed. */
    private String addTransactionBody() {
        return "{"
                + "\"acctId\":\"" + ACCT + "\","
                + "\"cardNum\":\"\","
                + "\"typeCd\":\"01\","
                + "\"categoryCd\":\"0001\","
                + "\"source\":\"POS\","
                + "\"description\":\"E2E golden path purchase\","
                + "\"amount\":\"+00000100.50\","
                + "\"origDate\":\"2023-03-01\","
                + "\"procDate\":\"2023-03-02\","
                + "\"merchantId\":\"000000123\","
                + "\"merchantName\":\"Store\","
                + "\"merchantCity\":\"Seattle\","
                + "\"merchantZip\":\"98101\","
                + "\"confirm\":\"Y\"}";
    }

    private static String billPayBody(String accountId, String confirm) {
        StringBuilder sb = new StringBuilder("{\"accountId\":\"").append(accountId).append("\"");
        if (confirm != null) {
            sb.append(",\"confirm\":\"").append(confirm).append("\"");
        }
        return sb.append("}").toString();
    }

    private static String reportBody() {
        return "{\"reportType\":\"CUSTOM\",\"startMonth\":\"01\",\"startDay\":\"01\","
                + "\"startYear\":\"2023\",\"endMonth\":\"12\",\"endDay\":\"31\","
                + "\"endYear\":\"2024\",\"confirm\":\"Y\"}";
    }
}
