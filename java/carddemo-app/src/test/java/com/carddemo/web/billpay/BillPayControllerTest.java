package com.carddemo.web.billpay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.carddemo.domain.Account;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;

/**
 * End-to-end tests for the ported {@code COBIL00C} bill-payment flow, driven against the real
 * seed data ({@code acctdata.txt} / {@code cardxref.txt}) and an authenticated principal.
 *
 * <p>Seed account {@code 00000000001} has current balance {@code 194.00} and card
 * {@code 9680294154603697}. Each test runs in a rolled-back transaction so its balance
 * mutations do not leak into other tests.</p>
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "USER0001", roles = "USER")
class BillPayControllerTest {

    private static final String ACCT = "00000000001";
    private static final BigDecimal BALANCE = new BigDecimal("194.00");

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static String body(String accountId, String confirm) {
        StringBuilder sb = new StringBuilder("{\"accountId\":\"").append(accountId).append("\"");
        if (confirm != null) {
            sb.append(",\"confirm\":\"").append(confirm).append("\"");
        }
        return sb.append("}").toString();
    }

    @Test
    void showsBalanceAndPromptsToConfirmWhenNotConfirmed() throws Exception {
        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body(ACCT, null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmationRequired").value(true))
            .andExpect(jsonPath("$.paid").value(false))
            .andExpect(jsonPath("$.currentBalance").value(194.00))
            .andExpect(jsonPath("$.message").value("Confirm to make a bill payment..."));
    }

    @Test
    void paymentDrawsBalanceToZeroAndWritesPaymentTransaction() throws Exception {
        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body(ACCT, "Y")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paid").value(true))
            .andExpect(jsonPath("$.newBalance").value(0.00))
            .andExpect(jsonPath("$.transactionId").isNotEmpty())
            .andExpect(jsonPath("$.message").value(
                    org.hamcrest.Matchers.startsWith("Payment successful.  Your Transaction ID is")));

        Account after = accountRepository.findById(ACCT).orElseThrow();
        assertThat(after.getAcctCurrBal()).isEqualByComparingTo("0.00");
        assertThat(after.getAcctCurrBal().scale()).isEqualTo(2);

        List<Transaction> payments = transactionRepository.findByTranCardNum("9680294154603697")
                .stream().filter(t -> "BILL PAYMENT".equals(t.getTranMerchantName())).toList();
        assertThat(payments).hasSize(1);
        Transaction payment = payments.get(0);
        assertThat(payment.getTranTypeCd()).isEqualTo("02");
        assertThat(payment.getTranCatCd()).isEqualTo(2);
        assertThat(payment.getTranAmt()).isEqualByComparingTo(BALANCE);
        assertThat(payment.getTranSource()).isEqualTo("POS TERM");
        assertThat(payment.getTranDesc()).isEqualTo("BILL PAYMENT - ONLINE");
    }

    @Test
    void payingAnAlreadyZeroBalanceReportsNothingToPay() throws Exception {
        // First payment zeroes the balance; the second turn hits "You have nothing to pay...".
        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body(ACCT, "Y")))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body(ACCT, "Y")))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("You have nothing to pay..."));
    }

    @Test
    void declinedConfirmationMakesNoPayment() throws Exception {
        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body(ACCT, "N")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paid").value(false))
            .andExpect(jsonPath("$.confirmationRequired").value(false));

        assertThat(accountRepository.findById(ACCT).orElseThrow().getAcctCurrBal())
                .isEqualByComparingTo(BALANCE);
    }

    @Test
    void emptyAccountIdReturnsCobolMessage() throws Exception {
        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body("", null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Acct ID can NOT be empty..."));
    }

    @Test
    void unknownAccountReturnsCobolMessage() throws Exception {
        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body("00000099999", "Y")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Account ID NOT found..."));
    }

    @Test
    void invalidConfirmValueReturnsCobolMessage() throws Exception {
        mockMvc.perform(post("/api/billpay")
                .contentType(MediaType.APPLICATION_JSON).content(body(ACCT, "X")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid value. Valid values are (Y/N)..."));
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/billpay").with(anonymous())
                .contentType(MediaType.APPLICATION_JSON).content(body(ACCT, "Y")))
            .andExpect(status().isUnauthorized());
    }
}
