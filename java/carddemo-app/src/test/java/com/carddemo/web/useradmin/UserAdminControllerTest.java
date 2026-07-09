package com.carddemo.web.useradmin;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

/**
 * End-to-end tests for the CS-9 admin user-maintenance API ({@code COUSR00C}/{@code 01C}/
 * {@code 02C}/{@code 03C}), driven against the real USRSEC seed users loaded from
 * {@code usrsec.txt} ({@code ADMIN001..ADMIN005} type 'A', {@code USER0001..USER0005} type 'U').
 * The class is {@code @Transactional} so each mutating test rolls back — the shared in-memory
 * H2 store is left untouched for other test classes (e.g. the seed row-count test).
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@Transactional
class UserAdminControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void listReturnsSeedUsersOrderedById() throws Exception {
        mockMvc.perform(get("/api/admin/users").param("startUserId", "ADMIN001").param("size", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(3))
            .andExpect(jsonPath("$.moreAvailable").value(true))
            .andExpect(jsonPath("$.users[0].userId").value("ADMIN001"))
            .andExpect(jsonPath("$.users[0].firstName").value("MARGARET"))
            .andExpect(jsonPath("$.users[0].lastName").value("GOLD"))
            .andExpect(jsonPath("$.users[0].userType").value("A"))
            .andExpect(jsonPath("$.users.length()").value(3));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void addValidUserReturns201AndConfirmation() throws Exception {
        String body = "{\"userId\":\"NEWUSR01\",\"firstName\":\"NEW\",\"lastName\":\"USER\","
                + "\"password\":\"PASS1\",\"userType\":\"A\"}";
        mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value("NEWUSR01"))
            .andExpect(jsonPath("$.userType").value("A"))
            .andExpect(jsonPath("$.message").value("User NEWUSR01 has been added ..."));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void addDuplicateUserReturns409() throws Exception {
        String body = "{\"userId\":\"ADMIN001\",\"firstName\":\"MARGARET\",\"lastName\":\"GOLD\","
                + "\"password\":\"PASSWORD\",\"userType\":\"A\"}";
        mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("User ID already exist..."));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void addMissingFirstNameReturnsCobolMessage() throws Exception {
        String body = "{\"userId\":\"NEWUSR02\",\"firstName\":\"\",\"lastName\":\"USER\","
                + "\"password\":\"PASS1\",\"userType\":\"A\"}";
        mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("First Name can NOT be empty..."));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void addInvalidUserTypeReturnsBadRequest() throws Exception {
        String body = "{\"userId\":\"NEWUSR03\",\"firstName\":\"NEW\",\"lastName\":\"USER\","
                + "\"password\":\"PASS1\",\"userType\":\"X\"}";
        mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("User Type must be 'A' (admin) or 'U' (user)..."));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void updateExistingUserReturnsConfirmation() throws Exception {
        String body = "{\"firstName\":\"LEE\",\"lastName\":\"CHANGED\","
                + "\"password\":\"PASSWORD\",\"userType\":\"U\"}";
        mockMvc.perform(put("/api/admin/users/USER0005").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("USER0005"))
            .andExpect(jsonPath("$.lastName").value("CHANGED"))
            .andExpect(jsonPath("$.message").value("User USER0005 has been updated ..."));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void updateUnknownUserReturns404() throws Exception {
        String body = "{\"firstName\":\"NO\",\"lastName\":\"BODY\","
                + "\"password\":\"PASSWORD\",\"userType\":\"U\"}";
        mockMvc.perform(put("/api/admin/users/NOSUCH99").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User ID NOT found..."));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void deleteExistingUserReturnsConfirmation() throws Exception {
        mockMvc.perform(delete("/api/admin/users/USER0004"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User USER0004 has been deleted ..."));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void deleteUnknownUserReturns404() throws Exception {
        mockMvc.perform(delete("/api/admin/users/NOSUCH99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User ID NOT found..."));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void regularUserIsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedIsRejected() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized());
    }
}
