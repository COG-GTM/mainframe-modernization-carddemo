package com.cardemo.batch.orchestration.integration;

import com.cardemo.batch.orchestration.BatchOrchestrationApplication;
import com.cardemo.batch.orchestration.config.ActuatorConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the custom batch jobs Actuator endpoint.
 */
@SpringBootTest(classes = BatchOrchestrationApplication.class)
@ActiveProfiles("test")
class ActuatorEndpointIntegrationTest {

    @Autowired
    private ActuatorConfig.BatchJobsEndpoint batchJobsEndpoint;

    @Test
    void batchJobStatus_returnsAllThreeFlows() {
        Map<String, Object> status = batchJobsEndpoint.batchJobStatus();

        assertNotNull(status);
        assertTrue(status.containsKey("posttran"));
        assertTrue(status.containsKey("intcalc"));
        assertTrue(status.containsKey("creastmt"));
        assertTrue(status.containsKey("metrics"));
    }

    @Test
    void batchJobStatus_neverRunJobs_showCorrectStatus() {
        Map<String, Object> status = batchJobsEndpoint.batchJobStatus();

        @SuppressWarnings("unchecked")
        Map<String, Object> posttranInfo = (Map<String, Object>) status.get("posttran");
        assertEquals("NEVER_RUN", posttranInfo.get("status"));
    }
}
