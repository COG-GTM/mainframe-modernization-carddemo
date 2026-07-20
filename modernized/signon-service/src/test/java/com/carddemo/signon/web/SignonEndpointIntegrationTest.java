package com.carddemo.signon.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.signon.domain.UserSecurityRepository;
import com.carddemo.signon.domain.UsrsecRecordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test: exercises the {@code POST /api/signon} endpoint wired to the
 * real service and a real H2 datastore. The datastore is seeded from raw 80-byte
 * {@code USRSEC} records (parsed via {@link UsrsecRecordMapper}), so this also
 * verifies the copybook {@code CSUSR01Y} field mapping all the way through
 * persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SignonEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserSecurityRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    // Raw fixed-width USRSEC records matching app/jcl/DUSRSECJ.jcl.
    private static final String ADMIN_RECORD =
            record("ADMIN001", "MARGARET", "GOLD", "PASSWORD", 'A');
    private static final String USER_RECORD =
            record("USER0001", "LAWRENCE", "THOMAS", "PASSWORD", 'U');

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(UsrsecRecordMapper.parse(ADMIN_RECORD));
        repository.save(UsrsecRecordMapper.parse(USER_RECORD));
    }

    @Test
    void adminSignonReturnsAdminRouting() throws Exception {
        mockMvc.perform(signon("ADMIN001", "PASSWORD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.outcome").value("ADMIN_MENU"))
                .andExpect(jsonPath("$.destinationProgram").value("COADM01C"))
                .andExpect(jsonPath("$.userType").value("ADMIN"));
    }

    @Test
    void regularSignonReturnsMainRouting() throws Exception {
        mockMvc.perform(signon("USER0001", "PASSWORD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.outcome").value("MAIN_MENU"))
                .andExpect(jsonPath("$.destinationProgram").value("COMEN01C"))
                .andExpect(jsonPath("$.userType").value("USER"));
    }

    @Test
    void wrongPasswordReturns401WithMainframeMessage() throws Exception {
        mockMvc.perform(signon("USER0001", "NOPE"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }

    @Test
    void unknownUserReturns401WithMainframeMessage() throws Exception {
        mockMvc.perform(signon("NOSUCH99", "PASSWORD"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.outcome").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found. Try again ..."));
    }

    @Test
    void blankUserIdReturns400() throws Exception {
        mockMvc.perform(signon("", "PASSWORD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter User ID ..."));
    }

    @Test
    void blankPasswordReturns400() throws Exception {
        mockMvc.perform(signon("ADMIN001", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter Password ..."));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder signon(
            String userId, String password) throws Exception {
        return post("/api/signon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignonRequest(userId, password)));
    }

    private static String record(String id, String fname, String lname, String pwd, char type) {
        return pad(id, 8) + pad(fname, 20) + pad(lname, 20) + pad(pwd, 8) + type + " ".repeat(23);
    }

    private static String pad(String value, int length) {
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
