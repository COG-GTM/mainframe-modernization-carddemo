package com.carddemo.web.transaction;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
 * MockMvc end-to-end tests for the ported transaction screens ({@code COTRN00C}/{@code
 * COTRN01C}/{@code COTRN02C}) exercised through {@link TransactionController} with an
 * authenticated principal. Uses the seeded card cross-reference for the add-flow key lookup
 * and inserts a known set of online transactions (there is no online-transaction seed file).
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@WithMockUser(username = "USER0001", roles = "USER")
class TransactionControllerTest {

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
        transactionRepository.save(tran("0000000000000001", "Coffee shop", new BigDecimal("12.50"),
                "2023-01-15 10:00:00.000000"));
        transactionRepository.save(tran("0000000000000002", "Grocery store", new BigDecimal("-5.00"),
                "2023-02-20 09:30:00.000000"));
    }

    private Transaction tran(String id, String desc, BigDecimal amt, String origTs) {
        Transaction t = new Transaction();
        t.setTranId(id);
        t.setTranTypeCd("01");
        t.setTranCatCd(1);
        t.setTranSource("POS");
        t.setTranDesc(desc);
        t.setTranAmt(amt);
        t.setTranMerchantId("000000123");
        t.setTranMerchantName("Test Merchant");
        t.setTranMerchantCity("Seattle");
        t.setTranMerchantZip("98101");
        t.setTranCardNum(card);
        t.setTranOrigTs(origTs);
        t.setTranProcTs(origTs);
        return t;
    }

    private String addBody(String field, String override) {
        // Build a valid add payload, then swap in one field for the negative cases.
        String acctId = "\"acctId\":\"" + acct + "\"";
        String cardNum = "\"cardNum\":\"\"";
        String typeCd = "\"typeCd\":\"01\"";
        String categoryCd = "\"categoryCd\":\"0001\"";
        String source = "\"source\":\"POS\"";
        String description = "\"description\":\"New purchase\"";
        String amount = "\"amount\":\"+00000100.50\"";
        String origDate = "\"origDate\":\"2023-03-01\"";
        String procDate = "\"procDate\":\"2023-03-02\"";
        String merchantId = "\"merchantId\":\"000000123\"";
        String merchantName = "\"merchantName\":\"Store\"";
        String merchantCity = "\"merchantCity\":\"Seattle\"";
        String merchantZip = "\"merchantZip\":\"98101\"";
        String confirm = "\"confirm\":\"Y\"";
        switch (field) {
            case "amount" -> amount = "\"amount\":\"" + override + "\"";
            case "acctId" -> acctId = "\"acctId\":\"" + override + "\"";
            case "confirm" -> confirm = "\"confirm\":\"" + override + "\"";
            case "none" -> {
                acctId = "\"acctId\":\"\"";
                cardNum = "\"cardNum\":\"\"";
            }
            default -> { }
        }
        return "{" + String.join(",", acctId, cardNum, typeCd, categoryCd, source, description,
                amount, origDate, procDate, merchantId, merchantName, merchantCity, merchantZip,
                confirm) + "}";
    }

    // --- COTRN00C list ---------------------------------------------------------------------

    @Test
    void listReturnsPageOrderedByTranIdWithDerivedDate() throws Exception {
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.transactions[0].tranId").value("0000000000000001"))
            .andExpect(jsonPath("$.transactions[0].date").value("01/15/23"))
            .andExpect(jsonPath("$.transactions[0].description").value("Coffee shop"))
            .andExpect(jsonPath("$.transactions[1].tranId").value("0000000000000002"))
            .andExpect(jsonPath("$.moreRecords").value(false))
            .andExpect(jsonPath("$.firstTranId").value("0000000000000001"))
            .andExpect(jsonPath("$.lastTranId").value("0000000000000002"));
    }

    @Test
    void listStartsBrowseAtStartTranId() throws Exception {
        mockMvc.perform(get("/api/transactions").param("startTranId", "0000000000000002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.transactions[0].tranId").value("0000000000000002"));
    }

    @Test
    void listRejectsNonNumericStartKey() throws Exception {
        mockMvc.perform(get("/api/transactions").param("startTranId", "ABC"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Tran ID must be Numeric ..."));
    }

    // --- COTRN01C view ---------------------------------------------------------------------

    @Test
    void viewReturnsTransactionWhenFound() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", "0000000000000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranId").value("0000000000000001"))
            .andExpect(jsonPath("$.cardNum").value(card))
            .andExpect(jsonPath("$.typeCd").value("01"))
            .andExpect(jsonPath("$.categoryCd").value(1))
            .andExpect(jsonPath("$.amount").value(12.50))
            .andExpect(jsonPath("$.merchantName").value("Test Merchant"));
    }

    @Test
    void viewReturnsNotFoundWithCobolMessage() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", "9999999999999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Transaction ID NOT found..."));
    }

    // --- COTRN02C add ----------------------------------------------------------------------

    @Test
    void addValidTransactionGeneratesNextIdAndResolvesCard() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addBody("valid", null)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tranId").value("0000000000000003"))
            .andExpect(jsonPath("$.message")
                .value("Transaction added successfully.  Your Tran ID is 0000000000000003."));

        Transaction saved = transactionRepository.findById("0000000000000003").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(saved.getTranCardNum()).isEqualTo(card);
        org.assertj.core.api.Assertions.assertThat(saved.getTranAmt()).isEqualByComparingTo("100.50");
    }

    @Test
    void addRejectsBadAmountFormat() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addBody("amount", "100.00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Amount should be in format -99999999.99"));
    }

    @Test
    void addRejectsWhenNeitherAccountNorCardProvided() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addBody("none", null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Account or Card Number must be entered..."));
    }

    @Test
    void addRejectsUnknownAccount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addBody("acctId", "99999999999")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Account ID NOT found..."));
    }

    @Test
    void addRequiresConfirmation() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addBody("confirm", "N")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Confirm to add this transaction..."));
    }

    @Test
    void addRejectsInvalidConfirmValue() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addBody("confirm", "X")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid value. Valid values are (Y/N)..."));
    }
}
