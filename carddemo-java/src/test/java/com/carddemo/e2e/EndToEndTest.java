package com.carddemo.e2e;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import com.carddemo.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserSecurityRepository userRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private CardRepository cardRepo;
    @Autowired private CardAccountXrefRepository xrefRepo;
    @Autowired private CustomerRepository customerRepo;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        setupTestData();
        adminToken = jwtTokenProvider.createToken("ADMIN001", "ADMIN");
        userToken = jwtTokenProvider.createToken("USER0001", "USER");
    }

    private void setupTestData() {
        if (userRepo.findById("ADMIN001").isEmpty()) {
            UserSecurity admin = new UserSecurity();
            admin.setUsrId("ADMIN001"); admin.setUsrFname("ADMIN"); admin.setUsrLname("USER");
            admin.setUsrPwd("PASSWORD"); admin.setUsrType("A");
            userRepo.save(admin);
        }
        if (userRepo.findById("USER0001").isEmpty()) {
            UserSecurity user = new UserSecurity();
            user.setUsrId("USER0001"); user.setUsrFname("REGULAR"); user.setUsrLname("USER");
            user.setUsrPwd("PASSWORD"); user.setUsrType("U");
            userRepo.save(user);
        }
        if (accountRepo.findById(10000000001L).isEmpty()) {
            Account acct = new Account();
            acct.setAcctId(10000000001L); acct.setActiveStatus("Y");
            acct.setCurrBal(new BigDecimal("1500.00")); acct.setCreditLimit(new BigDecimal("10000.00"));
            acct.setCashCreditLimit(new BigDecimal("2000.00"));
            acct.setCurrCycCredit(BigDecimal.ZERO); acct.setCurrCycDebit(BigDecimal.ZERO);
            acct.setGroupId("GROUP001");
            accountRepo.save(acct);
        }
        if (cardRepo.findById("4111111111111111").isEmpty()) {
            Card card = new Card();
            card.setCardNum("4111111111111111"); card.setCardAcctId(10000000001L);
            card.setCardCvvCd(123); card.setCardEmbossedName("JOHN DOE"); card.setCardActiveStatus("Y");
            cardRepo.save(card);
        }
        if (xrefRepo.findByCardNum("4111111111111111").isEmpty()) {
            CardAccountXref xref = new CardAccountXref();
            xref.setCardNum("4111111111111111"); xref.setAcctId(10000000001L); xref.setCustId(100000001L);
            xrefRepo.save(xref);
        }
    }

    @Test @Order(1)
    void e2e_login() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test @Order(2)
    void e2e_menu() throws Exception {
        mockMvc.perform(get("/menu").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(11));
    }

    @Test @Order(3)
    void e2e_viewAccount() throws Exception {
        mockMvc.perform(get("/accounts/10000000001").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctId").value(10000000001L));
    }

    @Test @Order(4)
    void e2e_listCards() throws Exception {
        mockMvc.perform(get("/cards").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test @Order(5)
    void e2e_getCard() throws Exception {
        mockMvc.perform(get("/cards/4111111111111111").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardEmbossedName").value("JOHN DOE"));
    }

    @Test @Order(6)
    void e2e_listTransactions() throws Exception {
        mockMvc.perform(get("/transactions").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test @Order(7)
    void e2e_reports() throws Exception {
        mockMvc.perform(get("/reports").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test @Order(8)
    void e2e_billPayment() throws Exception {
        mockMvc.perform(post("/payments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"acctId\":10000000001,\"amount\":100.00}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranTypeCd").value("BP"));
    }

    @Test @Order(9)
    void e2e_adminMenu() throws Exception {
        mockMvc.perform(get("/admin/menu").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(6));
    }

    @Test @Order(10)
    void e2e_userManagement() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test @Order(11)
    void e2e_transactionTypes_adminOnly() throws Exception {
        mockMvc.perform(get("/transaction-types").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test @Order(12)
    void e2e_transactionTypes_userDenied() throws Exception {
        mockMvc.perform(get("/transaction-types").header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test @Order(13)
    void e2e_authorizations() throws Exception {
        mockMvc.perform(get("/authorizations").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }
}
