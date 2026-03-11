package com.carddemo.integration;

import com.carddemo.entity.Account;
import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private UserSecurityRepository userSecurityRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;
    private String token;

    @BeforeEach
    void setUp() {
        if (userSecurityRepository.findById("ADMIN001").isEmpty()) {
            UserSecurity admin = new UserSecurity();
            admin.setUsrId("ADMIN001"); admin.setUsrFname("ADMIN"); admin.setUsrLname("USER");
            admin.setUsrPwd(passwordEncoder.encode("PASSWORD")); admin.setUsrType("A");
            userSecurityRepository.save(admin);
        }
        token = jwtTokenProvider.createToken("ADMIN001", "ADMIN");

        if (accountRepository.findById(10000000001L).isEmpty()) {
            Account acct = new Account();
            acct.setAcctId(10000000001L); acct.setActiveStatus("Y");
            acct.setCurrBal(new BigDecimal("1500.00")); acct.setCreditLimit(new BigDecimal("10000.00"));
            acct.setCashCreditLimit(new BigDecimal("2000.00"));
            acct.setCurrCycCredit(BigDecimal.ZERO); acct.setCurrCycDebit(BigDecimal.ZERO);
            acct.setGroupId("GROUP001");
            accountRepository.save(acct);
        }
    }

    @Test
    void getAccount_success() throws Exception {
        mockMvc.perform(get("/accounts/10000000001")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctId").value(10000000001L));
    }

    @Test
    void getAccount_unauthorized() throws Exception {
        mockMvc.perform(get("/accounts/10000000001"))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateAccount() throws Exception {
        mockMvc.perform(put("/accounts/10000000001")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"activeStatus\":\"Y\",\"creditLimit\":15000}"))
            .andExpect(status().isOk());
    }
}
