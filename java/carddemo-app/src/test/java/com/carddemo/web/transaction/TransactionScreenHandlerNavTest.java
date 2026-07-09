package com.carddemo.web.transaction;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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

import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;

/**
 * Verifies the CS-6 {@link ScreenHandler} beans plug into the CS-3 navigation framework: a
 * turn routed to {@code COTRN00}/{@code COTRN01}/{@code COTRN02} through {@code /api/nav} is
 * dispatched to the transaction handlers (list model, view record, add confirmation).
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@WithMockUser(username = "USER0001", roles = "USER")
class TransactionScreenHandlerNavTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;

    private MockMvc mockMvc;
    private String card;
    private String acct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        CardXref xref = cardXrefRepository.findAll().get(0);
        card = xref.getXrefCardNum();
        acct = xref.getXrefAcctId();

        transactionRepository.deleteAll();
        Transaction t = new Transaction();
        t.setTranId("0000000000000001");
        t.setTranTypeCd("01");
        t.setTranCatCd(1);
        t.setTranSource("POS");
        t.setTranDesc("Coffee shop");
        t.setTranAmt(new BigDecimal("12.50"));
        t.setTranMerchantId("000000123");
        t.setTranMerchantName("Test Merchant");
        t.setTranMerchantCity("Seattle");
        t.setTranMerchantZip("98101");
        t.setTranCardNum(card);
        t.setTranOrigTs("2023-01-15 10:00:00.000000");
        t.setTranProcTs("2023-01-15 10:00:00.000000");
        transactionRepository.save(t);
    }

    @Test
    void listHandlerDispatchedForCt00() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"CT00\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentProgram").value("TRANSACTION_LIST"))
            .andExpect(jsonPath("$.toProgram").value("COTRN00C"))
            .andExpect(jsonPath("$.model.count").value(1))
            .andExpect(jsonPath("$.model.transactions[0].tranId").value("0000000000000001"));
    }

    @Test
    void viewHandlerDispatchedForCt01() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"CT01\",\"fields\":{\"tranId\":\"0000000000000001\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentProgram").value("TRANSACTION_VIEW"))
            .andExpect(jsonPath("$.model.tranId").value("0000000000000001"))
            .andExpect(jsonPath("$.model.merchantName").value("Test Merchant"));
    }

    @Test
    void viewHandlerReportsNotFoundMessageForCt01() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));

        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"CT01\",\"fields\":{\"tranId\":\"9999999999999999\"}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Transaction ID NOT found..."));
    }

    @Test
    void addHandlerDispatchedForCt02() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/nav/signon").session(session));

        String fields = "{\"tranId\":\"CT02\",\"fields\":{"
                + "\"acctId\":\"" + acct + "\",\"typeCd\":\"01\",\"categoryCd\":\"0001\","
                + "\"source\":\"POS\",\"description\":\"New purchase\",\"amount\":\"+00000100.50\","
                + "\"origDate\":\"2023-03-01\",\"procDate\":\"2023-03-02\",\"merchantId\":\"000000123\","
                + "\"merchantName\":\"Store\",\"merchantCity\":\"Seattle\",\"merchantZip\":\"98101\","
                + "\"confirm\":\"Y\"}}";
        mockMvc.perform(post("/api/nav").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(fields))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentProgram").value("TRANSACTION_ADD"))
            .andExpect(jsonPath("$.message")
                .value("Transaction added successfully.  Your Tran ID is 0000000000000002."));
    }
}
