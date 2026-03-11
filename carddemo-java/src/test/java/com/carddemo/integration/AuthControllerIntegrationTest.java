package com.carddemo.integration;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserSecurityRepository userSecurityRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (userSecurityRepository.findById("ADMIN001").isEmpty()) {
            UserSecurity admin = new UserSecurity();
            admin.setUsrId("ADMIN001");
            admin.setUsrFname("ADMIN");
            admin.setUsrLname("USER");
            admin.setUsrPwd(passwordEncoder.encode("PASSWORD"));
            admin.setUsrType("A");
            userSecurityRepository.save(admin);
        }
    }

    @Test
    void login_success() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.userType").value("ADMIN"));
    }

    @Test
    void login_invalidCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"WRONG\"}"))
            .andExpect(status().isBadRequest());
    }
}
