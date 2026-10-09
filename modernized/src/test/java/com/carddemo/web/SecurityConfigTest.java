package com.carddemo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.AbstractIntegrationTest;
import com.carddemo.loader.LegacyDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class SecurityConfigTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private LegacyDataLoader loader;

    @BeforeEach
    void seed() {
        loader.loadUsers();
    }

    @Test
    void signonIsPublicButOtherEndpointsRequireCredentials() throws Exception {
        mvc.perform(post("/api/v1/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user0001\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/users")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/menus/main")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointsRequireAdminUserType() throws Exception {
        mvc.perform(get("/api/v1/menus/main").with(httpBasic("user0001", "password"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/users").with(httpBasic("USER0001", "PASSWORD"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/users").with(httpBasic("admin001", "password"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/users").with(httpBasic("ADMIN001", "WRONG"))).andExpect(status().isUnauthorized());
    }
}
