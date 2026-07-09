package com.carddemo.batch.orchestration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Verifies the {@code ROLE_ADMIN} guard and the pipeline listing of {@link BatchJobController}.
 * Uses an isolated in-memory H2 database so it does not interfere with the pipeline job tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carddemo-cs14-web;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class BatchJobControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void unauthenticatedIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/batch/jobs")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void regularUserIsForbidden() throws Exception {
        mockMvc.perform(get("/api/batch/jobs")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/batch/jobs/posttran")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void adminSeesPipelineCatalog() throws Exception {
        mockMvc.perform(get("/api/batch/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name=='posttran')].jobName").value("postTranPipelineJob"))
            .andExpect(jsonPath("$[?(@.name=='intcalc')]").exists())
            .andExpect(jsonPath("$[?(@.name=='creastmt')]").exists());
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    void adminLaunchingUnknownPipelineGets404() throws Exception {
        mockMvc.perform(post("/api/batch/jobs/nope"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Unknown batch pipeline: nope"));
    }
}
